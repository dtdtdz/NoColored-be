package com.ssafy.backend.websocket.domain;


import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Getter
public class RoomInfo {
    UUID roomId;

    String master;
    //1p가 방장?
    LinkedList<String> UserList;
    RoomInfo(String nickname){
        UserList = new LinkedList<>();
        UserList.add(nickname);
        roomId = UUID.randomUUID();
        master = nickname;
    }
}
