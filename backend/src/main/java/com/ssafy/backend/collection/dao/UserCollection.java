package com.ssafy.backend.collection.dao;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "usercollection")
@Builder
public class UserCollection {
    @Id
    private String userCode;
    private List<Integer> skinIds; // 가지고 있는 스킨
    private List<Integer> titleIds; // 가지고 있는 칭호
    private List<Integer> achievementIds; // 가지고 있는 업적
}
