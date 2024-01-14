package bootiful.datasources;

import com.zaxxer.hikari.HikariDataSource;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Profile("lazy")
@Configuration
class LazyDataSourceProxyConfiguration {


    private static DataSource dataSource() {
        return DataSourceBuilder
                .create(ClassLoader.getSystemClassLoader())
                .url("jdbc:postgresql://localhost:5434/postgres")
                .username("myuser")
                .password("secret")
                .type(HikariDataSource.class)
                .driverClassName(org.postgresql.Driver.class.getName())
                .build();
    }


    @Bean(name = "lazyDataSource")
    LazyConnectionDataSourceProxy lazyConnectionDataSourceProxyDataSource() {
        return new LazyConnectionDataSourceProxy(this.dataSource());
    }

    @Bean
    DataSourceTransactionManager dataSourceTransactionManager(DataSource lazyDataSource) {
        return new DataSourceTransactionManager(lazyDataSource);
    }

    @Bean
    static BeanPostProcessor beanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

                if (bean instanceof DataSource dataSource) {
                    var pf = new ProxyFactoryBean();
                    pf.setInterfaces(dataSource.getClass().getInterfaces());
                    pf.setProxyTargetClass(true);
                    pf.setTarget(dataSource);
                    pf.addAdvice((MethodInterceptor) invocation -> {
                        if (invocation.getMethod().getName().equals("getConnection"))
                            System.out.println("getConnection");
                        return invocation.proceed();
                    });
                    return pf.getObject();
                }

                return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
            }
        };
    }

    @Transactional
    static class TransactionalService {

        private final JdbcClient db;

        TransactionalService(DataSource db) {
            this.db = JdbcClient.create(db);
        }

        void op() {
            System.out.println("doing something; got " + this.db.sql("select 1").query(Integer.class).single());
        }

        void noop() {
            System.out.println("doing nothing");
        }

    }

    @Bean
    TransactionalService transactionalService(DataSource dataSource) {
        return new TransactionalService(dataSource);
    }

    @Bean
    ApplicationRunner lazyRunner(TransactionalService service) {
        return args -> {
            service.noop();
            service.op();
        };
    }

}
