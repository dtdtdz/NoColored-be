package com.ssafy.backend.websocket.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ssafy.backend.websocket.dao.SessionRepository;
import com.ssafy.backend.websocket.dao.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Service
public class TextMessageServiceImpl implements TextMessageService{

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    UserInfoRepository userInfoRepository;
    
    private final static ObjectMapper mapper = new ObjectMapper();
    
    // 로그인 처리하기
    @Override
    public String textMessageProcessing(WebSocketSession session, TextMessage message) throws IOException {

        // message에서 action을 가져온다
        JsonNode jsonNode = mapper.readTree(message.getPayload());
        String action = jsonNode.get("action").asText();

        ObjectNode responseNode = mapper.createObjectNode();
        responseNode.put("action", action);

        // 로그인인 경우
        if ("UserLogin".equals(action)) {
            // 사용자 ID와 비밀번호 추출
            String userId = jsonNode.get("data").get("userId").asText();
            String userPwd = jsonNode.get("data").get("userPwd").asText();

            // 여기서 사용자 정보 검증 로직 구현 (임시 로직)
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
        } else if ("GuestLogin".equals(action)) {
            // 게스트 로그인 처리 로직...
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