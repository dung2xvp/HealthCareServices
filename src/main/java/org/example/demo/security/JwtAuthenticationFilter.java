package org.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter - Filter kiểm tra JWT token trong mỗi request
 * 
 * Flow:
 * 1. Lấy token từ header "Authorization: Bearer <token>"
 * 2. Validate token
 * 3. Nếu hợp lệ: Load user và set vào SecurityContext
 * 4. Continue filter chain
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Lấy JWT token từ request header
            String jwt = getJwtFromRequest(request);

            // 2. Validate token và set authentication
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // 3. Lấy email từ token
                String email = jwtTokenProvider.getUsernameFromToken(jwt);

                // 4. Load user từ database
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                // 5. Tạo Authentication object
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 6. Set authentication vào SecurityContext
                // Từ giờ SecurityContextHolder.getContext().getAuthentication() sẽ có user
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            System.out.println("Could not set user authentication in security context: " + ex.getMessage());
        }

        // 7. Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Lấy JWT token từ header "Authorization: Bearer <token>"
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        // Kiểm tra header có format: "Bearer <token>"
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Bỏ "Bearer " để lấy token
        }
        
        return null;
    }
}

