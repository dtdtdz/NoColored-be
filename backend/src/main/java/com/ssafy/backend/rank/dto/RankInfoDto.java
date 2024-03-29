package com.ssafy.backend.rank.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RankInfoDto {
    private LocalDateTime refreshTime;
    private List<RankDto> players;
}
