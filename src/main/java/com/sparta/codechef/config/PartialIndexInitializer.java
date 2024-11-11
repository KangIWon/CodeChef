package com.sparta.codechef.config;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PartialIndexInitializer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    @Transactional
    public void createPartialIndex() {
        String checkIndexExistsQuery = """
                SELECT COUNT(1)
                FROM information_schema.statistics
                WHERE table_schema = DATABASE()
                AND table_name = 'boards'
                AND index_name = 'boards_title_contents_partial_index'
                """;

        Integer indexExists = jdbcTemplate.queryForObject(checkIndexExistsQuery, Integer.class);

        if (indexExists != null && indexExists == 0) {
            String createIndexQuery = "CREATE INDEX boards_title_contents_partial_index ON boards (title(50), contents(200))";
            jdbcTemplate.execute(createIndexQuery);
        }
    }
}
