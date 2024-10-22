package com.sparta.codechef.common.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LanguageConverter implements AttributeConverter<Language, String> {

    // DB에 저장할 때 Language enum의 displayName을 저장
    @Override
    public String convertToDatabaseColumn(Language language) {
        return language != null ? language.getDisplayName() : null;  // displayName을 저장
    }

    // DB에서 읽어올 때 displayName을 그대로 반환
    @Override
    public Language convertToEntityAttribute(String dbData) {
        return dbData != null ? Language.fromDisplayName(dbData) : null;  // displayName을 그대로 사용
    }
}


