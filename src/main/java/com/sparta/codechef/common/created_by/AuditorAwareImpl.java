//package com.sparta.codechef.common.created_by;
//
//import com.sparta.codechef.common.ErrorStatus;
//import com.sparta.codechef.common.exception.ApiException;
//import com.sparta.springtrello.domain.auth.dto.AuthUser;
//import org.springframework.data.domain.AuditorAware;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.util.Optional;

//public class AuditorAwareImpl implements AuditorAware<AuthUser> {
//    @Override
//    public Optional<AuthUser> getCurrentAuditor() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new ApiException(ErrorStatus.UNAUTHORIZED_USER);
//        }
//
//       return Optional.of(authentication.getPrincipal());
//    }
//}
