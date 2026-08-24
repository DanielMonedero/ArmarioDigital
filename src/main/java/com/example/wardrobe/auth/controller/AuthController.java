package com.example.wardrobe.auth.controller;

import com.example.wardrobe.auth.dto.CreateUserRequest;
import com.example.wardrobe.auth.dto.LoginRequest;
import com.example.wardrobe.auth.dto.LoginResponse;
import com.example.wardrobe.auth.dto.UserResponse;
import com.example.wardrobe.auth.entity.User;
import com.example.wardrobe.auth.repository.UserRepository;
import com.example.wardrobe.auth.service.AuthService;
import com.example.wardrobe.auth.service.CustomUserDetailsService;
import com.example.wardrobe.common.config.AppProperties;
import com.example.wardrobe.common.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();
    private final AppProperties appProperties;

    public AuthController(AuthenticationManager authenticationManager,
                          AuthService authService,
                          UserRepository userRepository,
                          AppProperties appProperties) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
        this.userRepository = userRepository;
        this.appProperties = appProperties;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest,
                                               jakarta.servlet.http.HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return ResponseEntity.ok(new LoginResponse(UserResponse.from(user)));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException("Not authenticated");
        }
        CustomUserDetailsService.AppUserPrincipal principal =
                (CustomUserDetailsService.AppUserPrincipal) authentication.getPrincipal();
        UserResponse response = authService.getById(principal.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        if (!appProperties.getAuth().isAllowUserCreation()) {
            throw new NotFoundException("Endpoint not available");
        }
        UserResponse response = authService.createUser(request);
        return ResponseEntity.status(201).body(response);
    }
}
