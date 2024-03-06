package com.ssafy.backend.websocket.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ssafy.backend.websocket.dao.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Service
public class TextMessageServiceImpl implements TextMessageService{

    @Autowired
    SessionRepository sessionRepository;
    
    private final static ObjectMapper mapper = new ObjectMapper();
    
    // 로그인 처리하기
    @Override
    public String textMessageProcessing(WebSocketSession session, TextMessage message) throws IOException {

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
    }
}
