package bootiful.datasources;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.Map;

@SpringBootApplication
public class DatasourcesApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatasourcesApplication.class, args);
	}

}


@Configuration
class DefaultDataSourceConfigurationRunners {

	static class ConnectingApplicationRunner implements ApplicationRunner {

		private final DataSource dataSource;

		private final JdbcClient jdbc;

		ConnectingApplicationRunner(DataSource dataSource) {
			this.dataSource = dataSource;
			this.jdbc = JdbcClient.create(this.dataSource);
		}

		@Override
		public void run(ApplicationArguments args) throws Exception {
			var jdbc = JdbcClient.create(this.dataSource);
			var result = jdbc.sql("select 1").query(Long.class).single();
			System.out.println(this.dataSource.toString());
			System.out.println("result: " + result);
		}
	}

	static class DataSourceEnumeratingApplicationRunner implements ApplicationRunner {

		private final Map<String, DataSource> dataSourceMap;

		DataSourceEnumeratingApplicationRunner(Map<String, DataSource> dataSourceMap) {
			this.dataSourceMap = dataSourceMap;
		}

		@Override
		public void run(ApplicationArguments args) throws Exception {
			Assert.state(!this.dataSourceMap.isEmpty(),
					"there should be at least one " + DataSource.class.getName());
			for (var k : this.dataSourceMap.keySet())
				System.out.println(k + '=' + this.dataSourceMap.get(k));

		}
	}

	@Bean
	ConnectingApplicationRunner applicationRunner(DataSource dataSource) {
		return new ConnectingApplicationRunner(dataSource);
	}

	@Bean
	DataSourceEnumeratingApplicationRunner dataSourceEnumeratingApplicationRunner(
			Map<String, DataSource> dataSourceMap) {
		return new DataSourceEnumeratingApplicationRunner(dataSourceMap);
	}

}

@Profile("java")
@Configuration
class JavaDataSourceConfiguration {

	@Bean
	DriverManagerDataSource dataSource() {
		return new DriverManagerDataSource("jdbc:h2:mem:springtips");
	}

	// todo mention things like spring batch's `@BatchDataSource`
	@Primary
	@Bean
	DataSource embeddedDataSource() {
		return new EmbeddedDatabaseBuilder()
				.setType(EmbeddedDatabaseType.H2)
				.build();
	}

	@Bean
	HikariDataSource hikariDataSource(JdbcConnectionDetails connectionDetails) {
		var classloader = getClass().getClassLoader();
		return DataSourceBuilder
				.create(classloader)
				.type(com.zaxxer.hikari.HikariDataSource.class)
				.driverClassName(connectionDetails.getDriverClassName())
				.url(connectionDetails.getJdbcUrl())
				.username(connectionDetails.getUsername())
				.password(connectionDetails.getPassword())
				.build();


	}


}