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
@Document(collection = "achievement")
public class Achievement {
    @Id
    private int id;
    //업적 이름
    private String name; 
    //업적 보상(칭호, 스킨)
    private String reward; 

}
