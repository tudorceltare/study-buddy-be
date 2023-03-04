package com.ps.studybuddy.domain.dtos;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String avatarColor;
    private String role;
    private boolean isActive;
    private boolean isNotLocked;
}
