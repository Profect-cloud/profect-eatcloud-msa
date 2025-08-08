package com.eatcloud.userservice.login.dto;

import lombok.Getter;

@Getter
public class PasswordChangeRequestDto {
    private String currentPassword;
    private String newPassword;
}
