package com.sparta.codechef.common.enums;


import lombok.Getter;

@Getter
public enum Language {
    C("C"),
    CPP("C++"),       // C++ 언어
    C_SHARP("C#"),    // C# 언어
    JAVA("Java"),
    PYTHON("Python"),
    JAVASCRIPT("JavaScript"),
    ETC("ETC");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    // displayName을 통해 enum을 찾는 메서드
    public static Language fromDisplayName(String displayName) {
        for (Language lang : Language.values()) {
            if (lang.getDisplayName().equalsIgnoreCase(displayName)) {
                return lang;
            }
        }
        throw new IllegalArgumentException("Unknown language: " + displayName);
    }
}

