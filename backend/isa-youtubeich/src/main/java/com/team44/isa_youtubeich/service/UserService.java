package com.team44.isa_youtubeich.service;

import com.team44.isa_youtubeich.domain.model.User;
import com.team44.isa_youtubeich.dto.UserRequestDto;

import java.util.List;

public interface UserService {
    User findById(Long id);
    User findByUsername(String username);
    List<User> findAll ();
    User save(UserRequestDto userRequest);
}
