package com.example.dbs;

import org.postgresql.Driver;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
@Profile("drivers")
class DriversConfiguration {

    @Bean
    ApplicationRunner driversRunner() {
        return args -> DriverManager.getDrivers()
                .asIterator().forEachRemaining(driver -> {


                    if (driver instanceof Driver) {
                        try {
                            System.out.println(driver.toString());

                            var props = new Properties();
                            props.put("user", "myuser");
                            props.put("password", "secret");

                            try (var connection = driver
                                    .connect("jdbc:postgresql://localhost:5434/postgres", props);
                                 var stmt = connection.createStatement()) {

                                stmt.execute("select region from region_metadata") ;
                                var rs = stmt.getResultSet() ;
                                while (rs.next()){
                                    var stringRegionName = rs.getString("region") ;
                                    System.out.println("region: " + stringRegionName);
                                }
                            }


                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }


                    }

                });
    }

}
