package com.team44.isa_youtubeich.service.application;

import com.team44.isa_youtubeich.dto.JwtAuthRequestDto;
import com.team44.isa_youtubeich.dto.SignupRequestDto;
import com.team44.isa_youtubeich.dto.UserResponseDto;
import com.team44.isa_youtubeich.dto.UserTokenStateDto;

public interface UserService {
    UserTokenStateDto login(JwtAuthRequestDto authenticationRequest);

    UserResponseDto signup(SignupRequestDto signupRequest);

    void activateAccount(String activationToken);

    UserResponseDto getCurrentUser();
}
