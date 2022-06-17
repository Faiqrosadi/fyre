package com.fyre.repositories;

import com.fyre.models.University;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnivesityRepository extends JpaRepository<University, Integer> {
    University findById(String id);

}
