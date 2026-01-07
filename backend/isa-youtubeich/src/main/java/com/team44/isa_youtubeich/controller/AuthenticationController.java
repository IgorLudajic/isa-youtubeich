package com.team44.isa_youtubeich.controller;

import com.team44.isa_youtubeich.dto.JwtAuthRequestDto;
import com.team44.isa_youtubeich.dto.SignupRequestDto;
import com.team44.isa_youtubeich.dto.UserResponseDto;
import com.team44.isa_youtubeich.dto.UserTokenStateDto;
import com.team44.isa_youtubeich.service.application.UserService;
import com.team44.isa_youtubeich.util.RateLimited;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RateLimited(key = "auth", limit = 5, windowSeconds = 60)
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

    @GetMapping("/activate")
    public ResponseEntity<String> activateAccount(@RequestParam("token") String token) {
        userService.activateAccount(token);
        return ResponseEntity.ok("Account activated successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        UserResponseDto user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }
}
