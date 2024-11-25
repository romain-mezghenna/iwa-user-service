package com.iwa.userservice.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.iwa.userservice.model.User;
import com.iwa.userservice.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        System.out.println("Processing request for path: " + path);

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("No Authorization header or invalid format");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        if (!jwtTokenUtil.validate(token)) {
            System.out.println("Invalid JWT token");
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = jwtTokenUtil.getUserId(token);
        User user = userService.getUserById(userId).orElse(null);

        if (user == null) {
            System.out.println("User not found for ID: " + userId);
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("User authenticated: " + user.getEmail());
        JwtAuthenticatedUser authentication = new JwtAuthenticatedUser(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/");
    }
}