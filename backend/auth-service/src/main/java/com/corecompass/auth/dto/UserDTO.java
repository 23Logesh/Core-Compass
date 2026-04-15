package com.corecompass.auth.dto;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserDTO {
    private UUID    id;
    private String  email;
    private String  name;
    private String  role;
    private String  avatarUrl;
    private Instant createdAt;
}
