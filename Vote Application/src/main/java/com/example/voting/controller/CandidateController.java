package com.example.voting.controller;

import com.example.voting.dto.CandidateRequest;
import com.example.voting.dto.CandidateResponse;
import com.example.voting.entity.Candidate;
import com.example.voting.service.CandidateService;
import com.example.voting.service.VoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/candidate")
public class CandidateController {

    private final CandidateService candidateService;

    private final VoteService voteService;

    @PostMapping("/vote")
    public ResponseEntity<String> vote(
            @RequestParam @NotBlank String candidateName,
            @RequestParam @NotBlank @Email String email) {
        voteService.submitVote(candidateName, email);
        return ResponseEntity.ok("Vote submitted successfully!");
    }

    @PostMapping
    public ResponseEntity<CandidateResponse> addCandidate(@Valid @RequestBody CandidateRequest request) {
        Candidate candidate = new Candidate();
        candidate.setCandidate(request.candidate());
        return ResponseEntity.ok(CandidateResponse.from(candidateService.addCandidate(candidate)));
    }

    @GetMapping
    public ResponseEntity<List<CandidateResponse>> getAllCandidate() {
        return ResponseEntity.ok(candidateService.getAllCandidate().stream().map(CandidateResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(CandidateResponse.from(candidateService.getCandidateById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateResponse> updateCandidate(@PathVariable Long id, @Valid @RequestBody CandidateRequest request) {
        return ResponseEntity.ok(CandidateResponse.from(candidateService.updateCandidate(id, request.candidate())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        candidateService.deleteCandidate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<CandidateResponse> getCandidateByName(@PathVariable String name) {
        return ResponseEntity.ok(CandidateResponse.from(candidateService.getCandidateByCandidate(name)));
    }

    @GetMapping("/{name}/votes")
    public ResponseEntity<Long> getVotes(@PathVariable String name) {
        long votes = candidateService.getNumOfVotes(name);
        return ResponseEntity.ok(votes);
    }

}
