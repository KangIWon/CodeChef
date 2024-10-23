package com.sparta.codechef.domain.auth.dto;

import com.sparta.codechef.common.enums.UserRole;
import lombok.*;

@Getter
@NoArgsConstructor
public class AuthResponse {

    @Getter
    @NoArgsConstructor
    public static class Signup {
        private Long userId;
        private String email;
        private String password;
        private UserRole userRole;
        private String personalHistory;
//        private String language;
//        private String framework;
        private String organization;

        public Signup(Long userId, String email, String password, UserRole userRole, String personalHistory, String organization) {//String language, String framework, String organization) {
            this.userId = userId;
            this.email = email;
            this.password = password;
            this.userRole = userRole;
            this.personalHistory = personalHistory;
//            this.language = language;
//            this.framework = framework;
            this.organization = organization;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class Login {
//        private String accessToken;
//        private String refreshToken;
        private String bearerToken;
        private Long userId;
        private String email;
        private String userRole;

        public Login(String bearerToken, Long userId, String email, String userRole) {//(String accessToken, String refreshToken, Long userId, String email, String userRole) {
//            this.accessToken = accessToken;
//            this.refreshToken = refreshToken;
            this.bearerToken = bearerToken;
            this.userId = userId;
            this.email = email;
            this.userRole = userRole;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class DuplicateCheck {
        private boolean isDuplicate;

        public DuplicateCheck(boolean isDuplicate) {
            this.isDuplicate = isDuplicate;
        }
    }

//    @Getter
//    @NoArgsConstructor
//    public static class Reissue {
//        private String newAccessToken;
//        private String newRefreshToken;
//
//        public Reissue(String newAccessToken, String newRefreshToken) {
//            this.newAccessToken = newAccessToken;
//            this.newRefreshToken = newRefreshToken;
//        }
//    }
}
