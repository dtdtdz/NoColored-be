package com.ssafy.backend.config;

import com.ssafy.backend.websocket.dao.SessionRepository;
import com.ssafy.backend.websocket.handler.MyWebSocketHandler;
import com.ssafy.backend.websocket.service.BinaryMessageService;
import com.ssafy.backend.websocket.service.TextMessageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final BinaryMessageService binaryMessageService;
    private final TextMessageService textMessageService;
    private final SessionRepository sessionRepository;
    private final ScheduledExecutorService authScheduledExecutorService;
    // 생성자를 통해 필요한 서비스들을 주입받음
    public WebSocketConfig(BinaryMessageService binaryMessageService,
                           TextMessageService textMessageService,
                           SessionRepository sessionRepository,
                           @Qualifier("authScheduledExecutorService") ScheduledExecutorService authScheduledExecutorService) {
        this.binaryMessageService = binaryMessageService;
        this.textMessageService = textMessageService;
        this.sessionRepository = sessionRepository;
        this.authScheduledExecutorService = authScheduledExecutorService;
    }
    @Bean
    public MyWebSocketHandler myWebSocketHandler(){
        return new MyWebSocketHandler(binaryMessageService, textMessageService, sessionRepository, authScheduledExecutorService);
    }

    // 요청경로: /game
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myWebSocketHandler(), "/game").setAllowedOrigins("*");//변경해야함
    }


}