package com.team44.isa_youtubeich.repository;

import com.team44.isa_youtubeich.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
