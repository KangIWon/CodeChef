package com.sparta.codechef.domain.elastic.repository;

import com.sparta.codechef.common.enums.Framework;
import com.sparta.codechef.common.enums.Language;
import com.sparta.codechef.domain.elastic.document.BoardDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface BoardDocumentRepository extends ElasticsearchRepository<BoardDocument, String> {

    void deleteByBoardId(Long boardId);
    BoardDocument findByBoardId(Long boardId);
}
