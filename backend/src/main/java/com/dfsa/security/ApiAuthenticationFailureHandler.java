package com.dfsa.security;

import com.dfsa.model.LoginAttempt;
import com.dfsa.model.User;
import com.dfsa.repository.LoginAttemptRepository;
import com.dfsa.repository.UserRepository;
import com.dfsa.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
public class ApiAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        String ipAddress = request.getRemoteAddr();

        // Log the failed login attempt
        LoginAttempt loginAttempt = new LoginAttempt();
        loginAttempt.setUsername(username != null ? username : "unknown");
        loginAttempt.setIpAddress(ipAddress);
        loginAttempt.setSuccess(false);
        loginAttempt.setTimestamp(new Date());
        loginAttemptRepository.save(loginAttempt);

        // Only increment failed attempts for bad credentials (wrong password) and if the user exists
        if (exception instanceof BadCredentialsException) {
            if (username != null && !username.isEmpty()) {
                userService.incrementFailedAttempts(username);
            }
        }

        // Set the response status to 401 (Unauthorized) and return JSON error
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        // Create an error response
        String errorMessage = exception.getMessage();
        if (exception instanceof LockedException) {
            errorMessage = "Account is locked due to too many failed login attempts. Please try again later.";
        } else if (exception instanceof BadCredentialsException) {
            errorMessage = "Invalid username or password.";
        }

        // We can use a simple JSON string or use a mapper. For simplicity, we'll write a JSON string.
        String json = String.format("{\"timestamp\":%d,\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\"}",
                System.currentTimeMillis(), errorMessage, request.getRequestURI());

        response.getWriter().write(json);
    }
}