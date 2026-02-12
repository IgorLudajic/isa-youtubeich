package com.team44.isa_youtubeich.service.impl;

import com.team44.isa_youtubeich.domain.model.ActivationEmail;
import com.team44.isa_youtubeich.domain.model.AddressJson;
import com.team44.isa_youtubeich.domain.model.Role;
import com.team44.isa_youtubeich.domain.model.User;
import com.team44.isa_youtubeich.dto.*;
import com.team44.isa_youtubeich.exception.AccountNotActivatedException;
import com.team44.isa_youtubeich.exception.ValidationException;
import com.team44.isa_youtubeich.repository.ActivationEmailRepository;
import com.team44.isa_youtubeich.repository.RoleRepository;
import com.team44.isa_youtubeich.repository.UserRepository;
import com.team44.isa_youtubeich.repository.VideoRepository;
import com.team44.isa_youtubeich.service.EmailService;
import com.team44.isa_youtubeich.service.UserService;
import com.team44.isa_youtubeich.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private VideoRepository videoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ActivationEmailRepository activationEmailRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public UserTokenStateDto login(JwtAuthRequestDto authenticationRequest) {
        Authentication authentication = null;
        authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()));

        // authenticate() will throw if credentials are invalid
        // Now check if the account is enabled only *after* successful authentication
        User user = userRepository.findByUsername(authenticationRequest.getUsername());
        if (user != null && !user.isEnabled()) {
            throw new AccountNotActivatedException();
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User authenticatedUser = (User) authentication.getPrincipal();
        assert authenticatedUser != null;
        String jwt = tokenUtils.generateToken(authenticatedUser);
        //String jwt = tokenUtils.generateToken(authenticatedUser.getUsername());
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
        user.setEnabled(false); // User disabled until email verification
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

        // Create activation email token
        String activationToken = UUID.randomUUID().toString();
        ActivationEmail activationEmail = new ActivationEmail();
        activationEmail.setUser(savedUser);
        activationEmail.setActivationToken(activationToken);
        activationEmail.setIssuedAt(new Timestamp(System.currentTimeMillis()));
        // TODO: Make configurable
        // Token expires in 24 hours
        activationEmail.setExpiresAt(new Timestamp(System.currentTimeMillis() + 24 * 60 * 60 * 1000));
        activationEmailRepository.save(activationEmail);

        // Send activation email
        emailService.sendActivationEmail(savedUser, activationToken);

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

    @Override
    @Transactional
    public void activateAccount(String activationToken) {
        // Find activation email by token
        Optional<ActivationEmail> activationEmailOpt = activationEmailRepository
                .findByActivationToken(activationToken);

        try {
            if (activationEmailOpt.isEmpty()) {
                throw new ValidationException("Invalid activation token");
            }

            ActivationEmail activationEmail = activationEmailOpt.get();

            // Check if token has expired
            if (activationEmail.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
                throw new ValidationException("Activation token has expired");
            }

            // Enable user account
            User user = activationEmail.getUser();
            if (user.isEnabled()) {
                throw new ValidationException("Account is already activated");
            }

            user.setEnabled(true);
            userRepository.save(user);
        } finally {
            activationEmailOpt.ifPresent(activationEmail -> {
                // Delete activation email record to prevent reuse
                activationEmailRepository.delete(activationEmail);
            });
        }
    }

    @Override
    public UserResponseDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new ValidationException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();
        return mapToUserResponseDto(user);
    }

    @Override
    public UserPublicProfileDto getPublicProfile(String username, Pageable pageable){
        User user = userRepository.findByUsername(username);

        if(user == null)
            throw new RuntimeException("User not found");

        Page<VideoHomeDto> videos = videoRepository.findByUserUsernameOrderByCreatedAtDesc(username, pageable)
                .map(video -> new VideoHomeDto(
                        video.getId(), video.getTitle(), video.getThumbnailUrl(), video.getViewCount(), video.getLikes(), video.getDislikes(), Date.from(video.getCreatedAt().toInstant()), username)
                );

        return new UserPublicProfileDto(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getCreatedAt(),
                videos
        );
    }
}
