package org.example.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Immutable record representing a row in the `emails` table.
 */
public record Email(
        UUID id,
        String fromAddr,
        String toAddr,
        String subject,
        String bodyPlain,
        String bodyHtml,
        String strippedText,
        OffsetDateTime receivedAt,
        String direction
) {}
