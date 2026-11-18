package com.example.voting.controller;

import com.example.voting.dto.RegisterVoterRequest;
import com.example.voting.dto.VoterResponse;
import com.example.voting.entity.User;
import com.example.voting.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<VoterResponse> addUser(@Valid @RequestBody RegisterVoterRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        return ResponseEntity.ok(VoterResponse.from(userService.addUser(user)));
    }

    @GetMapping
    public ResponseEntity<List<VoterResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream().map(VoterResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VoterResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(VoterResponse.from(userService.getUserById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully with id " + id);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<VoterResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(VoterResponse.from(userService.getUserByEmail(email)));
    }
}
