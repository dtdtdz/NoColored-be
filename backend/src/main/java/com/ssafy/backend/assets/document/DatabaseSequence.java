package com.ssafy.backend.assets.document;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document
public class DatabaseSequence {
    @Id
    private String id;
    private long seq;
}
