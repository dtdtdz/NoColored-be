package com.ssafy.backend.assets;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.ByteBuffer;
/**
 * 웹소켓 세션을 동기화하여 안전하게 보내기 위한 클래스
 * 이진데이터와 문자열데이터를 따로 처리
 */
public class SynchronizedSend {
    public static void binarySend(WebSocketSession session, ByteBuffer buffer){
        long dt1 = System.currentTimeMillis();
        if (session==null) {
            System.out.println("Can't find session.");
            buffer.clear();
            return;
        } else if (!session.isOpen()){
//            System.out.println("Session isn't open.");
            buffer.clear();
            return;
        }
        synchronized (session){

            try {
                buffer.flip();
                session.sendMessage(new BinaryMessage(buffer));
                buffer.clear();
            } catch (Exception e) {
                buffer.clear();
                throw new RuntimeException("Fail to send.");
            }

        }
        long dt2 = System.currentTimeMillis();
        if (dt2-dt1>50) System.out.println("send"+(dt2-dt1));
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Wrapper wrapper = new Wrapper();

    public static void textSend(WebSocketSession session, String action, Object data){
        if (session==null) {
            System.out.println("Can't find session.");
            return;
        } else if (!session.isOpen()){
//            System.out.println("Session isn't open.");
            return;
        }
        try {
        synchronized (session){
            wrapper.setAction(action);
            wrapper.setData(data);
            String messageContent = objectMapper.writeValueAsString(wrapper);
            // 메시지 내용 출력
            System.out.println("Sending message: " + messageContent);
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(wrapper)));
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    @NoArgsConstructor
    @Setter
    @Getter
    private static class Wrapper {
        private String action;
        private Object data;

        public Wrapper(String type, Object data) {
            this.action = type;
            this.data = data;
        }
    }
}
