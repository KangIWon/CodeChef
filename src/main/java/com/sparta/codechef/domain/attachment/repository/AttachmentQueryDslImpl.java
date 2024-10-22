package com.sparta.codechef.domain.attachment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AttachmentQueryDslImpl implements AttachmentQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;

}