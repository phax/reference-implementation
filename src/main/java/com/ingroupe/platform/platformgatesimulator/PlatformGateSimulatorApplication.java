package com.ingroupe.platform.platformgatesimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@EnableAutoConfiguration
public class PlatformGateSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformGateSimulatorApplication.class, args);
    }

}
