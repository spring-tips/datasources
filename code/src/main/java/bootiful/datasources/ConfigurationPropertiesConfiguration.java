package bootiful.datasources;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@Profile("configprops")
class ConfigurationPropertiesConfiguration {

    private static DataSource dataSource(DataSourceProperties dsp) {
        return DataSourceBuilder
                .create(ClassLoader.getSystemClassLoader())
                .url(dsp.determineUrl())
                .username(dsp.getUsername())
                .password(dsp.getPassword())
                .type(HikariDataSource.class)
                .driverClassName(org.postgresql.Driver.class.getName())
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "apac")
    DataSourceProperties apacDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "emea")
    DataSourceProperties emeaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    DataSource emeaDataSource(DataSourceProperties emeaDataSourceProperties) {
        return dataSource(emeaDataSourceProperties);
    }

    @Bean
    DataSource apacDataSource(DataSourceProperties apacDataSourceProperties) {
        return dataSource(apacDataSourceProperties);
    }

    @Bean
    ApplicationRunner dataSourceConfigurationPropertiesRunner(Map<String, DataSource> dataSourceMap) {
        return args -> dataSourceMap
                .forEach((key, db) -> System.out.println(key + '=' + JdbcClient.create(db).sql("select 1").query(Integer.class).single()));
    }

}
