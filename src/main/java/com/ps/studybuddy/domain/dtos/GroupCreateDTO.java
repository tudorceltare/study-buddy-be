package com.ps.studybuddy.domain.dtos;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupCreateDTO {
    private String name;
    private String description;
    private String location;
    private List<TopicDTO> topics;
}
