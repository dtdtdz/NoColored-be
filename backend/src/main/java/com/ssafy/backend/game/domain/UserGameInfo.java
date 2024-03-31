package com.ssafy.backend.game.domain;


import com.ssafy.backend.game.document.UserPlayInfo;
import com.ssafy.backend.game.type.GameUserState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserGameInfo {
    private WebSocketSession webSocketSession;
    private byte characterNum;
    private byte playerNum;
    private boolean isAccess;
    private byte score;
    private Byte stepOrder;

    private UserPlayInfo userPlayInfo;
    private Map<GameUserState, Long> states;//milliSecond

    public UserGameInfo(WebSocketSession webSocketSession,
                        byte characterNum,
                        byte playerNum){
        this.webSocketSession = webSocketSession;
        this.characterNum = characterNum;
        this.playerNum = playerNum;
        isAccess = false;
        score = 0;
        userPlayInfo = new UserPlayInfo();
        states = new LinkedHashMap<>();
        stepOrder = null;
    }

}
