package com.ingroupe.efti.eftigate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.ingroupe.efti"}, exclude = {DataSourceAutoConfiguration.class})
@EntityScan("com.ingroupe.efti")
@EnableJpaRepositories("com.ingroupe.efti")
@ComponentScan({"com.ingroupe.efti"})
@EnableAutoConfiguration
public class EftiGateApplication {

    public static void main(String[] args) {
        SpringApplication.run(EftiGateApplication.class, args);
    }

}
