package bootiful.datasources;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Profile("java")
@Configuration
class JavaDataSourceConfiguration {


    private final ClassLoader classLoader = ClassLoader.getPlatformClassLoader();

    @Bean
    DriverManagerDataSource dataSource() {
        return new DriverManagerDataSource("jdbc:h2:mem:springtips");
    }

    // todo spring batch's `@BatchDataSource`
    @Primary
    @Bean
    DataSource embeddedDataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }

    @Bean
    HikariDataSource hikariDataSource(JdbcConnectionDetails connectionDetails) {
        return DataSourceBuilder
                .create(this.classLoader)
                .type(HikariDataSource.class)
                .driverClassName(connectionDetails.getDriverClassName())
                .url(connectionDetails.getJdbcUrl())
                .username(connectionDetails.getUsername())
                .password(connectionDetails.getPassword())
                .build();
    }


}
