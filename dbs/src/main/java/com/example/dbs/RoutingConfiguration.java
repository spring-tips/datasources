package com.example.dbs;

import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.Driver;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Profile("routing")
@Configuration
class RoutingConfiguration {

    private static final ThreadLocal<String> CURRENT_REGION =
            new ThreadLocal<>();

    private String regionNameFor(DataSource db) {
        return JdbcClient
                .create(db).sql("select region from region_metadata")
                .query(String.class).single();
    }

    @Bean
    ApplicationRunner regionAwareRunner(DataSource dataSource) {
        return args -> {

            CURRENT_REGION.set("emea");
            System.out.println(regionNameFor(dataSource));

            CURRENT_REGION.set("apac");
            System.out.println(regionNameFor(dataSource));
        };
    }

    @Bean
    @Primary
    LocaleAwareAbstractRoutingDataSource theOneDataSource(
            DataSource apacDataSource,
            DataSource emeaDataSource) {
        return new LocaleAwareAbstractRoutingDataSource(
                Map.of("emea", emeaDataSource, "apac", apacDataSource)
        );
    }

    public static class LocaleAwareAbstractRoutingDataSource
            extends AbstractRoutingDataSource {

        LocaleAwareAbstractRoutingDataSource(Map<String, DataSource> dbs) {
            var newMap = new HashMap<Object, Object>();
            dbs.forEach(newMap::put);
            this.setTargetDataSources(newMap);
        }

        @Override
        protected Object determineCurrentLookupKey() {
            return CURRENT_REGION.get();
        }
    }


    // two different databases
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


}
