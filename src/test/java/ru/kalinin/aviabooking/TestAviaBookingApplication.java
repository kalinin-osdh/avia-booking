package ru.kalinin.aviabooking;

import org.springframework.boot.SpringApplication;

public class TestAviaBookingApplication {

    public static void main(String[] args) {
        SpringApplication.from(AviaBookingApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
