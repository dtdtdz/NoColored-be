package com.ssafy.backend.websocket.application;

import com.ssafy.backend.websocket.dao.SessionRepository;
import com.ssafy.backend.websocket.domain.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@Service
public class BinaryMessageServiceImpl implements BinaryMessageService {

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private SessionRepository sessionRepository;

    private ByteBuffer[] buffer;

    @PostConstruct
    public void construct(){
        buffer = new ByteBuffer[GameInfo.MAX_PLAYER];
        for (int i=0; i<buffer.length; i++){
            buffer[i] = ByteBuffer.allocate(1024);
        }
    }
    @Override
    public void setRoom(WebSocketSession session) {

    }

    @Override
    public void getRoomInfoList() {

    }
    @EventListener(ApplicationReadyEvent.class)
    public void scheduleTaskAfterStartup() {
        long initialDelay = 0; // 시작 지연 없음
        long period = 16_666; // 17ms

        // 여기에 반복 실행할 태스크의 로직을 작성
        //            System.out.println("태스크 실행: " + System.currentTimeMillis());
        scheduledExecutorService.scheduleAtFixedRate(this::physics, initialDelay, period, TimeUnit.MICROSECONDS);
    }

    private void physics (){
//        System.out.print("1");
        for (GameInfo gameInfo : SessionRepository.inGameList){
            if (Duration.between(gameInfo.getStartTime(), LocalDateTime.now()).getSeconds()>=100){
                System.out.println("game close");
                for (Map.Entry<WebSocketSession, UserGameInfo> entry: gameInfo.getUsers().entrySet()){
                    SessionRepository.inGameUser.remove(entry.getKey());
                }

                SessionRepository.inGameList.remove(gameInfo);

            } else {
                long dt = gameInfo.tick();

                MapInfo mapInfo = gameInfo.getMapInfo();
                CharacterInfo[] characterInfoArr = gameInfo.getCharacterInfoArr();
                boolean[][] floor = mapInfo.getFloor();

                PriorityQueue<CharacterInfo> characterQueue = new PriorityQueue<>(Comparator.comparingDouble(CharacterInfo::getY));

                for (Map.Entry<WebSocketSession, UserGameInfo> entry: gameInfo.getUsers().entrySet()){
                    int bufferNum = entry.getValue().getBufferNum();
                    buffer[bufferNum].clear();
                    buffer[bufferNum].put(SendBinaryMessageType.PHYSICS_STATE.getValue())
                            .put((byte) (4*4*characterInfoArr.length))
                            .put((byte) 0).put((byte) 0);
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
//                    System.out.print(3+":"+i+":1");
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
                                    && mapInfo.getFloor()[blockLeft][blockY])
                                    || (indexCheck(blockRight, floor.length)
                                    && mapInfo.getFloor()[blockRight][blockY]))){
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
                                        && mapInfo.getFloor()[blockLeft][nextBlockY])
                                        || (indexCheck(blockRight, floor.length)
                                        && mapInfo.getFloor()[blockRight][nextBlockY]))
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

//                    System.out.print(3+":"+i+":2");
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
//                    if (gameInfo.getMapInfo().getFloor()[(int)(tarX-halfSize)])
//                    o->o, o->x, x->o, x->x
//                    플랫폼위에 없으면 중력가속도 적용
//                    플랫폼위에 있다면 velY = 0;

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
                            break;
                        }
                    }
                    if (!flag && curC.isPlayer()){
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

                    try {

                        buffer[bufferNum].flip();
                        entry.getKey().sendMessage(new BinaryMessage(buffer[bufferNum]));
                    } catch (IOException e) {
//                    e.printStackTrace();
//                    roomInfo.getSessions().remove(session);
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    } catch (Exception e){
                        System.out.println("can't find session");
                        SessionRepository.inGameUser.remove(entry.getKey());
                        SessionRepository.inGameList.remove(gameInfo);
//                    e.printStackTrace();
                    }
                }
            }
        }
    }

    public boolean indexCheck(int idx, int size){
        return idx>=0 && idx<size;
    }

    @Override
    public void binaryMessageProcessing(WebSocketSession session, BinaryMessage message) {
        ByteBuffer byteBuffer = message.getPayload();

        // 여기에서 바이너리 데이터 처리
        byte[] arr = byteBuffer.array();
//        System.out.println("Received binary message of size: " + byteBuffer.remaining());

        ReceiveBinaryMessageType binaryMessageType = ReceiveBinaryMessageType.valueOf(arr[0]);
        if (binaryMessageType==null) return;
//        System.out.println(binaryMessageType);
        switch (binaryMessageType){
            case LEFT -> applyLeft(session);
            case RIGHT -> applyRight(session);
            case JUMP -> applyJump(session);
            case TEST_START -> testStart(session);
            case TEST_LOGIN -> testLogin(session);
        }
    }
    private void applyLeft(WebSocketSession session){
        GameInfo gameInfo = SessionRepository.inGameUser.get(session);
        int idx = gameInfo.getUsers().get(session).getCharacterNum();
        gameInfo.toLeft(idx);
//        System.out.println(0);
    }
    private void applyRight(WebSocketSession session){
        GameInfo gameInfo = SessionRepository.inGameUser.get(session);
        int idx = gameInfo.getUsers().get(session).getCharacterNum();
        gameInfo.toRight(idx);
//        System.out.println(1);
    }

    private void applyJump(WebSocketSession session){
        //바닥에 있으면 점프
        GameInfo gameInfo = SessionRepository.inGameUser.get(session);
        int idx = gameInfo.getUsers().get(session).getCharacterNum();
        gameInfo.jump(idx);
//        System.out.println(1);
    }

    private void testStart(WebSocketSession session){
//        RoomInfo roomInfoEx = new RoomInfo(SessionRepository.loginUserMap(session));


        GameInfo gameInfo = new GameInfo();
        //수정 필요
        gameInfo.putSession(session);
        SessionRepository.inGameList.add(gameInfo);
        SessionRepository.inGameUser.put(session, gameInfo);
    }

    private void testLogin(WebSocketSession session){

    }
}
