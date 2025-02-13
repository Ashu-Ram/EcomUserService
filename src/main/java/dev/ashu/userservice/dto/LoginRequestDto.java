package dev.ashu.userservice.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class LoginRequestDto {
    private String email;
    private String password;

}
