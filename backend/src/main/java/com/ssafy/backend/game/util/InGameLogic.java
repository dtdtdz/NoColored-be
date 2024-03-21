package com.ssafy.backend.game.util;

import com.ssafy.backend.game.domain.CharacterInfo;
import com.ssafy.backend.game.domain.GameInfo;
import com.ssafy.backend.game.domain.MapInfo;
import com.ssafy.backend.game.domain.UserGameInfo;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InGameLogic {

    private final PriorityQueue<CharacterInfo> characterQueue;
    public InGameLogic(){
        characterQueue = new PriorityQueue<>(Comparator.comparingDouble(CharacterInfo::getY));
    }
    public void scheduledLogic(GameInfo gameInfo){
        switch (gameInfo.getGameCycle()){
//            case CREATE -> create(gameInfo);
//            case READY -> ready(gameInfo);
//            case PLAY -> play(gameInfo);
//            case CLOSE -> close(gameInfo);
        }
        play(gameInfo);

    }
    public void create(GameInfo gameInfo){
        //http 요청을 10초 기다린다. -> gameinfo 생성자
        //모든 유저준비 완료
        if (gameInfo.isAllReady() || gameInfo.checkSecond() && gameInfo.getSecond()<=0){
            //사람 없으면 게임 제거
            gameInfo.putReadyInfo();
            gameInfo.sendBuffer();
            gameInfo.goToNextCycle();
        }
        gameInfo.sendBuffer();
    }
    public void ready(GameInfo gameInfo){
//        gameInfo.putReadyInfo();
//        gameInfo.sendBuffer();
    }

    public void play(GameInfo gameInfo){
        long dt = gameInfo.tick();

        MapInfo mapInfo = gameInfo.getMapInfo();
        CharacterInfo[] characterInfoArr = gameInfo.getCharacterInfoArr();
        boolean[][] floor = gameInfo.getFloor();

        characterQueue.clear();
//                System.out.print(2);
//phaser.js 에서 x좌표 이동 후 중력가속도 적용하는것처럼 작동함
        for (int i=0; i<characterInfoArr.length; i++){
            CharacterInfo cInfo = characterInfoArr[i];
            float tarX = cInfo.getX()+(dt/1000f)*cInfo.getVelX();
            float halfSize = GameInfo.CHARACTER_SIZE/2f;
            if (tarX + halfSize > mapInfo.getRight()){
                gameInfo.toLeft(i);
                tarX = -tarX+2*(mapInfo.getRight()-halfSize);
            } else if (tarX - halfSize < mapInfo.getLeft()){
                gameInfo.toRight(i);
                tarX = 2*(mapInfo.getLeft()+halfSize)-tarX;
            }
//                    System.out.print(3+":"+i+":1 ");
            float velY = cInfo.getVelY();
            float tarY = cInfo.getY();
//                    +(dt/1000f)*cInfo.getVelY()
            boolean isPlatForm = false;
//                    left = tarX-half, right = tarX+half, bottom = tarY+half
//                    [bottom/18] (bottom%18)<=4
//                    (left/18)
//                    [(right/18)-1]
//                    y축 이동 전 플랫폼 충돌

            int blockLeft = (int)((tarX-halfSize)/GameInfo.BLOCK_SIZE)-GameInfo.WALL_WIDTH;
            int blockRight =(int) Math.ceil((tarX+halfSize)/GameInfo.BLOCK_SIZE)-1-GameInfo.WALL_WIDTH;
            float bottom = tarY+halfSize;
            int blockY = (int)(bottom/GameInfo.BLOCK_SIZE);
            try {
                if (velY>=0&&bottom%GameInfo.BLOCK_SIZE<=4){

                    if (indexCheck(blockY, floor[0].length)
                            && ((indexCheck(blockLeft, floor.length)
                            && floor[blockLeft][blockY])
                            || (indexCheck(blockRight, floor.length)
                            && floor[blockRight][blockY]))){
                        tarY = blockY*GameInfo.BLOCK_SIZE-halfSize;
                        isPlatForm = true;
                    }
                }

                if (!isPlatForm){
                    velY += (dt/1000f)*GameInfo.GRAVITY;
                    tarY += (dt/1000f)*velY;
                    if (tarY > (GameInfo.MAP_HEIGHT*GameInfo.BLOCK_SIZE+halfSize)){
                        tarY %= (GameInfo.MAP_HEIGHT*GameInfo.BLOCK_SIZE+halfSize);
                    }


                    bottom = tarY+halfSize;
                    int nextBlockY = (int)(bottom/GameInfo.BLOCK_SIZE);

                    if (velY>=0 &&((bottom%GameInfo.BLOCK_SIZE<=4)||blockY<nextBlockY)){
                        if (indexCheck(nextBlockY, floor[0].length)
                                && ((indexCheck(blockLeft, floor.length)
                                && floor[blockLeft][nextBlockY])
                                || (indexCheck(blockRight, floor.length)
                                && floor[blockRight][nextBlockY]))
                        ){
                            tarY = nextBlockY*GameInfo.BLOCK_SIZE-halfSize;
                            isPlatForm = true;
                        }
                    }
                }
            } catch (Exception e){
//                        System.out.println("err:"+tarX+" "+tarY);
                e.printStackTrace();
            }

//                    System.out.print(3+":"+i+":2 ");
            if (isPlatForm) {
                if (cInfo.isJump()){
                    velY = -190;
                    cInfo.setJump(false);
                } else {
                    velY = 0;
                }
            } else {
                cInfo.setJump(false);
            }

            cInfo.setX(tarX);
            cInfo.setY(tarY);
            cInfo.setVelY(velY);

            characterQueue.offer(cInfo);
        }

        LinkedList<CharacterInfo> validCharacter = new LinkedList<>();
        Queue<CharacterInfo> removeCharacter = new ArrayDeque<>();
        int cSize = GameInfo.CHARACTER_SIZE;
        while (!characterQueue.isEmpty()){
            CharacterInfo curC = characterQueue.poll();
            boolean flag = false;
            for (CharacterInfo listC:validCharacter){
                if (listC.getY()+cSize<=curC.getY()){
                    removeCharacter.offer(listC);
                } else if(listC.getY()+cSize/2f<curC.getY()
                        && Math.abs(listC.getX()-curC.getX())<cSize
                        && listC.getVelY()>curC.getVelY()
                ){
//                            y좌표 캐릭터 크기에 대해 0.5<차이<1일때, 캐릭터 x축 겹치는가?
//                            속도 위에있는게 아래 밟을 수 있나?
                    listC.setVelY(-100);
                    flag = true;

                    if (curC.getUserGameInfo()!=null){
                        UserGameInfo user = listC.getUserGameInfo();
                        user.setScore((byte) (user.getScore()+1));
                        gameInfo.getStepList().add(new byte[]{ user.getPlayerNum(),
                                curC.getUserGameInfo().getPlayerNum(),
                                user.getCharacterNum(),
                                curC.getUserGameInfo().getCharacterNum(),
                                user.getScore()});
                    }

                    break;//밟힌 캐릭터는 더 밟힐 수 없다.
                }
            }
            if (!flag && curC.getUserGameInfo()!=null){
                validCharacter.offer(curC);
            }

            while (!removeCharacter.isEmpty()){
                CharacterInfo tmp = removeCharacter.poll();
                validCharacter.remove(tmp);
            }
        }
//                System.out.println(4);
//                System.out.println("game logic");

        gameInfo.putTime();
        gameInfo.putPhysicsState();
        gameInfo.putStep();
        gameInfo.sendBuffer();
    }
    public void close(GameInfo gameInfo){}

    public boolean indexCheck(int idx, int size){
        return idx>=0 && idx<size;
    }
}
