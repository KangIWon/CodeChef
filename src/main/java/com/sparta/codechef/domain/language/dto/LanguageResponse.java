package com.sparta.codechef.domain.language.dto;


import lombok.Getter;

@Getter
public class LanguageResponse {

    private String name;

    public LanguageResponse(String name) {
        this.name = name;
    }
}
