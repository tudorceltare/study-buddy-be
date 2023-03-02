package com.ps.studybuddy.domain.repositories;

import com.ps.studybuddy.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Override
    @Query("select i from User i order by i.createdDate")
    List<User> findAll();

    Optional<User> findById(UUID id);
    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByEmail(String email);
}
