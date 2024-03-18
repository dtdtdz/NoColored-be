package com.ssafy.backend.game.service;

import com.ssafy.backend.game.util.InGameCollection;
import com.ssafy.backend.websocket.util.SessionCollection;
import com.ssafy.backend.game.domain.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@Service
public class GameServiceImpl implements GameService {

    private final ScheduledExecutorService scheduledExecutorService;
//    private final ScheduledExecutorService
    private final SessionCollection sessionCollection;
    private final InGameCollection inGameCollection;
    private final ByteBuffer[] buffer;
    private final PriorityQueue<CharacterInfo> characterQueue;


    private ScheduledFuture<?> future;
    GameServiceImpl(@Qualifier("scheduledExecutorService")ScheduledExecutorService scheduledExecutorService,
                    SessionCollection sessionRepository,
                    InGameCollection inGameRepository){
        this.scheduledExecutorService = scheduledExecutorService;
        this.sessionCollection = sessionRepository;
        this.inGameCollection = inGameRepository;
        buffer = new ByteBuffer[GameInfo.MAX_PLAYER];
        for (int i=0; i<buffer.length; i++){
            buffer[i] = ByteBuffer.allocate(1024);
        }
        characterQueue = new PriorityQueue<>(Comparator.comparingDouble(CharacterInfo::getY));
    }
    @EventListener(ApplicationReadyEvent.class)
    public void scheduleTaskAfterStartup() {
        long initialDelay = 0; // 시작 지연 없음
        long period = 16_666; // 17ms

        // 여기에 반복 실행할 태스크의 로직을 작성
        //            System.out.println("태스크 실행: " + System.currentTimeMillis());
        future = scheduledExecutorService.scheduleAtFixedRate(this::gameLogic, initialDelay, period, TimeUnit.MICROSECONDS);
    }
    @EventListener
    public void onApplicationEvent(ContextClosedEvent event) {
//        future.cancel(false);
    }

    @Override
    public void setRoom(WebSocketSession session) {

    }

    @Override
    public void getRoomInfoList() {

    }




    private void physics(GameInfo gameInfo){
        long dt = gameInfo.tick();

        MapInfo mapInfo = gameInfo.getMapInfo();
        CharacterInfo[] characterInfoArr = gameInfo.getCharacterInfoArr();
        boolean[][] floor = gameInfo.getFloor();

        characterQueue.clear();


        boolean checkSecond = gameInfo.checkSecond();
        gameInfo.getStepList().clear();

        for (Map.Entry<WebSocketSession, UserGameInfo> entry: gameInfo.getUsers().entrySet()){
            int bufferNum = entry.getValue().getBufferNum();
            buffer[bufferNum].clear();
            if (checkSecond) gameInfo.putTime(buffer[bufferNum]);
            buffer[bufferNum].put(SendBinaryMessageType.PHYSICS_STATE.getValue())
                    .put((byte) (16))
                    .put((byte) characterInfoArr.length);
        }
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
                                curC.getUserGameInfo().getPlayerNum(), user.getScore()});
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
        for (Map.Entry<WebSocketSession,UserGameInfo> entry: gameInfo.getUsers().entrySet()){
            int bufferNum = entry.getValue().getBufferNum();

            for (CharacterInfo cInfo:characterInfoArr){

                buffer[bufferNum].putFloat(cInfo.getX());
                buffer[bufferNum].putFloat(cInfo.getY());
                buffer[bufferNum].putFloat(cInfo.getVelX());
                buffer[bufferNum].putFloat(cInfo.getVelY());
            }


            if (!gameInfo.getStepList().isEmpty()){
                gameInfo.putStep(buffer[bufferNum]);
//                        System.out.println(stepList.size());
            }

            try {

                buffer[bufferNum].flip();

                synchronized (entry.getKey()){
                    entry.getKey().sendMessage(new BinaryMessage(buffer[bufferNum]));
                }
            } catch (IOException e) {
//                    e.printStackTrace();
//                    roomInfo.getSessions().remove(session);
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (Exception e){
                System.out.println("can't find session");
//                        SessionRepository.userWebsocketMap.get(entry.getKey()).setGameInfo(null);
                inGameCollection.inGameUser.remove(entry.getKey());
                inGameCollection.removeGame(gameInfo);
                e.printStackTrace();
            }
        }
    }
    private void gameLogic(){
        try {
            inGameCollection.updateGameList();
            Iterator<GameInfo> gameInfoIterator = inGameCollection.getGameInfoIterator();
            while (gameInfoIterator.hasNext()){
                GameInfo gameInfo = gameInfoIterator.next();
                if (Duration.between(gameInfo.getStartDate(), LocalDateTime.now()).getSeconds()>=100){
                    System.out.println("game close");
                    for (Map.Entry<WebSocketSession, UserGameInfo> entry: gameInfo.getUsers().entrySet()){
                        inGameCollection.inGameUser.remove(entry.getKey());
                    }

                    inGameCollection.removeGame(gameInfo);

                } else {
                    try {
                        physics(gameInfo);
                    } catch (Exception e){
                        e.printStackTrace();
                        throw e;
                    }
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public boolean indexCheck(int idx, int size){
        return idx>=0 && idx<size;
    }

}
