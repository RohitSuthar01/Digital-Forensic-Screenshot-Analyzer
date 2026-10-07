package com.dfsa.security;

import com.dfsa.dto.AuthResponse;
import com.dfsa.model.LoginAttempt;
import com.dfsa.model.User;
import com.dfsa.repository.LoginAttemptRepository;
import com.dfsa.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
public class ApiAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // Reset failed attempts on successful login
        String username = authentication.getName();
        userService.resetFailedAttempts(username);

        // Log successful login attempt
        LoginAttempt loginAttempt = new LoginAttempt();
        loginAttempt.setUsername(username);
        loginAttempt.setIpAddress(request.getRemoteAddr());
        loginAttempt.setSuccess(true);
        loginAttempt.setTimestamp(new Date());
        loginAttemptRepository.save(loginAttempt);

        // Create a JSON response with the authenticated user's details
        AuthResponse authResponse = new AuthResponse(
                authentication.getName(),
                authentication.getAuthorities().toString()
        );

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), authResponse);
    }
}