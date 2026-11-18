package com.example.voting.repository;

import com.example.voting.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByCandidate(String candidate);

    @Query("select coalesce(c.votes, 0L) from Candidate c where c.candidate = :name")
    Optional<Long> findVoteCount(@Param("name") String name);

    @Modifying
    @Query("update Candidate c set c.votes = coalesce(c.votes, 0L) + 1 where c.candidate = :name")
    int incrementVotes(@Param("name") String name);

    @Modifying
    @Query("delete from Candidate c where c.id = :id and coalesce(c.votes, 0L) = 0")
    int deleteWithoutVotes(@Param("id") Long id);
}
