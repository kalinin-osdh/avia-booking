CREATE TABLE flights (
                         id                BIGSERIAL PRIMARY KEY,
                         flight_number     VARCHAR(20) NOT NULL UNIQUE,
                         departure_city    VARCHAR(100) NOT NULL,
                         arrival_city      VARCHAR(100) NOT NULL,
                         departure_time    TIMESTAMP NOT NULL,
                         arrival_time      TIMESTAMP NOT NULL,
                         total_seats       INTEGER NOT NULL,
                         available_seats   INTEGER NOT NULL
);