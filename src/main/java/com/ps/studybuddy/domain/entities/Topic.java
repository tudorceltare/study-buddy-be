package com.ps.studybuddy.domain.entities;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
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
public class Topic {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "pg-uuid")
    private UUID id;
    @Column(name = "created_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "topics", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Group> groups;
}
