package com.ssafy.backend.game.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GameRoomDto {

    public int mapId;
    public boolean[][] floor;
    public List<String> skins;
}
