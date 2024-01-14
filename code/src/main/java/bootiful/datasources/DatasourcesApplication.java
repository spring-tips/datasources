package bootiful.datasources;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class DatasourcesApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatasourcesApplication.class, args);
	}

}

@Profile("jdbc-drivers")
@Configuration
class DriversAndDataSourceConfiguration {

	private void workWithDriver(Driver driver) throws Exception {
		System.out.println("found " + driver.toString());

		var connection = driver.connect("jdbc:h2:mem:springtips", new Properties());

		var driverManagerDataSource = new SimpleDriverDataSource(
				driver, "jdbc:h2:mem:springtips",
				new Properties());

	}

	@Bean
	ApplicationRunner driverEnumeratingApplicationRunner() {
		return args -> {

//			var h2DriverClass = org.h2.Driver.class;

			DriverManager.getDrivers().asIterator().forEachRemaining(
					d -> {
						try {
							workWithDriver(d);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
			);


		};
	}

}

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
				.type(com.zaxxer.hikari.HikariDataSource.class)
				.driverClassName(connectionDetails.getDriverClassName())
				.url(connectionDetails.getJdbcUrl())
				.username(connectionDetails.getUsername())
				.password(connectionDetails.getPassword())
				.build();
	}


}

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

@Profile("routing")
@Configuration
class RoutingDataSourceConfiguration {

	static final ThreadLocal<String> CURRENT_USER_REGION = new ThreadLocal<>();

	@Bean
	ApplicationRunner regionalApplicationRunner(RegionRoutingDataSource dataSource) {
		return args -> {
			var jdbc = JdbcClient.create(dataSource);
			for (var regionName : Set.of("emea", "apac")) {
				CURRENT_USER_REGION.set(regionName);
				var result = jdbc
						.sql("select region_name from region_metadata")
						.query((rs, rowNum) -> rs.getString("region_name"))
						.single();
				System.out.println("for region " + regionName + " I got " + result);
			}

		};
	}


	static class RegionRoutingDataSource extends AbstractRoutingDataSource {

		@SuppressWarnings("unchecked")
		RegionRoutingDataSource(Map<String, DataSource> dbs) {
			super();
			var mapOfObjectToObject = new HashMap<>();
			mapOfObjectToObject.putAll(dbs);
			this.setTargetDataSources(mapOfObjectToObject);
		}

		@Override
		protected Object determineCurrentLookupKey() {
            return CURRENT_USER_REGION.get();
		}
	}


	private Map<String, Integer> lookup = new ConcurrentHashMap<>(
			Map.of("emea", 5435, "apac", 5434)
	);

	private DataSource createDataSourceForRegion(String region) {
		var url = "jdbc:postgresql://localhost:" + this.lookup.get(region) + "/postgres";
		var classloader = getClass().getClassLoader();
		return DataSourceBuilder
				.create(classloader)
				.type(com.zaxxer.hikari.HikariDataSource.class)
				.driverClassName(org.postgresql.Driver.class.getName())
				.url(url)
				.username("myuser")
				.password("secret")
				.build();
	}

	@Bean
	RegionRoutingDataSource joshsRoutingDataSource() {
		var geographicallyPartitionedDataSources = Map.of(
				"emea", createDataSourceForRegion("emea"),//
				"apac", createDataSourceForRegion("apac") //
		);

		return new RegionRoutingDataSource(geographicallyPartitionedDataSources);
	}


}

