package com.ps.studybuddy.domain.repositories;

import com.ps.studybuddy.domain.entities.Group;
import com.ps.studybuddy.domain.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    @Override
    @Query("select i from Group i order by i.createdDate")
    List<Group> findAll();
    @Override
    @EntityGraph(value = "group-with-meeting-dates", type = EntityGraph.EntityGraphType.FETCH)
    Optional<Group> findById(UUID id);

    List<Group> findGroupsByAdmin(User user);
    List<Group> findGroupsByMembersContaining(User user);
}
