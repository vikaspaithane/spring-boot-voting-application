package com.example.voting.dto;

import com.example.voting.entity.Candidate;

public record CandidateResponse(Long id, String candidate, Long votes) {

    public static CandidateResponse from(Candidate candidate) {
        return new CandidateResponse(candidate.getId(), candidate.getCandidate(), candidate.getVotes());
    }
}
