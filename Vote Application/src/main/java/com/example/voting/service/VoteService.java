package com.example.voting.service;

import com.example.voting.exception.CandidateNotFoundException;
import com.example.voting.exception.UserNotFoundException;
import com.example.voting.exception.VoteConflictException;
import com.example.voting.repository.CandidateRepository;
import com.example.voting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoteService {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;

    @Transactional
    public void submitVote(String candidateName, String email) {
        if (userRepository.markVoted(email) == 0) {
            if (!userRepository.existsByEmail(email)) {
                throw new UserNotFoundException("User not found");
            }
            throw new VoteConflictException("You have already voted");
        }

        if (candidateRepository.incrementVotes(candidateName) == 0) {
            throw new CandidateNotFoundException("Candidate not found");
        }
        log.debug("Vote recorded");
    }
}
