package com.sparta.codechef.domain.elastic.repository;

import com.sparta.codechef.domain.elastic.document.BoardDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface BoardDocumentRepository extends ElasticsearchRepository<BoardDocument, String>, BoardDocumentRepositoryCustom {

        BoardDocument findByBoardId(Long boardId);
}
