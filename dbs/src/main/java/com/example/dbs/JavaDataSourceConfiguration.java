package com.example.dbs;

import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.Driver;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.Map;

@Profile("javaconfig")
@Configuration
class JavaDataSourceConfiguration {

    @Bean
    DataSource apacDataSource(DataSourceProperties apacDataSourceProperties) {
        return DbsApplication.createDataSource(
                Driver.class,
                HikariDataSource.class,
                apacDataSourceProperties.determineUrl(),
                apacDataSourceProperties.determineUsername(),
                apacDataSourceProperties.determinePassword(),
                ClassLoader.getSystemClassLoader()
        );
    }

    @Bean
    DataSource emeaDataSource(DataSourceProperties emeaDataSourceProperties) {
        return DbsApplication.createDataSource(
                Driver.class,
                HikariDataSource.class,
                emeaDataSourceProperties.determineUrl(),
                emeaDataSourceProperties.determineUsername(),
                emeaDataSourceProperties.determinePassword(),
                ClassLoader.getSystemClassLoader()
        );
    }

    @Bean
    @ConfigurationProperties(prefix = "emea")
    DataSourceProperties emeaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "apac")
    DataSourceProperties apacDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    ApplicationRunner javaConfigDbRunner(Map<String, DataSource> dbs) {
        return args -> dbs.forEach((key, db) -> System.out.println(key + '=' + db));
    }
}
