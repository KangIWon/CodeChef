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

    @Getter
    @NoArgsConstructor
    public static class getMe {
        private Long userId;
        private String email;
        private UserRole userRole;
        private String personalHistory;
        private String organization;
        private Integer warning;
        private Integer point;
        private Boolean isAttended;

        public getMe(Long userId, String email, UserRole userRole, String personalHistory, String organization, Integer warning, Integer point, Boolean isAttended) {
            this.userId = userId;
            this.email = email;
            this.userRole = userRole;
            this.personalHistory = personalHistory;
            this.organization = organization;
            this.warning = warning;
            this.point = point;
            this.isAttended = isAttended;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class getOther {
        private String personalHistory;
        private String organization;
        private Integer point;

        public getOther(String personalHistory, String organization, Integer point) {
            this.personalHistory = personalHistory;
            this.organization = organization;
            this.point = point;
        }
    }
}
