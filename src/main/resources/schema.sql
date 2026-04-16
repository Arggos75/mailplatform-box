CREATE TABLE IF NOT EXISTS emails (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_addr     TEXT,
    to_addr       TEXT,
    subject       TEXT,
    body_plain    TEXT,
    body_html     TEXT,
    stripped_text TEXT,
    received_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    direction     TEXT
);

CREATE INDEX IF NOT EXISTS idx_emails_received_at ON emails (received_at DESC);
