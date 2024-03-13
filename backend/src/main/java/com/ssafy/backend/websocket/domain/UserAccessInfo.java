package com.ssafy.backend.websocket.domain;

import com.ssafy.backend.user.entity.UserInfo;
import com.ssafy.backend.user.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAccessInfo {
    private WebSocketSession session;
    private String token;
    private UserProfile userProfile;
}
