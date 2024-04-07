package com.ssafy.backend.game.domain;

import com.ssafy.backend.assets.SynchronizedSend;
import com.ssafy.backend.game.document.UserPlayInfo;
import com.ssafy.backend.game.type.EffectType;
import com.ssafy.backend.game.type.GameCycle;
import com.ssafy.backend.game.type.GameItemType;
import com.ssafy.backend.game.type.GameCharacterState;
import com.ssafy.backend.play.domain.RoomInfo;
import com.ssafy.backend.websocket.domain.SendBinaryMessageType;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.*;


@Getter
@AllArgsConstructor
public class GameInfo {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private long targetTime;
    private long time;
    private long itemTime;
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
//    private GameItemType type;
    private byte stepOrder;
    private GameItemType curItem;
    private byte[] useItem;
    private int idxItemY;

    //이것들 리팩토링 고려
    public static final int CHARACTER_SIZE = 27;
    public static final float DEFAULT_VEL_X = 120;
    public static final int MAX_PLAYER = 4;
    public static final float GRAVITY = 500;
    public static final int BLOCK_SIZE = 18;
    public static final int MAP_HEIGHT = 19;
    public static final int MAP_WIDTH = 27;
    public static final int WALL_WIDTH = 3;
    public static final int GAME_TIME = 40;
    public static final int CHARACTER_NUM = 10;
    public static final int JUMP_VEL_Y = -300;
    public static final int STEP_VEL_Y = -200;
    public static final int ITEM_CREATE_INTERVAL = 15;
    public static final int ITEM_REMOVE_INTERVAL = 5;
    public static final int ITEM_SIZE = 32;
    public static final float ITEM_X = (WALL_WIDTH+MAP_WIDTH/2f)*BLOCK_SIZE;
    public static final float[] ITEM_Y_ARR = new float[] {7*BLOCK_SIZE, 11*BLOCK_SIZE, 15*BLOCK_SIZE};
    public static final int MOVE_CLONE_PROP = 250;


    private ByteBuffer[] buffer = new ByteBuffer[4];

    private GameCycle gameCycle;
    public GameInfo(List<UserAccessInfo> userList, RoomInfo room) {
        for (int i=0; i<buffer.length; i++){
            buffer[i] = ByteBuffer.allocate(2048);
        }
        users = new LinkedHashMap<>();
        userGameInfoList = new LinkedList<>();
        startDate = LocalDateTime.now();
        setSecond(5);
        setItemTime();
        floor = new boolean[MAP_WIDTH][MAP_HEIGHT];
        characterInfoArr = new CharacterInfo[CHARACTER_NUM];
        random = new Random();
        gameCycle = GameCycle.CREATE;
        stepOrder = 1;
        if (room!=null && room.getRoomDto().getMapId()>0 && room.getRoomDto().getMapId()<=MapInfo.MAPLIST_SIZE){
            mapInfo = new MapInfo(room.getRoomDto().getMapId());
        } else {
            mapInfo = new MapInfo((random.nextInt(MapInfo.MAPLIST_SIZE))+1);
        }

        stepList = new ArrayList<>();
        effectList = new LinkedList<>();
        displaySkinList = new LinkedList<>();
        curItem = GameItemType.NO_ITEM;
        useItem = null;
        //캐릭터 위치 랜덤배치
        List<int[]> floorPos = new LinkedList<>();
        //유저 캐릭터 번호 랜덤 매핑
        List<Byte> idxs = new LinkedList<>();
        for (int[] arr:mapInfo.getFloorList()){
            for (int i=0; i<arr[2]; i++){
                floor[arr[0]+i-WALL_WIDTH][arr[1]] = true;
                floorPos.add(new int[]{arr[0]+i,arr[1]});
            }
        }

        for (byte i=0; i<CHARACTER_NUM; i++){
            idxs.add(i);
        }
        Collections.shuffle(idxs);
        Collections.shuffle(floorPos);
//        for (int i=0; i<20; i++){
//            floorPos.remove(0);
//        }

        for (byte i=0; i<userList.size(); i++){
            UserGameInfo userGameInfo = new UserGameInfo(userList.get(i).getSession(),
                    idxs.get(i),i,(room==null)?"friendly":"ranking");
            CharacterInfo characterInfo = new CharacterInfo(
                    (floorPos.get(i)[0]+0.5f)*BLOCK_SIZE,
                    floorPos.get(i)[1]*BLOCK_SIZE-CHARACTER_SIZE/2f,
                    (int) ((random.nextInt(2)-0.5f)*2),
                    userGameInfo
            );

            characterInfoArr[idxs.get(i)] = characterInfo;
            users.put(userList.get(i), userGameInfo);
            userGameInfoList.add(userGameInfo);
        }

        for (int i=userList.size(); i<CHARACTER_NUM ; i++){
            CharacterInfo characterInfo = new CharacterInfo(
                    (floorPos.get(i)[0]+0.5f)*BLOCK_SIZE,
                    floorPos.get(i)[1]*BLOCK_SIZE-CHARACTER_SIZE/2f,
                    (int) ((random.nextInt(2)-0.5f)*2),
                    null
            );

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
        long dt = now - time;
        time = now;

        itemManageProcess();
        applyTimeAtState(dt);

        return dt;
    }

    public void setSecond(int second){
        time = System.currentTimeMillis();
        targetTime = time+(long)second*1000;
        this.second = second;
    }

    public void setItemTime(){
        itemTime = time + 1000L*ITEM_CREATE_INTERVAL;
    }

    private void itemManageProcess(){
        if (curItem==null) return;
        if (curItem.equals(GameItemType.NO_ITEM)){
            if (itemTime<time){
                idxItemY = random.nextInt(ITEM_Y_ARR.length);
                while (!curItem.equals(GameItemType.NO_ITEM) && !curItem.equals(GameItemType.REBEL)){
                    curItem = GameItemType.valueOf((byte)(random.nextInt(GameItemType.size()-1)+1));
                }
//                curItem = GameItemType.STOP_NPC;
                setItemTime();
            }
        } else if (itemTime-(ITEM_CREATE_INTERVAL-ITEM_REMOVE_INTERVAL)*1000L<time){
            curItem = GameItemType.NO_ITEM;
            effectList.add(new Effect(EffectType.ITEM_TIME_OUT, ITEM_X,
                    ITEM_Y_ARR[idxItemY]));
        }

    }

    public void itemUse(CharacterInfo cInfo){
        float interval = (ITEM_SIZE+CHARACTER_SIZE)/2f;
        if (cInfo.getUserGameInfo()==null) return;
        if ((Math.abs(cInfo.getX()-ITEM_X)<interval && (!curItem.equals(GameItemType.NO_ITEM)))
                && (Math.abs(cInfo.getY()- ITEM_Y_ARR[idxItemY])<interval)){
            effectList.add(new Effect(EffectType.ITEM_USE, ITEM_X, ITEM_Y_ARR[idxItemY]));
            useItem = new byte[]{curItem.getValue(), cInfo.getUserGameInfo().getPlayerNum()};
            curItem = GameItemType.NO_ITEM;
        }
    }

    public void applyItem(){
        if (useItem==null) return;
        //itemUse에서 useItem에 할당할경우 duration<=0이고 필요하다면 지정
        UserPlayInfo userPlayInfo = userGameInfoList.get(useItem[1]).getUserPlayInfo();
        userPlayInfo.setItemCount(userPlayInfo.getItemCount()+1);
        userPlayInfo.getItemCountMap().put(GameItemType.valueOf(useItem[0]).name().toLowerCase(),
                userPlayInfo.getItemCountMap().get(GameItemType.valueOf(useItem[0]).name().toLowerCase())+1);
        switch (GameItemType.valueOf(useItem[0])){
            case LIGHT_U_PALL -> {
                for (UserGameInfo userGameInfo: userGameInfoList){
                    if (userGameInfo.getPlayerNum()==useItem[1]) continue;
                    CharacterInfo characterInfo = characterInfoArr[userGameInfo.getCharacterNum()];
                    applyState(characterInfo, GameCharacterState.DISPLAY_SKIN, 3000L);
                    effectList.add(new Effect(EffectType.SKIN_APPEAR,
                            characterInfo.getX(), characterInfo.getY()));
                }

            }
            case STOP_NPC -> {
                for (CharacterInfo characterInfo: characterInfoArr){
                    if(characterInfo.getUserGameInfo()==null){
                        applyState(characterInfo, GameCharacterState.STOP, 3000L);
                        effectList.add(new Effect(EffectType.ITEM_STOP, characterInfo.getX(), characterInfo.getY()));
                    }
                }
            }
            case RANDOM_BOX -> {
                int num = random.nextInt(users.size());
                CharacterInfo characterInfo = characterInfoArr[userGameInfoList.get(num).getCharacterNum()];
                applyState(characterInfo, GameCharacterState.DISPLAY_SKIN, 3000L);
                effectList.add(new Effect(EffectType.SKIN_APPEAR, characterInfo.getX(), characterInfo.getY()));

            }
            case REBEL -> {
                effectList.add(new Effect(EffectType.REBEL, ITEM_X, ITEM_Y_ARR[idxItemY]));
            }
            case STOP_PLAYER -> {
                for (UserGameInfo userGameInfo: userGameInfoList) {
                    if (userGameInfo.getPlayerNum() == useItem[1]) continue;
                    CharacterInfo characterInfo = characterInfoArr[userGameInfo.getCharacterNum()];
                    applyState(characterInfo, GameCharacterState.STOP, 3000L);
                    effectList.add(new Effect(EffectType.ITEM_STOP, characterInfo.getX(), characterInfo.getY()));
                }
            }
        }

        useItem = null;
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
            if (random.nextInt(MOVE_CLONE_PROP) < 1){
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
    public void putItem(){
        for (int i=0; i<users.size(); i++){
            buffer[i].put(SendBinaryMessageType.ITEM.getValue())
                    .put(curItem.getValue())
                    .putFloat(ITEM_X).putFloat(ITEM_Y_ARR[idxItemY]);
        }
    }
    public void putEnd() {
        for (int i=0; i<users.size(); i++){
            buffer[i].put(SendBinaryMessageType.END.getValue());
        }
    }
    public void putPhysicsState() {
        for (int i = 0; i < users.size(); i++) {
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
            CharacterInfo char1 = characterInfoArr[bytes[2]];
            CharacterInfo char2 = characterInfoArr[bytes[3]];;
            if (user1.getStepOrder()==null){
                user1.setStepOrder(stepOrder++);
            }

            effectList.add(new Effect(EffectType.SKIN_APPEAR,
                    characterInfoArr[characterNum].getX(),
                    characterInfoArr[characterNum].getY()));
            effectList.add(new Effect(EffectType.SKIN_APPEAR,
                    characterInfoArr[characterNum].getX(),
                    characterInfoArr[characterNum].getY()));
            user1.getUserPlayInfo()
                    .setStep(user1.getUserPlayInfo().getStep() + 1);
            user2.getUserPlayInfo()
                    .setStepped(user2.getUserPlayInfo().getStepped()+1);
            applyState(char1, GameCharacterState.DISPLAY_SKIN, 4000L);
            applyState(char2, GameCharacterState.DISPLAY_SKIN, 2000L);
            applyState(char2, GameCharacterState.STOP, 2000L);
            applyState(char2, GameCharacterState.STEPED, 2000L);

        }
        stepList.clear();
    }
    public void applyState(CharacterInfo characterInfo, GameCharacterState state, long time){
        characterInfo.getStates().compute(state, (key, currentValue) ->
                Math.max(currentValue == null ? 0L : currentValue, time));
        if (state.equals(GameCharacterState.STOP) && time>0){
            characterInfo.setVelX(0);
        }
    }

    public void applyTimeAtState(long dt){
        for (CharacterInfo characterInfo : characterInfoArr) {
            Iterator<Map.Entry<GameCharacterState, Long>> it = characterInfo.getStates().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<GameCharacterState, Long> entry = it.next();
                long time = entry.getValue() - dt;
                if (time < 0) {
                    it.remove(); // 안전하게 원소 제거
                    if (entry.getKey().equals(GameCharacterState.STOP)) {
                        characterInfo.setVelX(DEFAULT_VEL_X);
                    } else if (entry.getKey().equals(GameCharacterState.STEPED)){
                        int val;
                        do {
                            val = random.nextInt(CHARACTER_NUM);
                        } while (characterInfoArr[val].getUserGameInfo()!=null);

                        float tmp = characterInfo.getX();
                        characterInfo.setX(characterInfoArr[val].getX());
                        characterInfoArr[val].setX(tmp);

                        tmp = characterInfo.getY();
                        characterInfo.setY(characterInfoArr[val].getY());
                        characterInfoArr[val].setY(tmp);

                        characterInfoArr[characterInfo.getUserGameInfo().getCharacterNum()] = characterInfoArr[val];
                        characterInfoArr[val] = characterInfo;
                        characterInfo.getUserGameInfo().setCharacterNum((byte) val);

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
        effectList.clear();
    }

    public void putSkin(){

        for (UserGameInfo userGameInfo:userGameInfoList){
            if (characterInfoArr[userGameInfo.getCharacterNum()]
                    .getStates().containsKey(GameCharacterState.DISPLAY_SKIN))
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
        UserGameInfo userInfo = new UserGameInfo(user.getSession(), (byte) users.size(), num, (room==null)?"friendly":"ranking");
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

    public void setEndDate(){
        endDate = LocalDateTime.now();
    }
}
