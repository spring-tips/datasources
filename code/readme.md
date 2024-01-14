# DataSources

You know the drill. start.spring.io, add a database driver, `spring.datasource.url`, and then you're off to the races!
but what is actually happening behind the scenes?

* configuration 
* embedded datasource builder
* How to configure multiple of them (https://github.com/joshlong-attic/multiple-datasource-spring-apps)
  * [spring batch's `@BatchDataSource` ](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/autoconfigure/batch/BatchDataSource.html)
* How to configure connection pool properties
* Getting them from JNDI ? [(maybe show the andy wilkinson example w/ tomcat?)](https://github.com/joshlong/spring-boot-sample-tomcat-jndi/blob/master/src/main/java/sample/tomcat/jndi/SampleTomcatJndiApplication.java)
* `AbstractRoutingDataSource`
* `LazyConnectionDataSourceProxy`


## configuration 

show common configuration properties like 

```properties 
# section: configuration
spring.datasource.url=
spring.datasource.password=
spring.datasource.username=
spring.datasource.hikari.connection-test-query=select 1;
```

## actuator support 
* have `DataSourceHealthIndicator` which actually runs a validation query manually for us.

## java datasource
* show how to create a `javax.sql.DataSource` with, e.g., `DriverManagerDataSource` 
* and an embedded one with `EmbeddedDatabaseBuilder`
* 


## abstractroutingdatasource 

* you don't want to test the ARDS for its health as it is in fact a composite, so we have `management.health.db.ignore-routing-data-sources=(true|false)`