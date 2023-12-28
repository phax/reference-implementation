package com.ingroupe.efti.eftigate.config;

import com.ingroupe.efti.eftigate.entity.ControlEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"com.ingroupe.efti.eftigate.repository"},
        entityManagerFactoryRef = "controlEntityManagerFactory",
        transactionManagerRef = "controlTransactionManager"
)
public class ControlJpaConfiguration {

    @Value("${spring.jpa.properties.hibernate.control_schema}")
    private String schema;

    @Bean
    @ConfigurationProperties("spring.datasource.control")
    public DataSourceProperties controlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource controlDataSource() {
        return controlDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean controlEntityManagerFactory(
            @Qualifier("controlDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(dataSource)
                .packages(ControlEntity.class)
                .properties(jpaProperties())
                .build();
    }

    @Bean
    public PlatformTransactionManager controlTransactionManager(
            @Qualifier("controlEntityManagerFactory") LocalContainerEntityManagerFactoryBean controlEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(controlEntityManagerFactory.getObject()));
    }

    private Map<String, Object> jpaProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("spring.datasource.schema", schema);
        return props;
    }

}
