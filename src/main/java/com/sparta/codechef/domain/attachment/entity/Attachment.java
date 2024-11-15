package com.sparta.codechef.domain.attachment.entity;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.board.entity.Board;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Entity
@NoArgsConstructor
@Builder(builderClassName = "AttachmentBuilder")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cdnUrl;

    @Column(nullable = false, unique = true)
    private String s3Key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    public static class AttachmentBuilder {
        public AttachmentBuilder id(Long id) {
            throw new ApiException(ErrorStatus.ID_CANNOT_BE_SET);
        }
    }
}
