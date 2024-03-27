package com.ssafy.backend.collection.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TitleDto {
    private int id;
    private String name;
    private String condition;
    private boolean isOwn;
    private boolean isEquipped;
}
