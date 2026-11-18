package com.example.voting.service;

import com.example.voting.entity.Candidate;
import com.example.voting.exception.CandidateNotFoundException;
import com.example.voting.exception.VoteConflictException;
import com.example.voting.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidateService {

    private final CandidateRepository candidateRepository;

    @Transactional
    public Candidate addCandidate(Candidate candidate) {
        candidate.setVotes(0L);
        return candidateRepository.save(candidate);
    }

    public List<Candidate> getAllCandidate() {
        return candidateRepository.findAll();
    }

    public Candidate getCandidateById(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new CandidateNotFoundException("Candidate " + id + " not found"));
    }

    @Transactional
    public Candidate updateCandidate(Long id, String name) {
        Candidate existing = getCandidateById(id);
        existing.setCandidate(name);
        return existing;
    }

    @Transactional
    public void deleteCandidate(Long id) {
        if (candidateRepository.deleteWithoutVotes(id) == 0 && candidateRepository.existsById(id)) {
            throw new VoteConflictException("A candidate with recorded votes cannot be deleted");
        }
    }

    public Candidate getCandidateByCandidate(String name) {
        return candidateRepository.findByCandidate(name)
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found"));
    }

    public long getNumOfVotes(String name) {
        return candidateRepository.findVoteCount(name)
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found"));
    }
}
