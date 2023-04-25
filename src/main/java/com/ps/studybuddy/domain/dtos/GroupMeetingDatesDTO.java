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
public class GroupMeetingDatesDTO {
    private UUID groupId;
    private List<Date> meetingDates;
}
