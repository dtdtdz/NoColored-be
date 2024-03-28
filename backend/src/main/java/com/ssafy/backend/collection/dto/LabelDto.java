package com.ssafy.backend.collection.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LabelDto {
    private int id;
    private String name;
    private boolean isOwn;
    private boolean isEquipped;
}
