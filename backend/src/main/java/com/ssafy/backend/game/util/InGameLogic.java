package com.ssafy.backend.game.util;

import com.ssafy.backend.game.domain.*;
import com.ssafy.backend.game.type.GameCharacterState;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class InGameLogic {

    private final PriorityQueue<CharacterInfo> characterQueue;
    public InGameLogic(){
        characterQueue = new PriorityQueue<>(Comparator.comparingDouble(CharacterInfo::getY));
    }
    public void create(GameInfo gameInfo){
        gameInfo.tick();
        //http 요청을 10초 기다린다. -> gameinfo 생성자
        //모든 유저준비 완료
        //사람 없으면 게임 제거
        if (gameInfo.isAllReady() || gameInfo.checkSecond() && gameInfo.getSecond()<=0){
            gameInfo.setSecond(3);
            gameInfo.putCountDown();
//            3초 카운트 시작
            gameInfo.putCharacterMapping();
            gameInfo.putPhysicsState();
//            gameInfo.putTestMap();
            gameInfo.goToNextCycle();
            gameInfo.sendBuffer();
        }
        long dt2 =System.currentTimeMillis();
    }
    public void ready(GameInfo gameInfo){

        gameInfo.tick();
        if (gameInfo.checkSecond()){

            if (gameInfo.getSecond()<=0){
                gameInfo.putStart();
                gameInfo.putCountDown();
                gameInfo.putCharacterMapping();
                gameInfo.goToNextCycle();
                for (CharacterInfo characterInfo: gameInfo.getCharacterInfoArr()){
                    characterInfo.setVelX(GameInfo.DEFAULT_VEL_X);
                }
                gameInfo.setSecond(GameInfo.GAME_TIME);
                gameInfo.setItemTime();
            } else {
                gameInfo.putCountDown();
            }
            gameInfo.sendBuffer();
        }

        long dt2 =System.currentTimeMillis();
    }

    public void play(GameInfo gameInfo){
        long dt = gameInfo.tick();

        if (gameInfo.checkSecond()){
            if (gameInfo.getSecond()<=0){
                gameInfo.goToNextCycle();
                gameInfo.putTime();
                gameInfo.putEnd();
                gameInfo.sendBuffer();
                return;
            }
            gameInfo.putTime();
        }
        MapInfo mapInfo = gameInfo.getMapInfo();
        CharacterInfo[] characterInfoArr = gameInfo.getCharacterInfoArr();
        boolean[][] floor = gameInfo.getFloor();

        characterQueue.clear();
//phaser.js 에서 x좌표 이동 후 중력가속도 적용하는것처럼 작동함
        gameInfo.setCharacterDirection();
        for (int i=0; i<characterInfoArr.length; i++){
            CharacterInfo cInfo = characterInfoArr[i];

            float tarX = cInfo.getX()+(dt/1000f)*cInfo.getVelX()*cInfo.getDir();
            float halfSize = GameInfo.CHARACTER_SIZE/2f;
            if (tarX + halfSize > mapInfo.getRight()){
                gameInfo.toLeft(i);
                tarX = -tarX+2*(mapInfo.getRight()-halfSize);
            } else if (tarX - halfSize < mapInfo.getLeft()){
                gameInfo.toRight(i);
                tarX = 2*(mapInfo.getLeft()+halfSize)-tarX;
            }
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
                e.printStackTrace();
            }

            if (cInfo.getUserGameInfo()!=null &&
                    cInfo.getStates().containsKey(GameCharacterState.STOP)){
                cInfo.setJump(false);
            }

            if (isPlatForm) {
                if (cInfo.isJump()){
                    velY = GameInfo.JUMP_VEL_Y;
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
            gameInfo.itemUse(curC);
            if (curC.getUserGameInfo()!=null &&
            curC.getStates().containsKey(GameCharacterState.STEPED)) continue;
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
                    listC.setVelY(GameInfo.STEP_VEL_Y);
                    flag = true;

                    if (curC.getUserGameInfo()!=null){
                        UserGameInfo user = listC.getUserGameInfo();
                        user.setScore((byte) (user.getScore()+1));

                        gameInfo.getStepList().add(new byte[]{ user.getPlayerNum(),
                                curC.getUserGameInfo().getPlayerNum(),
                                user.getCharacterNum(),
                                curC.getUserGameInfo().getCharacterNum()});
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

            long dt1 = System.currentTimeMillis();
            gameInfo.applyStep();
            gameInfo.applyItem();

            gameInfo.putCharacterMapping();
            gameInfo.putPhysicsState();
            gameInfo.putItem();

            gameInfo.putScore();
            gameInfo.putEffect();
            gameInfo.putSkin();
            long dt2 = System.currentTimeMillis();
            if (dt2-dt1>50) System.out.println("set"+(dt2-dt1));
            gameInfo.sendBuffer();
            dt2 = System.currentTimeMillis();
            if (dt2-dt1>50) System.out.println("send"+(dt2-dt1));
    }
    public boolean indexCheck(int idx, int size){
        return idx>=0 && idx<size;
    }
}
