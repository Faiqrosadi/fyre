package com.fyre.repositories;

import com.fyre.models.ContestProblems;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContestProblemsRepository extends JpaRepository<ContestProblems, Long> {
    List<ContestProblems> findByContestId(Long contestId);
    ContestProblems findByContestIdAndProblemIdx(Long contestId, int problemIdx);
}

