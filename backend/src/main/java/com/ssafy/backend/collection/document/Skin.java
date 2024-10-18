package com.ssafy.backend.collection.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "skin")
public class Skin {

    @Id
    private int id;
    // 스킨 이름
    private String name;
    // S3 링크
    private String link;
}
