package com.team44.isa_youtubeich.service.application.impl;

import com.team44.isa_youtubeich.domain.model.AddressJson;
import com.team44.isa_youtubeich.domain.model.Role;
import com.team44.isa_youtubeich.domain.model.User;
import com.team44.isa_youtubeich.dto.JwtAuthRequestDto;
import com.team44.isa_youtubeich.dto.SignupRequestDto;
import com.team44.isa_youtubeich.dto.UserResponseDto;
import com.team44.isa_youtubeich.dto.UserTokenStateDto;
import com.team44.isa_youtubeich.exception.ValidationException;
import com.team44.isa_youtubeich.repository.RoleRepository;
import com.team44.isa_youtubeich.repository.UserRepository;
import com.team44.isa_youtubeich.service.application.UserService;
import com.team44.isa_youtubeich.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserTokenStateDto login(JwtAuthRequestDto authenticationRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();
        assert user != null;
        String jwt = tokenUtils.generateToken(user.getUsername());
        long expiresIn = tokenUtils.getExpiresIn();

        return new UserTokenStateDto(jwt, expiresIn);
    }

    @Override
    @Transactional
    public UserResponseDto signup(SignupRequestDto signupRequest) {
        // Validate password match
        if (!signupRequest.getPassword().equals(signupRequest.getPasswordConfirm())) {
            throw new ValidationException("Passwords do not match");
        }

        // Validate password length (additional check beyond annotation)
        if (signupRequest.getPassword().length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }

        // Check if username already exists
        User existingUser = userRepository.findByUsername(signupRequest.getUsername());
        if (existingUser != null) {
            throw new ValidationException("Username already exists");
        }

        // Check if email already exists
        User existingEmail = userRepository.findByEmail(signupRequest.getEmail());
        if (existingEmail != null) {
            throw new ValidationException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setEmail(signupRequest.getEmail());
        user.setFirstName(signupRequest.getName());
        user.setLastName(signupRequest.getSurname());
        user.setEnabled(true);
        user.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        // Assign default role (ROLE_USER)
        List<Role> roles = roleRepository.findByName("ROLE_USER");
        if (roles.isEmpty()) {
            throw new ValidationException("Default role not found");
        }
        user.setRoles(roles);

        // Create and set address as JSON
        AddressJson addressJson = new AddressJson(
                signupRequest.getStreet(),
                signupRequest.getCity(),
                signupRequest.getCountry()
        );
        user.setAddressJson(addressJson);

        // Save user
        User savedUser = userRepository.save(user);

        // Convert to DTO and return
        return mapToUserResponseDto(savedUser);
    }

    private UserResponseDto mapToUserResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setName(user.getFirstName());
        dto.setSurname(user.getLastName());

        if (user.getAddressJson() != null) {
            dto.setStreet(user.getAddressJson().getStreet());
            dto.setCity(user.getAddressJson().getCity());
            dto.setCountry(user.getAddressJson().getCountry());
        }

        return dto;
    }
}
