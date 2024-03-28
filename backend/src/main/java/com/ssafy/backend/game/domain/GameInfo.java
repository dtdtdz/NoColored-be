package com.ssafy.backend.game.domain;

import com.ssafy.backend.assets.SynchronizedSend;
import com.ssafy.backend.websocket.domain.SendBinaryMessageType;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.*;


@Getter
@AllArgsConstructor
public class GameInfo {
    private LocalDateTime startDate;
    private long targetTime;
    private long time;
    private int second;
    private Map<WebSocketSession, UserGameInfo> users = new LinkedHashMap<>();
    private List<UserGameInfo> userGameInfoList = new LinkedList<>();
    private MapInfo mapInfo;
    private CharacterInfo[] characterInfoArr;
    private boolean[][] floor;
    private Random random;
    private GameRoomDto gameRoomDto;
    private UUID roomUuid;

    private List<byte[]> stepList;
    private List<Effect> effectList;
    private List<byte[]> displaySkinList;
    private GameItemType type;

    //이것들 리팩토링 고려
    public static final int CHARACTER_SIZE = 27;
    public static final float DEFAULT_SPEED = 160;
    public static final int MAX_PLAYER = 2;
    public static final float GRAVITY = 200;
    public static final int BLOCK_SIZE = 18;
    public static final int MAP_HEIGHT = 19;
    public static final int MAP_WIDTH = 27;
    public static final int WALL_WIDTH = 3;
    public static final int DEFAULT_TIME = 120;
    public static final int CHARACTER_NUM = 10;

    public static final ByteBuffer[] buffer = new ByteBuffer[4];
    static {
        for (int i=0; i<buffer.length; i++){
            buffer[i] = ByteBuffer.allocate(2048);
        }
    }


    public enum GameCycle {
        CREATE {
            @Override
            public GameCycle next() {
                return READY;
            }
        },
        READY {
            @Override
            public GameCycle next() {
                return PLAY;
            }
        },
        PLAY {
            @Override
            public GameCycle next() {
                return CLOSE;
            }
        },
        CLOSE {
            @Override
            public GameCycle next() {
                return CLOSE;
            }
        };

        // 모든 열거형 상수가 구현해야 하는 추상 메서드
        public abstract GameCycle next();
    }

    private GameCycle gameCycle;
    public GameInfo(List<UserAccessInfo> userList, UUID roomUuid){
        this(userList);
        this.roomUuid = roomUuid;
    }
    public GameInfo(List<UserAccessInfo> userList){
        startDate = LocalDateTime.now();
        setSecond(3);
        mapInfo = new MapInfo();
        floor = new boolean[MAP_WIDTH][MAP_HEIGHT];
        characterInfoArr = new CharacterInfo[CHARACTER_NUM];
        random = new Random();
        gameCycle = GameCycle.CREATE;

        stepList = new ArrayList<>();
        effectList = new LinkedList<>();
        displaySkinList = new LinkedList<>();
        //캐릭터 위치 랜덤배치
        List<int[]> floorPos = new LinkedList<>();
        //유저 캐릭터 번호 랜덤 매핑
        List<Byte> idxs = new LinkedList<>();
        for (int[] arr:mapInfo.getFloorList()){
            for (int i=0; i<arr[2]; i++){
                floor[arr[0]+i-WALL_WIDTH][arr[1]] = true;
                floorPos.add(new int[]{arr[0]+i-WALL_WIDTH,arr[1]});
            }
        }

        for (byte i=0; i<CHARACTER_NUM; i++){
            idxs.add(i);
        }
        Collections.shuffle(idxs);
        Collections.shuffle(floorPos);

        for (byte i=0; i<userList.size(); i++){
            UserGameInfo userGameInfo = new UserGameInfo(userList.get(i).getSession(),
                    idxs.get(i),i);
            CharacterInfo characterInfo = new CharacterInfo();

            characterInfo.setUserGameInfo(userGameInfo);
            characterInfo.setX((floorPos.get(i)[0]+1/2f+WALL_WIDTH)*BLOCK_SIZE);
            characterInfo.setY(floorPos.get(i)[1]*BLOCK_SIZE-CHARACTER_SIZE/2f);
            characterInfo.setDir((int) ((random.nextInt(2)-0.5f)*2));
            characterInfo.setVelX(0);
            characterInfo.setVelY(0);

            characterInfoArr[idxs.get(i)] = characterInfo;
            users.put(userList.get(i).getSession(), userGameInfo);
            userGameInfoList.add(userGameInfo);
        }

        for (int i= userList.size(); i<characterInfoArr.length ; i++){
            CharacterInfo characterInfo = new CharacterInfo();

            characterInfo.setX((floorPos.get(i)[0]+1/2f+WALL_WIDTH)*BLOCK_SIZE);
            characterInfo.setY(floorPos.get(i)[1]*BLOCK_SIZE-CHARACTER_SIZE/2f);
            characterInfo.setDir((int) ((random.nextInt(2)-0.5f)*2));
            characterInfo.setVelX(0);
            characterInfo.setVelY(0);

            characterInfoArr[idxs.get(i)] = characterInfo;
        }

        gameRoomDto = new GameRoomDto();
        List<String> skins = new LinkedList<>();
        for (UserAccessInfo userAccessInfo:userList){
            skins.add(userAccessInfo.getUserProfile().getUserSkin());
        }
        gameRoomDto.setSkins(skins);
        gameRoomDto.setFloorList(mapInfo.getFloorList());
        gameRoomDto.setMapId(mapInfo.getMapId());
    }

//    private GameInfo(int num){ //리팩토링 필요
//        startDate = LocalDateTime.now();
//        startTime = System.currentTimeMillis();
//        time = startTime;
//        second = DEFAULT_TIME;
//        characterInfoArr = new CharacterInfo[CHARACTER_NUM];
//        for (int i=0; i<characterInfoArr.length; i++){
//            characterInfoArr[i] = new CharacterInfo();
//            characterInfoArr[i].setX((1+i)*100);
//            characterInfoArr[i].setY(0);
//            characterInfoArr[i].setVelX(DEFAULT_SPEED);
//        }
//
//        mapInfo = new MapInfo();//num
//        floor = new boolean[MAP_HEIGHT][MAP_WIDTH];
//    }

    public boolean isAllReady(){
        for (Map.Entry<WebSocketSession, UserGameInfo> entry:users.entrySet()){
            if (!entry.getValue().isAccess()) return false;
        }
        return true;
    }

    public void toLeft(int idx){
        characterInfoArr[idx].setDir(-1);
    }

    public void toRight(int idx){
        characterInfoArr[idx].setDir(1);
    }

    public void jump(int idx){
        characterInfoArr[idx].setJump(true);
    }

    public long tick(){
        long now = System.currentTimeMillis();
        long result = now - time;
        time = now;

        for (UserGameInfo userGameInfo:userGameInfoList){
            for (Map.Entry<GameUserState,Long> entry:userGameInfo.getStates().entrySet()){
                long leftTime = entry.getValue()-result;
                if (leftTime>0){
                    userGameInfo.getStates().put(entry.getKey(), leftTime);
                } else {
                    userGameInfo.getStates().remove(entry.getKey());
                }
            }
        }
        return result;
    }

    public void setSecond(int second){
        time = System.currentTimeMillis();
        targetTime = time+(long)second*1000;
        this.second = second;
    }

    public boolean checkSecond(){
        int newSecond = (int)Math.ceil((targetTime-time)/1000f);
        if (newSecond<second){
            second = newSecond;
            return true;
        }
        return false;
    }

    public void putStart(){
        for (int i=0; i<users.size(); i++){
            buffer[i].put(SendBinaryMessageType.START.getValue());
        }
    }

    public void putSetCharacter(){
        for (UserGameInfo userGameInfo: userGameInfoList){
            buffer[userGameInfo.getPlayerNum()]
                    .put(userGameInfo.getPlayerNum())
                    .put(SendBinaryMessageType.SET_CHARACTER.getValue())
                    .put(userGameInfo.getCharacterNum());

        }
    }

    public void putTime(){
        for (int i=0; i< users.size(); i++){
            buffer[i].put(SendBinaryMessageType.TIME.getValue())
                    .put((byte) (Math.max(second, 0)));
        }
    }

    public void putCountDown() {
        for (int i=0; i< users.size(); i++){
            buffer[i].put(SendBinaryMessageType.COUNT_DOWN.getValue())
                    .put((byte) (Math.max(second, 0)));
        }
    }
    public void putEnd() {
        for (int i=0; i<users.size(); i++){
            buffer[i].put(SendBinaryMessageType.END.getValue());
        }
    }
    public void putPhysicsState() {
        for (int i = 0; i < users.size(); i++) {
//            System.out.println(buffer[i].position());
            buffer[i].put(SendBinaryMessageType.PHYSICS_STATE.getValue())
                    .put((byte) characterInfoArr.length);
            for (CharacterInfo cInfo:characterInfoArr){
                buffer[i].putFloat(cInfo.getX());
                buffer[i].putFloat(cInfo.getY());
                buffer[i].putFloat(cInfo.getVelX()*cInfo.getDir());
                buffer[i].putFloat(cInfo.getVelY());
            }
        }
    }

    public void applyStep(){ //변경
        if (stepList.isEmpty()) return;
        for (int i=0; i<users.size(); i++){
            byte characterNum = stepList.get(i)[2];
            effectList.add(new Effect(EffectType.STEP,
                    characterInfoArr[characterNum].getX(),
                    characterInfoArr[characterNum].getY()+CHARACTER_SIZE/2f));
            userGameInfoList.get(i).getUserPlayInfo()
                    .setStep(userGameInfoList.get(i).getUserPlayInfo().getStep()+1);
        }
        stepList.clear();
    }
    public void putScore(){
        for (int i=0; i<users.size(); i++){
            buffer[i].put(SendBinaryMessageType.SCORE.getValue()).put((byte) users.size());
            for (UserGameInfo user:userGameInfoList){
                buffer[i].put(user.getScore());
            }
        }
    }

    public void putEffect(){
        if (effectList.isEmpty()) return;
        for (int i=0; i<users.size(); i++){
            buffer[i].put(SendBinaryMessageType.EFFECT.getValue())
                    .put((byte) effectList.size());
            for (Effect effect:effectList){
                buffer[i].put(effectList.get(i).effectType.getValue())
                        .putFloat(effectList.get(i).getX())
                        .putFloat(effectList.get(i).getY());
            }
        }
    }

    public void putSkin(){
        for (UserGameInfo userGameInfo:userGameInfoList){
            if (userGameInfo.getStates().containsKey(GameUserState.DISPLAY_SKIN))
                displaySkinList.add(new byte[]{userGameInfo.getPlayerNum(),userGameInfo.getCharacterNum()});
        }
        for (int i=0; i<userGameInfoList.size(); i++){
            buffer[i].put(SendBinaryMessageType.SKIN.getValue())
                    .put((byte) displaySkinList.size());
            for (byte[] bytes : displaySkinList) {
                buffer[i].put(bytes);
            }
        }
        displaySkinList.clear();
    }
    public void putTestMap(){
        for (int i=0; i< users.size(); i++){
            buffer[i].put(SendBinaryMessageType.TEST_MAP.getValue())
                    .put((byte) mapInfo.getFloorList().size());
            for (int[] arr: mapInfo.getFloorList()){
                buffer[i].put((byte) arr[0]).put((byte) arr[1]).put((byte) arr[2]);
            }
        }
    }

    //세션과 캐릭터를 매핑한다.
    public void insertSession(WebSocketSession session){
        byte num = 0;
        while (characterInfoArr[num].getUserGameInfo()==null) num++;
        UserGameInfo user = new UserGameInfo(session, (byte) users.size(), num);
        users.put(session, user);
        characterInfoArr[num].setUserGameInfo(user);
    }

    //사용 안하나?
    public void delSession(WebSocketSession session){
        users.remove(session);
    }

    public void goToNextCycle(){
        gameCycle = gameCycle.next();
    }

    public void sendBuffer(){
        for (Map.Entry<WebSocketSession,UserGameInfo> entry: getUsers().entrySet()){
            int bufferNum = entry.getValue().getPlayerNum();
            try {
                SynchronizedSend.binarySend(entry.getKey(), buffer[bufferNum]);
            } catch (Exception e){
                buffer[bufferNum].clear();
//                e.printStackTrace();
            }
        }
    }
}
