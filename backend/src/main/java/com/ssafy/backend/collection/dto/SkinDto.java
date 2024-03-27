package com.ssafy.backend.collection.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SkinDto {
    private int id;
    private String name;
    private String link;
    private boolean isOwn;
    private boolean isEquipped;
}
