package bootiful.datasources;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
