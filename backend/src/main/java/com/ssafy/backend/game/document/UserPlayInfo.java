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
    int step;
    int itemCount;
    LocalDateTime startDate;
    LocalDateTime endTime;
    String gameType;
    List<String[]> users; //code, nickname
    int againCount;
    int invincibleCount;
    int ninjaCount;
    int lightPullCount;
    int lightUPonceCount;

}
