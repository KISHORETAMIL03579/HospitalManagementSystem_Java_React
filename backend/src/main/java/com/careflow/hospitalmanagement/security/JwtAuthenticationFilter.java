package com.careflow.hospitalmanagement.security;

import com.careflow.hospitalmanagement.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Claims claims = tokenProvider.getClaimsFromToken(jwt);
                String sub = claims.getSubject();
                if (sub != null) {
                    Integer userId = Integer.parseInt(sub);
                    userRepository.findByIdWithRole(userId).ifPresent(user -> {
                        if (user.isActive()) {
                            UserPrincipal userPrincipal = UserPrincipal.create(user);
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userPrincipal, null, userPrincipal.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    });
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}


//    "In Spring Boot, we can create custom middleware by implementing the `Filter` interface or extending `OncePerRequestFilter`. We override the `doFilter()` or `doFilterInternal()` method to intercept incoming HTTP requests, perform operations such as authentication, authorization, logging, or validation, and then use `filterChain.doFilter(request, response)` to pass the request to the next component in the filter chain."

//   If the interviewer asks about JWT authentication:

//    "We extend `OncePerRequestFilter`, extract the JWT token from the Authorization header, validate the token, identify the user, set the authentication details in `SecurityContextHolder`, and allow the request to proceed to the Controller."

//   Remember this key line:

//    `OncePerRequestFilter` + `doFilterInternal()` + `FilterChain` = Custom middleware in Spring Boot.