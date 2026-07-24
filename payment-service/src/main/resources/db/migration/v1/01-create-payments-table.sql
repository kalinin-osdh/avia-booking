CREATE TABLE IF NOT EXISTS payments
(
    id             BIGSERIAL PRIMARY KEY,
    payment_number UUID           NOT NULL UNIQUE,
    booking_number UUID           NOT NULL UNIQUE,
    username       VARCHAR(20)    NOT NULL,
    price          NUMERIC(10, 2) NOT NULL,
    status         VARCHAR(20)    NOT NULL,
    created_at     TIMESTAMP      NOT NULL,
    expired_at     TIMESTAMP      NOT NULL
)