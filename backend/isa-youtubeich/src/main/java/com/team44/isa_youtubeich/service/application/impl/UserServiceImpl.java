package com.team44.isa_youtubeich.service.application.impl;

import com.team44.isa_youtubeich.service.application.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    // TODO izmestiti u aplikativni UserService tako da vraca DTO

    /*@Override
    public User save(UserRequestDto userRequest) {
        User u = new User();
        u.setUsername(userRequest.getUsername());

        u.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        u.setFirstName(userRequest.getFirstname());
        u.setLastName(userRequest.getLastname());
        u.setEnabled(true);
        u.setEmail(userRequest.getEmail());

        List<Role> roles = roleService.findByName("ROLE_USER");
        u.setRoles(roles);

        return this.userRepository.save(u);
    }*/
}
