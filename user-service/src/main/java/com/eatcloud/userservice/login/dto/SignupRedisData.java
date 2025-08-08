package com.eatcloud.userservice.login.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRedisData implements Serializable {
    private SignupRequestDto request;
    private String code;
}
