package com.team44.isa_youtubeich.service;

import com.team44.isa_youtubeich.dto.*;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserTokenStateDto login(JwtAuthRequestDto authenticationRequest);

    UserResponseDto signup(SignupRequestDto signupRequest);

    void activateAccount(String activationToken);

    UserPublicProfileDto getPublicProfile(String username, Pageable pageable);
}
