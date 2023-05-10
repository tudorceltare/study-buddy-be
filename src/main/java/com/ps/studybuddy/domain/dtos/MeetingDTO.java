package com.ps.studybuddy.domain.dtos;

import lombok.*;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingDTO {
    private Date meetingDate;
    private LocationDTO location;
    private String groupName;
    private UUID groupId;
}
