package com.ssafy.backend.game.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

//@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPlayInfo {
    int rank;
    long step;
    long itemCount;
    LocalDateTime startDate;
    LocalDateTime endTime;
    String gameType;
    List<String[]> users; //code, nickname
    long againCount;
    long invincibleCount;
    long ninjaCount;
    long lightUPallCount;
    long lightUPonceCount;
    long fireworksCount;
    long stopNPCCount;
    long blackoutCount;
    long randomBoxCount;
    long rebelCount;
}
