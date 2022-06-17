package com.fyre.repositories;

import com.fyre.models.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAuthorUsernameAndProblemIndex(String username, String problemIndex);

    int countByAuthorUsernameAndProblemIndexAndContestId(String username, String problemIndex, long contestId);

    List<Submission> findByContestId(long contestId);
}
