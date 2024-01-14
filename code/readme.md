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

## what's in a datasource
* not all jdbc implementations are created equal: [types 1-4](https://en.wikipedia.org/wiki/JDBC_driver#:~:text=The%20JDBC%20type%201%20driver,calls%20into%20ODBC%20function%20calls.)
* akshtually the core interface is `Driver`, not `DataSource` (show `DriverManager.registerDriver`)

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
* and of course [you can use `JndiObjectFactoryBean`](https://github.com/joshlong/spring-boot-sample-tomcat-jndi/blob/master/src/main/java/sample/tomcat/jndi/SampleTomcatJndiApplication.java)
* all you care about in your business logic is that you're using a `DataSource`

## TransactionAwareDataSourceProxy
* should we cover this? JDBI maybe benefits from it? worth mentioning?

## AbstractRoutingDataSource
* you don't want to test the ARDS for its health as it is in fact a composite, so we have `management.health.db.ignore-routing-data-sources=(true|false)`

## LazyConnectionDataSourceProxy
* 