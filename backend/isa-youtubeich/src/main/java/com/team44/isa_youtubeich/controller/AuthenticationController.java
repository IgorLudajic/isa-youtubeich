package com.team44.isa_youtubeich.controller;

import com.team44.isa_youtubeich.domain.model.User;
import com.team44.isa_youtubeich.dto.JwtAuthRequestDto;
import com.team44.isa_youtubeich.dto.UserRequestDto;
import com.team44.isa_youtubeich.dto.UserTokenStateDto;
import com.team44.isa_youtubeich.exception.ResourceConflictException;
import com.team44.isa_youtubeich.service.internal.InternalUserService;
import com.team44.isa_youtubeich.util.TokenUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private InternalUserService internalUserService;

    @PostMapping("/login")
    public ResponseEntity<UserTokenStateDto> createAuthenticationToken(
            @RequestBody JwtAuthRequestDto authenticationRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getUsername(), authenticationRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();
        String jwt = tokenUtils.generateToken(user.getUsername());
        long expiresIn = tokenUtils.getExpiresIn();

        return ResponseEntity.ok(new UserTokenStateDto(jwt, expiresIn));
    }

    // TODO izmeniti po implementaciji UserService
    /*@PostMapping("/signup")
    public ResponseEntity<User> addUser(@RequestBody UserRequestDto userRequest, UriComponentsBuilder ucBuilder) {
        User existingUser = this.internalUserService.findByUsername(userRequest.getUsername());

        if (existingUser != null) {
            throw new ResourceConflictException(userRequest.getId(), "Username already exists");
        }

        User user = this.internalUserService.save(userRequest);

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }*/
}
