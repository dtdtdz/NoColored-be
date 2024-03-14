package com.ssafy.backend.game.domain;


import lombok.Getter;

import java.util.LinkedList;
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
