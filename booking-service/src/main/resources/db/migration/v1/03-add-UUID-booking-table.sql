ALTER TABLE bookings
    ADD COLUMN booking_number UUID NOT NULL UNIQUE;