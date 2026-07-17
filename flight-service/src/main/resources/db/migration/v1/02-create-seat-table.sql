CREATE TABLE seats (
                       id            BIGSERIAL PRIMARY KEY,
                       flight_id     BIGINT NOT NULL REFERENCES flights(id) ON DELETE CASCADE,
                       seat_number   VARCHAR(10) NOT NULL,
                       status        VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
                       price         NUMERIC(10, 2) NOT NULL,

                       CONSTRAINT uq_seat_per_flight UNIQUE (flight_id, seat_number)
);