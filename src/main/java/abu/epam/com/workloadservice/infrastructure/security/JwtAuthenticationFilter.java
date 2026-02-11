package abu.epam.com.workloadservice.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String requestUri = request.getRequestURI();

        log.info("Processing request to: {}, Authorization header present: {}", requestUri, authHeader != null);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No Bearer token found for request to: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = authHeader.substring(7);
            log.info("Extracting username from JWT token (length: {})", jwt.length());
            String username = jwtUtil.extractUsername(jwt);
            log.info("Extracted username: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.info("Validating JWT token for user: {}", username);
                if (jwtUtil.validateToken(jwt)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("JWT authentication successful for user: {}", username);
                } else {
                    log.error("JWT token validation failed for user: {}", username);
                }
            } else {
                log.warn("Username is null or authentication already set. Username: {}, Auth: {}",
                        username, SecurityContextHolder.getContext().getAuthentication());
            }
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}