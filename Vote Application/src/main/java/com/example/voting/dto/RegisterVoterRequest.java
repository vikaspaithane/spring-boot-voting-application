package com.example.voting.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterVoterRequest(
        String name,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits") String phone) {
}
