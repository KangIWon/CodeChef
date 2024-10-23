package com.sparta.codechef.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        try {
//            // URI 추출
//            String uri = request.getRequestURI();
//            List<String> uris = Arrays.stream(uri.split("/"))
//                    .filter(s -> !s.isBlank())
//                    .toList();
//
//            // 인증 객체 가져오기
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser)) {
//                log.warn("Authentication information not found or invalid.");
//                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
//                return;
//            }
//
//            AuthUser authUser = (AuthUser) authentication.getPrincipal();
//            boolean isAdmin = authUser.getAuthorities().stream()
//                    .anyMatch(a -> a.getAuthority().equals(UserRole.Authority.ADMIN));
//
//            // 필터 체인 실행
//            filterChain.doFilter(request, response);
//        } catch (Exception e) {
//            // 예외 발생 시, 예외 처리기 사용
//            handlerExceptionResolver.resolveException(request, response, null, e);
//        }
//    }
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        // 필터링을 제외할 경로 목록
//        String[] excludePaths = {"/auth/signup", "/auth/login", "/auth/reissue", "/error"};
//        String path = request.getRequestURI();
//        return Arrays.stream(excludePaths).anyMatch(path::startsWith);
//    }
//}
