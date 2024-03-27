package com.ssafy.backend.game.domain;


import com.ssafy.backend.game.document.UserPlayInfo;
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
    private WebSocketSession webSocketSession;
    private byte characterNum;
    private byte playerNum;
    private boolean isAccess;
    private UserPlayInfo userPlayInfo;

    public UserGameInfo(WebSocketSession webSocketSession,
                        byte characterNum,
                        byte playerNum){
        this.webSocketSession = webSocketSession;
        this.characterNum = characterNum;
        this.playerNum = playerNum;
        isAccess = false;
        userPlayInfo = new UserPlayInfo();
    }

}
