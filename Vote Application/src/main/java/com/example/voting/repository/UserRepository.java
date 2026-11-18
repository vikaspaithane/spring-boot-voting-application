package com.example.voting.repository;

import com.example.voting.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("""
            update User u set u.status = 'Voted'
            where u.email = :email and upper(coalesce(u.status, '')) <> 'VOTED'
            """)
    int markVoted(@Param("email") String email);

    @Modifying
    @Query("delete from User u where u.id = :id and upper(coalesce(u.status, '')) <> 'VOTED'")
    int deleteBeforeVote(@Param("id") Long id);
}
