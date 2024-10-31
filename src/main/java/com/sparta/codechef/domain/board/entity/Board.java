package com.sparta.codechef.domain.board.entity;

import com.sparta.codechef.common.Timestamped;
import com.sparta.codechef.common.enums.Framework;
import com.sparta.codechef.common.enums.Language;
import com.sparta.codechef.common.enums.LanguageConverter;
import com.sparta.codechef.domain.comment.entity.Comment;
import com.sparta.codechef.domain.user.entity.User;
import jakarta.persistence.*;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "boards")
public class Board extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "contents", nullable = false, length = 2000)
    private String contents;

    @Convert(converter = LanguageConverter.class)
    @Column(name = "language", nullable = false)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(name = "framework", nullable = false)
    private Framework framework;

    private Long viewCount = 0L;

//    @Version // 낙관적 락 적용을 위한 버전 필드
//    private Long version;

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }


    public void BoardModify(String title,
                            String contents,
                            Language language,
                            Framework framework)  {
        if (title != null) this.title = title;
        if (contents != null) this.contents = contents;
        if (language != null) this.language = language;
        if (framework != null) this.framework = framework;
    }
}
