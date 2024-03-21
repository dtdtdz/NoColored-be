package com.ssafy.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignDto {
    String id;
    String password;
    String passwordConfirm;
    String nickname;

    public boolean confirm(){
        return password.equals(passwordConfirm);
    }
}
