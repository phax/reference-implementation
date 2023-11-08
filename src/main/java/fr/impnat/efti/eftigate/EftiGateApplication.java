package fr.impnat.efti.eftigate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"fr.impnat.efti"}, exclude = {DataSourceAutoConfiguration.class})
@EntityScan("fr.impnat.efti")
@EnableJpaRepositories("fr.impnat.efti")
@ComponentScan({"fr.impnat.efti"})
@EnableAutoConfiguration
public class EftiGateApplication {

    public static void main(String[] args) {
        SpringApplication.run(EftiGateApplication.class, args);
    }

}
