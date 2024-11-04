package com.sparta.codechef.domain.elastic.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sparta.codechef.common.enums.Framework;
import com.sparta.codechef.common.enums.Language;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "boards")
@Builder
public class BoardDocument {

    @Id
    private String id = UUID.randomUUID().toString();

    @Field(type = FieldType.Long, index = false, docValues = false)
    private Long boardId;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String contents;


    @Field(type = FieldType.Keyword)
    private Framework framework;

    @Field(type = FieldType.Keyword)
    private Language language;


}