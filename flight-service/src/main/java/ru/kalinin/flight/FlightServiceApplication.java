package ru.kalinin.flight;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@ComponentScan(basePackages = {
        "ru.kalinin"
})
public class FlightServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlightServiceApplication.class, args);
    }
}
