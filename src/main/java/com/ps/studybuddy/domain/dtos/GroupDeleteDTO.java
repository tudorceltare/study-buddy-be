package com.ps.studybuddy.domain.dtos;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupDeleteDTO {
    private UUID groupId;
    private UUID adminId;
}
