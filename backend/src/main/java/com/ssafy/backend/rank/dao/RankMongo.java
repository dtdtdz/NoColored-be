package com.ssafy.backend.rank.dao;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "userrank")
@Builder
public class RankMongo {
    @Id
    private String userCode;

    private Integer rating;
}
