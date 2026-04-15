package com.corecompass.auth.dto;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String  accessToken;
    @Builder.Default
    private String  tokenType  = "Bearer";
    private long    expiresIn;   // seconds (900 = 15 min)
    private UserDTO user;
}
