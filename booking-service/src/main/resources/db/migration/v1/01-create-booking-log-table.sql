CREATE TABLE bookings
(
    id             BIGSERIAL PRIMARY KEY,
    username       VARCHAR(20)    NOT NULL,
    flight_number  VARCHAR(20)    NOT NULL,
    seat_number    VARCHAR(10)    NOT NULL,
    status         VARCHAR(20)    NOT NULL DEFAULT 'CREATED',
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);