package com.filemanage.dto;

import com.filemanage.entity.User;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private User.UserStatus status;
    private Boolean sendEmail;
}
