package com.example.voting.service;

import com.example.voting.entity.User;
import com.example.voting.exception.UserNotFoundException;
import com.example.voting.exception.VoteConflictException;
import com.example.voting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User addUser(User user) {
        user.setStatus("Not Voted");
        user.setRole("VOTER");
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User " + id + " not found"));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (userRepository.deleteBeforeVote(id) == 0 && userRepository.existsById(id)) {
            throw new VoteConflictException("A user who has voted cannot be deleted");
        }
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
