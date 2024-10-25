package com.sparta.codechef.domain.comment.entity;

import com.sparta.codechef.common.Timestamped;
import com.sparta.codechef.domain.board.entity.Board;
import com.sparta.codechef.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Comment extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Builder.Default
    private Boolean isAdopted = false;

    public void update(String content)
    {
        this.content = content;
    }
    public void isAdopted()
    {

            this.isAdopted = true;

    }


}
