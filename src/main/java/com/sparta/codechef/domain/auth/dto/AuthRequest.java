package com.sparta.codechef.domain.auth.dto;

import com.sparta.codechef.common.enums.Framework;
import com.sparta.codechef.common.enums.Language;
import com.sparta.codechef.common.enums.Organization;
import com.sparta.codechef.common.enums.UserRole;
import lombok.*;

@Getter
public class AuthRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Signup {
        private String email;
        private String password;
        private UserRole userRole;
        private String personalHistory;
//        private String language;
//        private String framework;
        private String organization;
        private String adminToken; // 관리자 등록을 위한 필드
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Login {
        private String email;
        private String password;
        private String adminToken; // 관리자 로그인을 위한 필드
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CheckEmail {
        private String email;
    }

//    @Getter
//    @NoArgsConstructor
//    @AllArgsConstructor(access = AccessLevel.PRIVATE)
//    public static class Update {
//        private String language;
//        private Framework framework;
//        private Organization organization;
//    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ChangePassword {
        private String oldPassword;
        private String newPassword;
    }
}
