package com.example.voting;

import com.example.voting.entity.Candidate;
import com.example.voting.entity.User;
import com.example.voting.exception.CandidateNotFoundException;
import com.example.voting.exception.VoteConflictException;
import com.example.voting.repository.CandidateRepository;
import com.example.voting.repository.UserRepository;
import com.example.voting.service.VoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:voting;NON_KEYWORDS=USER",
        "spring.datasource.username=sa",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class VotingSystemTests {

    @Autowired
    private VoteService voteService;

    @Autowired
    private UserRepository users;

    @Autowired
    private CandidateRepository candidates;

    @Autowired
    private MockMvc mvc;

    @Test
    void rejectsConcurrentRepeatVoteWithoutLosingOtherVoters() throws Exception {
        candidate("Race Candidate");
        user("one@example.com");
        user("two@example.com");

        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(3);
        try {
            List<Future<Boolean>> attempts = List.of(
                    executor.submit(() -> attemptVote(start, "one@example.com")),
                    executor.submit(() -> attemptVote(start, "one@example.com")),
                    executor.submit(() -> attemptVote(start, "two@example.com")));
            start.countDown();

            assertThat(List.of(attempts.get(0).get(10, TimeUnit.SECONDS),
                    attempts.get(1).get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
            assertThat(attempts.get(2).get(10, TimeUnit.SECONDS)).isTrue();
            assertThat(candidates.findVoteCount("Race Candidate")).contains(2L);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void rollsBackVoterStatusWhenCandidateIsMissing() {
        user("rollback@example.com");
        candidate("Retry Candidate");

        assertThatThrownBy(() -> voteService.submitVote("Missing", "rollback@example.com"))
                .isInstanceOf(CandidateNotFoundException.class);
        assertThat(users.findByEmail("rollback@example.com").orElseThrow().getStatus())
                .isEqualTo("Not Voted");

        voteService.submitVote("Retry Candidate", "rollback@example.com");
        assertThat(candidates.findVoteCount("Retry Candidate")).contains(1L);
    }

    @Test
    void ownsRegistrationStateAndVoteCountOnTheServer() throws Exception {
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email":"api@example.com","phone":"9876543210","status":"Voted",
                         "role":"ADMIN","password":"ignored"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Not Voted"))
                .andExpect(jsonPath("$.role").value("VOTER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mvc.perform(post("/api/candidate").contentType(MediaType.APPLICATION_JSON).content("""
                        {"candidate":"API Candidate","votes":1000}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.votes").value(0));

        mvc.perform(post("/api/candidate/vote")
                        .param("candidateName", "API Candidate").param("email", "api@example.com"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/candidate/vote")
                        .param("candidateName", "API Candidate").param("email", "api@example.com"))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/candidate/{name}/votes", "API Candidate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));

        Long candidateId = candidates.findByCandidate("API Candidate").orElseThrow().getId();
        mvc.perform(put("/api/candidate/{id}", candidateId).contentType(MediaType.APPLICATION_JSON).content("""
                        {"candidate":"Renamed Candidate","votes":999}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidate").value("Renamed Candidate"))
                .andExpect(jsonPath("$.votes").value(1));

        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email":"invalid","phone":"1"}
                        """))
                .andExpect(status().isBadRequest());
    }

    private boolean attemptVote(CountDownLatch start, String email) throws InterruptedException {
        start.await();
        try {
            voteService.submitVote("Race Candidate", email);
            return true;
        } catch (VoteConflictException ex) {
            return false;
        }
    }

    private void user(String email) {
        var user = new User();
        user.setEmail(email);
        user.setPhone("9876543210");
        user.setStatus("Not Voted");
        users.save(user);
    }

    private void candidate(String name) {
        var candidate = new Candidate();
        candidate.setCandidate(name);
        candidates.save(candidate);
    }
}
