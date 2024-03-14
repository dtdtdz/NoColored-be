package com.ssafy.backend.config;

import com.ssafy.backend.websocket.service.MessageProcessService;
import com.ssafy.backend.websocket.util.SessionCollection;
import com.ssafy.backend.websocket.handler.MyWebSocketHandler;
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

    private final MessageProcessService messageProcessService;
    private final SessionCollection sessionRepository;
    private final ScheduledExecutorService authScheduledExecutorService;
    // 생성자를 통해 필요한 서비스들을 주입받음
    public WebSocketConfig(MessageProcessService messageProcessService,
                           SessionCollection sessionRepository,
                           @Qualifier("authScheduledExecutorService") ScheduledExecutorService authScheduledExecutorService) {
        this.messageProcessService = messageProcessService;
        this.sessionRepository = sessionRepository;
        this.authScheduledExecutorService = authScheduledExecutorService;
    }
    @Bean
    public MyWebSocketHandler myWebSocketHandler(){
        return new MyWebSocketHandler(messageProcessService, sessionRepository, authScheduledExecutorService);
    }

    // 요청경로: /game
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myWebSocketHandler(), "/game").setAllowedOrigins("*");//변경해야함
    }


}