package com.sparta.codechef.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class RoleFilter extends OncePerRequestFilter {
//    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String[] excludePath = {"/auth/signup", "/auth/login", "/auth/reissue", "/error"};
        String path = request.getRequestURI();
        return Arrays.stream(excludePath).anyMatch(path::startsWith);
    }
}
