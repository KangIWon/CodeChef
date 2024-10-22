//package com.sparta.codechef.security;
//
//import com.sparta.codechef.common.enums.UserRole;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import org.springframework.web.servlet.HandlerExceptionResolver;
//
//import java.util.Arrays;
//import java.util.List;
//
//@RequiredArgsConstructor
//@Component
//public abstract class RoleFilter extends OncePerRequestFilter {
////    private final MemberRepository memberRepository;
////    private final HandlerExceptionResolver handlerExceptionResolver;
////
////    @Override
////    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
////        try {
////            String uri = request.getRequestURI();
////            List<String> uris = Arrays.stream(uri.split("/")).filter(s -> !s.isBlank()).toList();
////            int workspacesIndex = uris.indexOf("workspaces");
////            AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
////            boolean isAdmin = authUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(UserRole.Authority.ADMIN));
////
////            // 어드민이 아닌 경우 && uri에 /workspaces 가 있는 경우 && /workspaces/* 인 경우
////            // 특정 워크스페이스에 어드민이 아닌 유저가 접근하려는 경우
////            if (!isAdmin && workspacesIndex != -1 && workspacesIndex != uris.size() - 1) {
////                Long workspaceId = Long.valueOf(uris.get(workspacesIndex + 1));
////                Member member = memberRepository.findByWorkspace_IdAndUser_Id(workspaceId, authUser.getUserId())
////                        // 워크스페이스 멤버가 아닌 경우
////                        .orElseThrow(AccessDeniedException::new);
////
////                // 워크스페이스 멤버이지만 역할이 READ_ONLY이고 메서드가 GET이 아닌 경우
////                if (member.getRole().equals(MemberRole.ROLE_READ_ONLY) && !request.getMethod().equals(HttpMethod.GET.name()))
////                    throw new AccessDeniedException();
////            }
////
////            filterChain.doFilter(request, response);
////        } catch (Exception e) {
////            handlerExceptionResolver.resolveException(request, response, null, e);
////        }
////    }
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String[] excludePath = {"/auth/signup", "/auth/login", "/auth/reissue", "/error"};
//        String path = request.getRequestURI();
//        return Arrays.stream(excludePath).anyMatch(path::startsWith);
//    }
//}
