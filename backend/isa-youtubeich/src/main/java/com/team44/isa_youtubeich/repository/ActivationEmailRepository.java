package com.team44.isa_youtubeich.repository;

import com.team44.isa_youtubeich.domain.model.ActivationEmail;
import com.team44.isa_youtubeich.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivationEmailRepository extends JpaRepository<ActivationEmail, Long> {
    Optional<ActivationEmail> findByUser(User user);

    Optional<ActivationEmail> findByActivationToken(String activationToken);
}

