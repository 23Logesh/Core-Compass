package com.corecompass.auth.config;

import com.corecompass.auth.dto.AuthResponse;
import com.corecompass.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Called after Google OAuth2 authentication succeeds.
 *
 * Flow:
 *  1. Google redirects back with auth code
 *  2. Spring Security exchanges code → ID token → loads OAuth2User
 *  3. This handler fires → extracts profile → issues JWT pair
 *  4. Redirects frontend to /oauth/callback?token=<accessToken>
 *     (refresh token is set as HttpOnly cookie automatically)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService   authService;
    private final ObjectMapper  objectMapper;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String googleId   = oAuth2User.getAttribute("sub");
        String email      = oAuth2User.getAttribute("email");
        String name       = oAuth2User.getAttribute("name");
        String avatarUrl  = oAuth2User.getAttribute("picture");

        log.info("Google OAuth2 success for: {}", email);

        AuthResponse authResponse = authService.loginWithGoogle(
            googleId, email, name, avatarUrl, response
        );

        // Redirect to frontend with access token in query param.
        // Frontend reads it, stores in memory, then discards the URL param.
        String redirectUrl = String.format(
            "%s/oauth/callback?token=%s&expiresIn=%d",
            frontendUrl,
            authResponse.getAccessToken(),
            authResponse.getExpiresIn()
        );

        response.sendRedirect(redirectUrl);
    }
}
