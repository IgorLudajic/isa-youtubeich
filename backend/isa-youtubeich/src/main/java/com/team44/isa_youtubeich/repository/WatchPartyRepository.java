package com.team44.isa_youtubeich.repository;

import com.team44.isa_youtubeich.domain.model.WatchParty;
import org.springframework.boot.autoconfigure.ssl.SslProperties;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchPartyRepository extends JpaRepository<WatchParty, Long> {
    List<WatchParty> findAllByOrderByCreatedAtDesc();
}
