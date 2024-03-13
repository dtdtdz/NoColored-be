package com.ssafy.backend.websocket.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ssafy.backend.websocket.dao.SessionRepository;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.dao.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.ssafy.backend.user.util.RandomNickname.makeNickname;

@Service
public class TextMessageServiceImpl implements TextMessageService{


    private final SessionRepository sessionRepository;
    private final UserProfileRepository userProfileRepository;
    private final ObjectMapper mapper;
    private final Map<String, Consumer<JsonNode>> actionHandlers;

    public TextMessageServiceImpl(SessionRepository sessionRepository,
                                  UserProfileRepository userProfileRepository){
        this.sessionRepository = sessionRepository;
        this.userProfileRepository = userProfileRepository;
        mapper = new ObjectMapper();
        actionHandlers = new HashMap<>();
        actionHandlers.put("token", this::handleToken);
    }

    static {

    }

    private void handleToken(JsonNode node){
        System.out.println(node);
    };
    // 로그인 처리하기
    @Override
    public String textMessageProcessing(WebSocketSession session, TextMessage message) throws IOException {

        // message에서 action을 가져온다
        JsonNode jsonNode = mapper.readTree(message.getPayload());
        String action = jsonNode.get("action").asText();

        switch (action){

        }

        ObjectNode responseNode = mapper.createObjectNode();
        responseNode.put("action", action);

        // 로그인인 경우
        if ("UserLogin".equals(action)) {
            // 사용자 ID와 비밀번호 추출
            String userId = jsonNode.get("data").get("userId").asText();
            String userPwd = jsonNode.get("data").get("userPwd").asText();

            responseNode.put("status", "success");

        } else if ("GuestLogin".equals(action)) { // 게스트 로그인+회원가입 처리 로직
            // UserInfo 객체 생성
            UserProfile guestUser = UserProfile.builder()
                    .userNickname(makeNickname())
                    .userSkin("") // 기본 스킨 설정
                    .isGuest(true) // 게스트 사용자로 설정
                    .userExp(0L) // 경험치 초기값 설정
                    .userTitle("") // 타이틀 설정
                    .userLevel(0) // 레벨 설정
                    .build();

            // UserInfo 객체를 데이터베이스에 저장
//            userInfoRepository.save(guestUser);

            // 응답 노드에 게스트 사용자 정보 추가
            responseNode.put("status", "success");
            responseNode.put("userId", guestUser.getId().toString());
            responseNode.put("userNickname", guestUser.getUserNickname());
            responseNode.put("userSkin", guestUser.getUserSkin());
            responseNode.put("isGuest", guestUser.isGuest());
            responseNode.put("userExp", guestUser.getUserExp());
            responseNode.put("userTitle", guestUser.getUserTitle());
            responseNode.put("userLevel", guestUser.getUserLevel());

            // 웹소켓 세션에 사용자 정보 저장
            session.getAttributes().put("usrInfo", guestUser);

//            UserAccessInfo userAccessInfo = new UserAccessInfo(session, guestUser);
//            // 맵에 매핑
//            userWebsocketMap.put(session, guestUser);
//            userCodeMap.put(guestUser, session);

            // loginUserMap 순회
//            loginUserMap.forEach((session, userInfo) -> {
//                // 각 UserInfo 객체의 정보 출력
//                System.out.println("Session ID: " + session.getId());
//                System.out.println("User ID: " + userInfo.getId());
//                System.out.println("Nickname: " + userInfo.getUserNickname());
//                System.out.println("Is Guest: " + userInfo.isGuest());
//                System.out.println("User Skin: " + userInfo.getUserSkin());
//                System.out.println("User Exp: " + userInfo.getUserExp());
//                System.out.println("User Title: " + userInfo.getUserTitle());
//                System.out.println("User Level: " + userInfo.getUserLevel());
//                System.out.println("----------------------------------");
//            });



        } else {
            // 지원되지 않는 액션 처리
            responseNode.put("action", "Error");
            responseNode.put("message", "Action not supported");
        }




        // 메시지 처리 결과를 송신자(클라이언트)에게 반환
        session.sendMessage(new TextMessage(responseNode.toString()));
        return responseNode.toString();

    }
}


/*
* // 여기서 사용자 정보 검증 로직 구현 (임시 로직)
            boolean canFindUserInfo = validateUser(userId, userPwd);

            if (isValidUser) {
                // 사용자 인증이 성공한 경우, UserInfo 객체 생성 또는 조회
                // 실제 어플리케이션에서는 userId를 사용하여 DB에서 UserInfo를 조회하거나 생성해야 합니다.
                // 여기서는 단순화를 위해 임시 UserInfo 객체를 생성합니다. 실제 구현에서는 DB 조회/생성 로직 필요
                UserInfo userInfo = userInfoService.createAndSaveUserInfo("UserNickname", "defaultSkin", false, 0L, "new user", 1);
                sessionRepository.sessionUserMap.put(session, userInfo);

                // 성공 응답 설정
                responseNode.put("status", "success");
                responseNode.put("message", "Login successful.");
            } else {
                // 사용자 인증 실패 처리
                responseNode.put("status", "failure");
                responseNode.put("message", "Invalid userId or password.");
            }
*
*
* */



/*
* // message에서 action을 가져온다
        JsonNode jsonNode = mapper.readTree(message.getPayload());
        String action = jsonNode.get("action").asText();

        ObjectNode responseNode = mapper.createObjectNode();
        responseNode.put("action", action);

        if ("UserLogin".equals(action)) {
            // UserLogin 액션 처리. 요청된 메시지 그대로 클라이언트에게 반환.
            responseNode.set("data", jsonNode.get("data"));
        } else if ("GuestLogin".equals(action)) {
            // GuestLogin 액션 처리. 고정된 데이터로 응답 생성.
            ObjectNode dataNode = responseNode.putObject("data");
            dataNode.put("userId", "guest");
            dataNode.put("userPwd", "guestpwd");
        } else {
            // 지원되지 않는 액션 처리
            responseNode = mapper.createObjectNode();
            responseNode.put("action", "Error");
            responseNode.put("message", "Action not supported");
        }

        // 메시지 처리 결과를 송신자(클라이언트)에게 반환
        session.sendMessage(new TextMessage(responseNode.toString()));
        return responseNode.toString(); // 이 반환값은 서버 로그 등에서 사용될 수 있습니다.
* */



//        // 메시지 내용(JSON)을 JsonNode로 파싱
//        JsonNode jsonNode = mapper.readTree(message.getPayload());
//        String action = jsonNode.get("action").asText(); // 액션 추출
//
//        // UserLogin 액션 처리
//        if ("UserLogin".equals(action)) {
//            JsonNode dataNode = jsonNode.get("data");
//            String userId = dataNode.get("userId").asText();
//            String userPwd = dataNode.get("userPwd").asText();
//
//            // 로그인 로직 처리 (예시에서는 단순히 출력만 함)
//            System.out.println("UserLogin action received with userId: " + userId + " and password: " + userPwd);
//
//            // 처리 결과 반환 (여기서는 예시로 입력받은 내용을 그대로 반환)
//            return mapper.writeValueAsString(jsonNode);
//        }else if ("GuestLogin".equals(action)) {
//
//            // 로그인 로직 처리 (예시에서는 단순히 출력만 함)
//            System.out.println("guest입니당");
//
//            // 처리 결과 반환 (여기서는 예시로 입력받은 내용을 그대로 반환)
//            return mapper.writeValueAsString(jsonNode);
//        }
//        // 다른 액션 처리 (필요한 경우)
//        return "Action not supported";