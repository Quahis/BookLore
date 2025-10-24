package com.adityachandel.booklore.controller;

import com.adityachandel.booklore.config.AppProperties;
import com.adityachandel.booklore.config.security.service.AuthenticationService;
import com.adityachandel.booklore.exception.ApiError;
import com.adityachandel.booklore.model.dto.UserCreateRequest;
import com.adityachandel.booklore.model.dto.request.RefreshTokenRequest;
import com.adityachandel.booklore.model.dto.request.UserLoginRequest;
import com.adityachandel.booklore.model.entity.BookLoreUserEntity;
import com.adityachandel.booklore.repository.UserRepository;
import com.adityachandel.booklore.service.user.UserProvisioningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Tag(name = "Authentication", description = "Endpoints for user authentication, registration, and token management")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AppProperties appProperties;
    private final UserProvisioningService userProvisioningService;
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    @Operation(summary = "Register a new user", description = "Register a new user. Only admins can register users.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "User registered successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/register")
    @PreAuthorize("@securityUtil.isAdmin()")
    public ResponseEntity<?> registerUser(
            @Parameter(description = "User registration request") @RequestBody @Valid UserCreateRequest userCreateRequest) {
        userProvisioningService.provisionInternalUser(userCreateRequest);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Login user", description = "Authenticate a user and return JWT tokens.")
    @ApiResponse(responseCode = "200", description = "User authenticated successfully")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(
            @Parameter(description = "User login request") @RequestBody @Valid UserLoginRequest loginRequest) {
        return authenticationService.loginUser(loginRequest);
    }

    @Operation(summary = "Refresh JWT token", description = "Refresh the JWT token using a valid refresh token.")
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(
            @Parameter(description = "Refresh token request") @Valid @RequestBody RefreshTokenRequest request) {
        return authenticationService.refreshToken(request.getRefreshToken());
    }

    @Operation(summary = "Remote login", description = "Authenticate a user using remote authentication headers.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
        @ApiResponse(responseCode = "403", description = "Remote authentication is disabled")
    })
    @GetMapping("/remote")
    public ResponseEntity<Map<String, String>> loginRemote(
            @Parameter(description = "Authentication headers") @RequestHeader Map<String, String> headers) {
        if (!appProperties.getRemoteAuth().isEnabled()) {
            throw ApiError.REMOTE_AUTH_DISABLED.createException();
        }

        String name = headers.get(appProperties.getRemoteAuth().getHeaderName().toLowerCase(Locale.ROOT));
        String username = headers.get(appProperties.getRemoteAuth().getHeaderUser().toLowerCase(Locale.ROOT));
        String email = headers.get(appProperties.getRemoteAuth().getHeaderEmail().toLowerCase(Locale.ROOT));
        String groups = headers.get(appProperties.getRemoteAuth().getHeaderGroups().toLowerCase(Locale.ROOT));
        log.debug("Remote-Auth: retrieved values from headers: name: {}, username: {}, email: {}, groups: {}", name, username, email, groups);
        log.debug("Remote-Auth: remote auth settings: {}", appProperties.getRemoteAuth());

        if ((username == null || username.isEmpty()) && (email != null && !email.isEmpty())) {
            log.debug("Remote-Auth: username is empty, trying to find user by email: {}", email);
            Optional<BookLoreUserEntity> user = userRepository.findByEmail(email);
            if (user.isPresent()) {
                username = user.get().getUsername();
                log.debug("Remote-Auth: found user by email, username: {}", username);
            }
        }

        return authenticationService.loginRemote(name, username, email, groups);
    }
}
