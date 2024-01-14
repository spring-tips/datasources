package bootiful.datasources;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Properties;

@Profile("drivers")
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
