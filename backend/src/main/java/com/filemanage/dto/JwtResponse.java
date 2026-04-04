package com.filemanage.dto;

import com.filemanage.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private Long companyId;
    private String companyName;
    private User.Role role;
    private User.UserStatus status;

    public JwtResponse(String token, Long id, String username, String email,
                       Long companyId, String companyName, User.Role role, User.UserStatus status) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.companyId = companyId;
        this.companyName = companyName;
        this.role = role;
        this.status = status;
    }
}
