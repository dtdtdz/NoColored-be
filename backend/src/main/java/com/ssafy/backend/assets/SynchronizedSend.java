package com.ssafy.backend.assets;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SynchronizedSend {
    public static void send(WebSocketSession session, Object object){
        if (session==null) throw new RuntimeException("세션 없음");
        synchronized (session){
            try {
                if (object instanceof ByteBuffer buffer){
                    buffer.flip();
                    session.sendMessage(new BinaryMessage(buffer));
                    buffer.clear();
                } else {
                    session.sendMessage(new TextMessage(SendTextMessageWrapper.wrapAndConvertToJson(object)));
                }
            } catch (IOException e) {
                if (object instanceof ByteBuffer buffer) buffer.clear();
                e.printStackTrace();
//                throw new IOException(e);
            }
        }
    }
}
