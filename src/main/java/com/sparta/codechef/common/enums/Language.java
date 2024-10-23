package com.sparta.codechef.common.enums;


import lombok.Getter;

@Getter
public enum Language {
    C,          // C 언어
    CPP, // C++ 언어
    C_SHARP,    // C# 언어
    JAVA,       // Java 언어
    PYTHON,     // Python 언어
    JAVASCRIPT, // JavaScript 언어
    ETC;        // 기타 언어

    // 선택적으로 값을 출력하기 위한 메소드
    @Override
    public String toString() {
        switch(this) {
            case CPP:
                return "C++";
            case C_SHARP:
                return "C#";
            default:
                return name(); // 다른 값은 기본적으로 name()으로 리턴
        }
    }
}
