package com.ps.studybuddy.domain.dtos;

import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupDetailsDTO {
    private UUID id;
    private String name;
    private String description;
    private String location;
    private UserDTO admin;
    private List<UserDTO> members;
    private List<Date> meetingDates;
    private List<TopicDTO> topics;
}
