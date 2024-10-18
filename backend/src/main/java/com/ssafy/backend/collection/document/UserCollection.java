package com.ssafy.backend.collection.document;

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
    // 가지고 있는 스킨 리스트
    private List<Integer> skinIds;
    // 가지고 있는 칭호 리스트 
    private List<Integer> labelIds; 
    // 가지고 있는 업적 리스트
    private List<Integer> achievementIds; 
}
