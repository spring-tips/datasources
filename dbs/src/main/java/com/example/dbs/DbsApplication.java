package com.example.dbs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.sql.Driver;

@SpringBootApplication
public class DbsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbsApplication.class, args);
    }

    static DataSource createDataSource(
            Class<? extends Driver> driverClassName,
            Class<? extends DataSource> type,
            String url, String username, String password,
            ClassLoader classLoader) {
        return DataSourceBuilder
                .create(classLoader)
                .type(type)
                .driverClassName(driverClassName.getName())
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
}

/*

	@SuppressWarnings("unchecked")
 DataSource createDataSource(JdbcConnectionDetails connectionDetails, Class<? extends DataSource> type,
			ClassLoader classLoader) {
		return (T) DataSourceBuilder.create(classLoader)
			.type(type)
			.driverClassName(connectionDetails.getDriverClassName())
			.url(connectionDetails.getJdbcUrl())
			.username(connectionDetails.getUsername())
			.password(connectionDetails.getPassword())
			.build();
	}
*/