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

public class SynchronizedSend {
    public static void binarySend(WebSocketSession session, ByteBuffer buffer){
        if (session==null) {
            System.out.println("세션 없음");
            return;
        }
        synchronized (session){
            try {
                buffer.flip();
                session.sendMessage(new BinaryMessage(buffer));
                buffer.clear();
            } catch (Exception e) {
                buffer.clear();
                throw new RuntimeException("전송 실패");
            }

        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Wrapper wrapper = new Wrapper();

    public static void textSend(WebSocketSession session, String action, Object data){
        if (session==null) {
            System.out.println("세션 없음");
            return;
        }
        try {
            wrapper.setAction(action);
            wrapper.setData(data);
            synchronized (session){
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
        // getter, setter 생략
    }
}
