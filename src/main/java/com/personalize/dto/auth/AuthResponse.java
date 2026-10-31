package com.personalize.dto.auth;

import com.personalize.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String token; // Alias for frontend compatibility

    @Builder.Default
    private String tokenType = "Bearer";

    private Long userId;
    private String name;
    private String email;
    private Role role;
}
