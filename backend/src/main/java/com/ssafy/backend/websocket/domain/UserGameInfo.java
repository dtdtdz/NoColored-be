package com.ssafy.backend.websocket.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserGameInfo {
    WebSocketSession webSocketSession;
    int characterNum;
    int bufferNum;
}
