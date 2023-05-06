package com.ps.studybuddy.domain.dtos;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicDTO {
    private UUID id;
    private String name;
    private String description;
}
