package com.ssafy.backend.game.domain;

import com.ssafy.backend.assets.SynchronizedSend;
import com.ssafy.backend.play.domain.RoomInfo;
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
    private Map<UserAccessInfo, UserGameInfo> users;
    private List<UserGameInfo> userGameInfoList;
    private MapInfo mapInfo;
    private CharacterInfo[] characterInfoArr;
    private boolean[][] floor;
    private Random random;
    private GameRoomDto gameRoomDto;
    private RoomInfo room;

    private List<byte[]> stepList;
    private List<Effect> effectList;
    private List<byte[]> displaySkinList;
    private GameItemType type;
    private byte stepOrder;
    //이것들 리팩토링 고려
    public static final int CHARACTER_SIZE = 27;
    public static final float DEFAULT_SPEED = 160;
    public static final int MAX_PLAYER = 4;
    public static final float GRAVITY = 500;
    public static final int BLOCK_SIZE = 18;
    public static final int MAP_HEIGHT = 19;
    public static final int MAP_WIDTH = 27;
    public static final int WALL_WIDTH = 3;
    public static final int DEFAULT_TIME = 60;
    public static final int CHARACTER_NUM = 10;
    public static final int JUMP_VEL_Y = -300;
    public static final int STEP_VEL_Y = -200;

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
    public GameInfo(List<UserAccessInfo> userList, RoomInfo room) {
        users = new LinkedHashMap<>();
        userGameInfoList = new LinkedList<>();
        startDate = LocalDateTime.now();
        setSecond(3);
        floor = new boolean[MAP_WIDTH][MAP_HEIGHT];
        characterInfoArr = new CharacterInfo[CHARACTER_NUM];
        random = new Random();
        gameCycle = GameCycle.CREATE;
        stepOrder = 1;
        if (room!=null && room.getRoomDto().getMapId()>0 && room.getRoomDto().getMapId()<=2){
            mapInfo = new MapInfo(room.getRoomDto().getMapId()-1);
        } else {
            mapInfo = new MapInfo(random.nextInt(2));
        }

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
            users.put(userList.get(i), userGameInfo);
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
        System.out.println(skins+" "+userList.size());
        gameRoomDto.setSkins(skins);
        gameRoomDto.setFloorList(mapInfo.getFloorList());
        gameRoomDto.setMapId(mapInfo.getMapId());
        this.room = room;
    }
    public GameInfo(List<UserAccessInfo> userList){
        this(userList, null);
    }

    public boolean isAllReady(){
        for (UserGameInfo userGameInfo:userGameInfoList){
            if (!userGameInfo.isAccess()) return false;
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

        applyTimeAtState(result);
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

    public void setCharacterDirection() {
        for (CharacterInfo characterInfo:characterInfoArr){
            if (characterInfo.getUserGameInfo()!=null) continue;
            if (random.nextInt(300) < 1){
                characterInfo.setDir(-characterInfo.getDir());
            }
        }
    }
    public void putStart(){
        for (int i=0; i<users.size(); i++){
            buffer[i].put(SendBinaryMessageType.START.getValue());
        }
    }

    public void putCharacterMapping(){
        for (UserGameInfo userGameInfo: userGameInfoList){
            buffer[userGameInfo.getPlayerNum()]
                    .put(SendBinaryMessageType.CHARACTER_MAPPING.getValue())
                    .put(userGameInfo.getPlayerNum())
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
        for (byte[] bytes : stepList) {
            byte characterNum = bytes[2];
            UserGameInfo user1 = userGameInfoList.get(bytes[0]);
            UserGameInfo user2 = userGameInfoList.get(bytes[1]);

            if (user1.getStepOrder()==null){
                user1.setStepOrder(stepOrder++);
            }

            effectList.add(new Effect(EffectType.STEP,
                    characterInfoArr[characterNum].getX(),
                    characterInfoArr[characterNum].getY() + CHARACTER_SIZE / 2f));
            user1.getUserPlayInfo()
                    .setStep(user1.getUserPlayInfo().getStep() + 1);
            applyState(user1, GameUserState.DISPLAY_SKIN, 4000L);
            applyState(user2, GameUserState.DISPLAY_SKIN, 2000L);
            applyState(user2, GameUserState.STEPED, 2000L);
            applyState(user2, GameUserState.STOP, 2000L);
            characterInfoArr[user2.getCharacterNum()].setVelX(0);
        }
        stepList.clear();
    }
    public void applyState(UserGameInfo userGameInfo, GameUserState state, long time){
        userGameInfo.getStates().compute(state, (key, currentValue) ->
                Math.max(currentValue == null ? 0L : currentValue, time));
    }

    public void applyTimeAtState(long dt){
        for (UserGameInfo userGameInfo : userGameInfoList) {
            Iterator<Map.Entry<GameUserState, Long>> it = userGameInfo.getStates().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<GameUserState, Long> entry = it.next();
                long time = entry.getValue() - dt;
                if (time < 0) {
                    it.remove(); // 안전하게 원소 제거
                    if (entry.getKey().equals(GameUserState.STOP)) {
                        characterInfoArr[userGameInfo.getCharacterNum()].setVelX(DEFAULT_SPEED);
                    } else if (entry.getKey().equals(GameUserState.STEPED)){
                        int val;
                        do {
                            val = random.nextInt(CHARACTER_NUM);
                        } while (characterInfoArr[val].getUserGameInfo()!=null);
                        characterInfoArr[val].setUserGameInfo(userGameInfo);
                        characterInfoArr[userGameInfo.getCharacterNum()].setUserGameInfo(null);
                        userGameInfo.setCharacterNum((byte) val);
                    }
                } else {
                    entry.setValue(time); // 값을 직접 수정하는 것은 안전함
                }
            }
        }
    }

    public void putScore(){
        for (int i=0; i<userGameInfoList.size(); i++){
            buffer[i].put(SendBinaryMessageType.SCORE.getValue()).put((byte) userGameInfoList.size());
            for (UserGameInfo user:userGameInfoList){
                buffer[i].put(user.getScore());
            }
        }

//        StringBuilder str = new StringBuilder();
//        for (UserGameInfo user:userGameInfoList){
//            str.append(user.getScore()).append(" ");
//        }
//        System.out.println(str);
    }

    public void putEffect(){
        if (effectList.isEmpty()) return;
        for (int i=0; i<users.size(); i++){
            buffer[i].put(SendBinaryMessageType.EFFECT.getValue())
                    .put((byte) effectList.size());
            for (Effect effect:effectList){
                buffer[i].put(effect.effectType.getValue())
                        .putFloat(effect.getX())
                        .putFloat(effect.getY());
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
    public void insertUser(UserAccessInfo user){
        byte num = 0;
        while (characterInfoArr[num].getUserGameInfo()==null) num++;
        UserGameInfo userInfo = new UserGameInfo(user.getSession(), (byte) users.size(), num);
        users.put(user, userInfo);
        characterInfoArr[num].setUserGameInfo(userInfo);
    }

    //사용 안하나?
//    public void delSession(WebSocketSession session){
//        users.remove(session);
//    }

    public void goToNextCycle(){
        gameCycle = gameCycle.next();
    }

    public void sendBuffer(){
        for (int i=0; i<userGameInfoList.size(); i++){
            try {
                SynchronizedSend.binarySend(userGameInfoList.get(i).getWebSocketSession(),
                        buffer[i]);
            } catch (Exception e){
                buffer[i].clear();
//                e.printStackTrace();
            }
        }

    }
}
