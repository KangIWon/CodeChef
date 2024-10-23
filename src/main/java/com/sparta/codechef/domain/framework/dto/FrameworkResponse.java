package com.sparta.codechef.domain.framework.dto;

import lombok.Getter;

@Getter
public class FrameworkResponse {

    private String name;


    public FrameworkResponse(String name)
    {
        this.name = name;
    }

}
