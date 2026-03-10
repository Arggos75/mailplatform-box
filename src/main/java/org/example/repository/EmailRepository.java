package org.example.repository;

import org.example.model.Email;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

public interface EmailRepository {
    List<Email> findAll();
    Email findById(UUID id);
}

@Repository
class EmailRepositoryImpl implements EmailRepository {

    private final JdbcTemplate jdbc;

    EmailRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Minimal row mapper used for the list view (no body columns)
    private static final RowMapper<Email> LIST_MAPPER = (rs, rowNum) -> new Email(
            UUID.fromString(rs.getString("id")),
            rs.getString("from_addr"),
            rs.getString("to_addr"),
            rs.getString("subject"),
            null,   // bodyPlain not fetched in list query
            null,   // bodyHtml not fetched in list query
            null,   // strippedText not fetched in list query
            toOffsetDateTime(rs, "received_at"),
            rs.getString("direction")
    );

    // Full row mapper used for the detail view
    private static final RowMapper<Email> FULL_MAPPER = (rs, rowNum) -> new Email(
            UUID.fromString(rs.getString("id")),
            rs.getString("from_addr"),
            rs.getString("to_addr"),
            rs.getString("subject"),
            rs.getString("body_plain"),
            rs.getString("body_html"),
            rs.getString("stripped_text"),
            toOffsetDateTime(rs, "received_at"),
            rs.getString("direction")
    );

    @Override
    public List<Email> findAll() {
        return jdbc.query(
                "SELECT id, from_addr, to_addr, subject, received_at, direction " +
                "FROM emails ORDER BY received_at DESC",
                LIST_MAPPER
        );
    }

    @Override
    public Email findById(UUID id) {
        return jdbc.queryForObject(
                "SELECT * FROM emails WHERE id = ?",
                FULL_MAPPER,
                id
        );
    }

    // Helper: converts a SQL TIMESTAMP column to OffsetDateTime (UTC).
    private static OffsetDateTime toOffsetDateTime(ResultSet rs, String col) throws SQLException {
        Timestamp ts = rs.getTimestamp(col);
        return ts == null ? null : ts.toInstant().atOffset(ZoneOffset.UTC);
    }
}
