package com.team44.isa_youtubeich.util;

import com.team44.isa_youtubeich.domain.model.Role;
import com.team44.isa_youtubeich.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "app.jwt.expires-in-ms=86400000")
class TokenUtilsExpiryTest {

    @Autowired
    private TokenUtils tokenUtils;

    @Test
    void tokenExpiryIsAtLeast24Hours() {
        User user = new User();
        user.setUsername("testuser");

        // Moramo dodati i rolu jer TokenUtils pokušava da je pročita (user.getAuthorities())
        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(List.of(role));

        // Pozivamo metodu sa objektom umesto stringa
        String token = tokenUtils.generateToken(user);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(tokenUtils.SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();

        long diffMs = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertTrue(diffMs >= Duration.ofHours(24).toMillis(), "JWT exp should be at least 24h after iat");

        // Also make sure API-reported expiresIn matches configured expiry.
        assertTrue(tokenUtils.getExpiresIn() >= Duration.ofHours(24).toMillis(), "expiresIn should be at least 24h");
    }
}
