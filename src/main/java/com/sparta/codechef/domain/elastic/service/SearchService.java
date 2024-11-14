package com.sparta.codechef.domain.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.sparta.codechef.common.enums.Framework;
import com.sparta.codechef.common.enums.Language;
import com.sparta.codechef.domain.elastic.document.BoardDocument;
import com.sparta.codechef.domain.elastic.dto.TopTenSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequestMapping
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient elasticsearchClient;

    // 제목으로 검색
    public Page<BoardDocument> searchByTitle(String title, Pageable pageable) throws IOException {
        log.info("Executing-search-by-title: {}", title);
        Query query = Query.of(q -> q.match(m -> m.field("title").query(title)));
        return executeSearch(query, pageable);
    }

    // 내용으로 검색
    public Page<BoardDocument> searchByContents(String contents, Pageable pageable) throws IOException {
        log.info("Executing search-by-contents: {}", contents);
        Query query = Query.of(q -> q.match(m -> m.field("contents").query(contents)));
        return executeSearch(query, pageable);
    }

    // 제목 + 내용으로 검색
    public Page<BoardDocument> searchByTitleAndContents(String keyword, Pageable pageable) throws IOException {
        log.info("Executing-search-by-keyword: {}", keyword);
        Query query = Query.of(q -> q
                .multiMatch(mm -> mm
                        .fields("title", "contents")
                        .query(keyword)
                ));
        return executeSearch(query, pageable);
    }

    // 프레임워크로 검색
    public Page<BoardDocument> searchByFramework(Framework framework, Pageable pageable) throws IOException {
        log.info("Executing-search-by-framework: {}", framework);
        Query query = Query.of(q -> q.term(t -> t.field("framework").value(framework.name())));
        return executeSearch(query, pageable);
    }

    // 언어로 검색
    public Page<BoardDocument> searchByLanguage(Language language, Pageable pageable) throws IOException {
        log.info("Executing-search-by-language: {}", language);
        Query query = Query.of(q -> q.term(t -> t.field("language").value(language.name())));
        return executeSearch(query, pageable);
    }

    // 댓글 내용으로 검색
    public Page<BoardDocument> searchByCommentContents(String commentContent, Pageable pageable) throws IOException {
        log.info("Executing-search-by-comment-contents: {}", commentContent);
        Query query = Query.of(q -> q.nested(n -> n
                .path("comments")
                .query(nq -> nq.match(m -> m.field("comments.content").query(commentContent)))
        ));
        return executeSearch(query, pageable);
    }

    // 공통 검색 실행 메서드
    private Page<BoardDocument> executeSearch(Query query, Pageable pageable) throws IOException {
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("boards") // 인덱스 이름을 지정
                .query(query)
                .from((int) pageable.getOffset()) // 시작 인덱스
                .size(pageable.getPageSize())
        );

        SearchResponse<BoardDocument> response = elasticsearchClient.search(searchRequest, BoardDocument.class);
        List<BoardDocument> documents = response.hits().hits().stream()
                .map(Hit::source)
                .toList();

        log.info("Search-executed-with {} results", documents.size());

        return new PageImpl<>(documents, pageable, response.hits().total().value());
    }

    public List<TopTenSearchResponse> getTop10ByField(String field) throws IOException {
        // Aggregation 설정
        Map<String, Aggregation> aggregations = new HashMap<>();
        aggregations.put("top_" + field, Aggregation.of(a -> a
                .terms(TermsAggregation.of(t -> t.field(field + ".keyword").size(10))) // .keyword 필드로 지정
        ));

        // SearchRequest 설정
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("logstash-logs-*")  // 날짜별로 인덱스를 포함할 수 있는 패턴
                .aggregations(aggregations)
                .build();

        // Elasticsearch에 요청 보내기
        SearchResponse<Void> searchResponse = elasticsearchClient.search(searchRequest, Void.class);

        // Aggregation 결과 처리 - 상위 10개 결과와 count 값을 함께 반환
        return searchResponse.aggregations().get("top_" + field).sterms().buckets().array().stream()
                .map(bucket -> new TopTenSearchResponse(bucket.key().stringValue(), bucket.docCount()))
                .toList(); // List<TopTenSearchResponse>로 반환
    }
}


