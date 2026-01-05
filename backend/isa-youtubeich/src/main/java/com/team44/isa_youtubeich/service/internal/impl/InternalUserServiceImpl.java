package com.team44.isa_youtubeich.service.internal.impl;

import com.team44.isa_youtubeich.domain.model.User;
import com.team44.isa_youtubeich.repository.UserRepository;
import com.team44.isa_youtubeich.service.internal.InternalRoleService;
import com.team44.isa_youtubeich.service.internal.InternalUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InternalUserServiceImpl implements InternalUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InternalRoleService roleService;

    @Override
    public User findByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if(user == null){
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        }

        return user;
    }

}