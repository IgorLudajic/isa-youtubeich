package com.team44.isa_youtubeich.controller;

import com.team44.isa_youtubeich.dto.JwtAuthRequestDto;
import com.team44.isa_youtubeich.dto.SignupRequestDto;
import com.team44.isa_youtubeich.dto.UserResponseDto;
import com.team44.isa_youtubeich.dto.UserTokenStateDto;
import com.team44.isa_youtubeich.service.application.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<UserTokenStateDto> createAuthenticationToken(
            @RequestBody JwtAuthRequestDto authenticationRequest) {
        UserTokenStateDto tokenState = userService.login(authenticationRequest);
        return ResponseEntity.ok(tokenState);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@Valid @RequestBody SignupRequestDto signupRequest) {
        UserResponseDto user = userService.signup(signupRequest);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
}
