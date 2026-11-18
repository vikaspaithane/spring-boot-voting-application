package com.example.voting.dto;

import com.example.voting.entity.User;

public record VoterResponse(Long id, String name, String email, String phone, String status, String role) {

    public static VoterResponse from(User user) {
        return new VoterResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(),
                user.getStatus(), user.getRole());
    }
}
