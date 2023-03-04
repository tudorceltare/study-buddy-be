package com.ps.studybuddy.domain.dtos;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UserUpdateDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String newUsername;
    private String username;
    private String newEmail;
    private String email;
    boolean isActive;
    boolean isNotLocked;
}
