package com.team44.isa_youtubeich.security.auth;

import com.team44.isa_youtubeich.config.ActiveUsersMetricsConfig;
import com.team44.isa_youtubeich.util.TokenUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private TokenUtils tokenUtils;

    private UserDetailsService userDetailsService;

    private ActiveUsersMetricsConfig activeUsersMetricsConfig;

    protected final Log LOGGER = LogFactory.getLog(getClass());

    public TokenAuthenticationFilter(TokenUtils tokenHelper, UserDetailsService userDetailsService, ActiveUsersMetricsConfig activeUsersMetricsConfig) {
        this.tokenUtils = tokenHelper;
        this.userDetailsService = userDetailsService;
        this.activeUsersMetricsConfig = activeUsersMetricsConfig;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String username;
        String authToken = tokenUtils.getToken(request);

        try {

            if (authToken != null) {

                username = tokenUtils.getUsernameFromToken(authToken);

                if (username != null) {

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (tokenUtils.validateToken(authToken, userDetails)) {
                        TokenBasedAuthentication authentication = new TokenBasedAuthentication(userDetails);
                        authentication.setToken(authToken);
                        authentication.setAuthenticated(true);

                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // 6. Record user activity for metrics
                        if (activeUsersMetricsConfig != null) {
                            activeUsersMetricsConfig.recordUserActivity(username);
                        }
                    }
                }
            }

        } catch (ExpiredJwtException ex) {
            LOGGER.debug("Token expired!");
        }

        chain.doFilter(request, response);
    }

}
