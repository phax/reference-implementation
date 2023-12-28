package com.ingroupe.efti.metadataregistry.config;

import com.ingroupe.efti.metadataregistry.entity.MetadataEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        basePackages = {"com.ingroupe.efti.metadataregistry.repository"},
        entityManagerFactoryRef = "metadataEntityManagerFactory",
        transactionManagerRef = "metadataTransactionManager"
)
public class MetadataJpaConfiguration {


    @Value("${spring.jpa.properties.hibernate.metadata_schema}")
    private String schema;

    @Bean
    @ConfigurationProperties("spring.datasource.metadata")
    public DataSourceProperties metadataDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource metadataDataSource() {
        return metadataDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean metadataEntityManagerFactory(
            @Qualifier("metadataDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        return builder.dataSource(dataSource)
                .packages(MetadataEntity.class)
                .properties(jpaProperties())
                .build();
    }

    private Map<String, Object> jpaProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("spring.datasource.schema", schema);
        return props;
    }

    @Bean
    public PlatformTransactionManager metadataTransactionManager(
            @Qualifier("metadataEntityManagerFactory") LocalContainerEntityManagerFactoryBean metadataEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(metadataEntityManagerFactory.getObject()));
    }
}
