package com.sparta.codechef.domain.elastic.service;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.sparta.codechef.common.enums.Framework;
import com.sparta.codechef.common.enums.Language;
import com.sparta.codechef.domain.elastic.document.BoardDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import java.io.IOException;
import java.util.List;


@Service
@RequestMapping
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchOperations elasticsearchOperations;
    // 제목으로 검색
    public Page<BoardDocument> searchByTitle(String title, Pageable pageable) throws IOException {
        Query query = Query.of(q -> q.match(m -> m.field("title").query(title)));
        return executeSearch(query, pageable);
    }

    // 내용으로 검색
    public Page<BoardDocument> searchByContents(String contents, Pageable pageable) throws IOException {
        Query query = Query.of(q -> q.match(m -> m.field("contents").query(contents)));
        return executeSearch(query, pageable);
    }

    // 제목 + 내용으로 검색
    public Page<BoardDocument> searchByTitleAndContents(String keyword, Pageable pageable) throws IOException {
        Query query = Query.of(q -> q.bool(b -> b
                .should(sh -> sh.match(m -> m.field("title").query(keyword)))
                .should(sh -> sh.match(m -> m.field("contents").query(keyword)))
        ));
        return executeSearch(query, pageable);
    }

    // 프레임워크로 검색
    public Page<BoardDocument> searchByFramework(Framework framework, Pageable pageable) throws IOException {
        Query query = Query.of(q -> q.term(t -> t.field("framework").value(framework.name())));
        return executeSearch(query, pageable);
    }

    // 언어로 검색
    public Page<BoardDocument> searchByLanguage(Language language, Pageable pageable) throws IOException {
        Query query = Query.of(q -> q.term(t -> t.field("language").value(language.name())));
        return executeSearch(query, pageable);
    }

    // 댓글 내용으로 검색
    public Page<BoardDocument> searchByCommentContents(String commentContent, Pageable pageable) throws IOException {
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
            List<BoardDocument> documents =  response.hits().hits().stream()
                    .map(Hit::source)
                    .toList();

            return new PageImpl<>(documents, pageable, response.hits().total().value());
        }

}
