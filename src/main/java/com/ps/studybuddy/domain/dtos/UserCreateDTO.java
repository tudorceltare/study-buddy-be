package com.ps.studybuddy.domain.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UserCreateDTO {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private String role;
    boolean isNotLocked;
    boolean isActive;
}
