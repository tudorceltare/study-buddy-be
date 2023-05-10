package com.ps.studybuddy.domain.repositories;

import com.ps.studybuddy.domain.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {
}
