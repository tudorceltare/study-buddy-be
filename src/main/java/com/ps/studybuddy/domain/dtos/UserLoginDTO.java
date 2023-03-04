package com.ps.studybuddy.domain.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UserLoginDTO {
    private String username;
    private String password;
}
