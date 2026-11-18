package com.example.voting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CandidateRequest(
        @NotBlank(message = "Candidate name must not be blank") @Size(max = 255) String candidate) {
}
