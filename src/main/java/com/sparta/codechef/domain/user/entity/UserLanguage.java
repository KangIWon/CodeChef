package com.sparta.codechef.domain.user.entity;

import com.sparta.codechef.domain.language.entity.Language;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@NoArgsConstructor
public class UserLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    private Language language;

    public UserLanguage(User user, Language language)
    {
        this.user = user;
        this.language = language;
    }
    public void update(Language language)
    {
        this.language = language;
    }
}
