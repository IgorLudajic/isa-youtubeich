package com.team44.isa_youtubeich.controller;

import com.team44.isa_youtubeich.dto.UserPublicProfileDto;
import com.team44.isa_youtubeich.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{username}/profile")
    public ResponseEntity<UserPublicProfileDto> getProfile(@PathVariable String username, @PageableDefault(size = 12) Pageable pageable){
        return ResponseEntity.ok(userService.getPublicProfile(username, pageable));
    }
}
