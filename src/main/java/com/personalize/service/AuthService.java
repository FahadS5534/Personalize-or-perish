package com.personalize.service;

import com.personalize.dto.auth.AuthResponse;
import com.personalize.dto.auth.LoginRequest;
import com.personalize.dto.auth.RegisterRequest;
import com.personalize.entity.Role;
import com.personalize.entity.User;
import com.personalize.exception.BadRequestException;
import com.personalize.repository.UserRepository;
import com.personalize.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        user = userRepository.save(user);

        return createAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse guestLogin(Role role) {
        Role targetRole = (role != null) ? role : Role.STUDENT;
        String email = (targetRole == Role.FACILITATOR) 
                ? "guest.facilitator@personalize.io" 
                : "guest.student@personalize.io";
        String name = (targetRole == Role.FACILITATOR) ? "Guest Facilitator" : "Guest Student";

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode("GuestSecret123!"))
                    .role(targetRole)
                    .build();
            return userRepository.save(newUser);
        });

        return createAuthResponse(user);
    }

    private AuthResponse createAuthResponse(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("name", user.getName());
        extraClaims.put("email", user.getEmail());

        String jwtToken = jwtService.generateToken(extraClaims, userDetails);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .token(jwtToken)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
