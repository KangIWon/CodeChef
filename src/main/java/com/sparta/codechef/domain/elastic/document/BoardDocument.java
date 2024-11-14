package com.sparta.codechef.domain.elastic.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sparta.codechef.common.enums.Framework;
import com.sparta.codechef.common.enums.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
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

    @Field(type = FieldType.Long, index = true, docValues = false)
    private Long boardId;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String contents;

    @Field(type = FieldType.Long)
    private Long viewCount;

    @Field(type = FieldType.Keyword)
    private Framework framework;

    @Field(type = FieldType.Keyword)
    private Language language;


    public void update(String title, String contents, Framework framework, Language language)
    {
        this.title = title;
        this.contents = contents;
        this.framework = framework;
        this.language = language;
    }


}
