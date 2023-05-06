package com.ps.studybuddy.domain.repositories;

import com.ps.studybuddy.domain.entities.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
    @Override
    @Query("select i from Topic i order by i.createdDate")
    List<Topic> findAll();

    Optional<Topic> findById(UUID id);
    Optional<Topic> findTopicByName(String name);

}
