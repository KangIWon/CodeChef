package com.sparta.codechef.domain.attachment.entity;

import com.sparta.codechef.domain.board.entity.Board;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String s3Url;
    private String s3Key;
    private boolean isDeleted;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;


}
