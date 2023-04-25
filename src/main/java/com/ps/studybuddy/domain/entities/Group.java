package com.ps.studybuddy.domain.entities;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "app_group")
@NamedEntityGraph(name = "group-with-meeting-dates", attributeNodes = @NamedAttributeNode("meetingDates"))
public class Group implements Serializable {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "pg-uuid")
    private UUID id;
    @Column(name = "created_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    @Column(name = "location", nullable = false)
    private String location;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "group_members",
            joinColumns = @JoinColumn(name = "group_id")
    )
    @ToString.Exclude
    private List<User> members;
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "group_meeting_dates", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "meeting_dates")
    @Temporal(TemporalType.TIMESTAMP)
    private List<Date> meetingDates;
}
