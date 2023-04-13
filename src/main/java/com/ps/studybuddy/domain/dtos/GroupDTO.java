package com.ps.studybuddy.domain.dtos;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupDTO {
    private UUID id;
    private String name;
    private String description;
    private String location;
}
