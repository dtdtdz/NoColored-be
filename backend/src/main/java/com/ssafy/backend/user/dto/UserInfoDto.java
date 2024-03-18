package com.ssafy.backend.user.dto;

public class UserInfoDto {
    String token;//이건 로그인 후 자기정보일때만 not null
    String userCode;
    String nickName;
    long exp; //누적 경험치? 남은 경험치? 둘다?
    int level;
    String tier;
    String skinId;
    String titleId;
}
