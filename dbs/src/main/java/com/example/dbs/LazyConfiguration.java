package com.example.dbs;

import com.zaxxer.hikari.HikariDataSource;
import org.aopalliance.intercept.MethodInterceptor;
import org.postgresql.Driver;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@EnableTransactionManagement
//@Profile("lazy")
@Configuration
class LazyConfiguration {

    @Bean
    TransactionalService transactionalService(DataSource db) {
        return new TransactionalService(db);
    }

    @Bean
    static BeanPostProcessor connectionBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

                if (bean instanceof LazyConnectionDataSourceProxy db) {
                    var pfb = new ProxyFactoryBean();
                    pfb.setTarget(db);
                    pfb.setProxyTargetClass(true);
                    pfb.setInterfaces(db.getClass().getInterfaces());
                    pfb.addAdvice((MethodInterceptor) invocation -> {
                        var result = invocation.proceed();
                        if (invocation.getMethod().getName().equals("getConnection")) {
                            System.out.println("getConnection");
                            System.out.println(result.getClass().toString());
                        }

                        return result;
                    });
                    return pfb.getObject();
                }

                return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
            }
        };
    }


    @Bean
    PlatformTransactionManager platformTransactionManager(DataSource db) {
        return new DataSourceTransactionManager(db);
    }

    @Bean
    ApplicationRunner transactionalServiceRunner(TransactionalService ts) {
        return args -> {
            ts.op();
            ts.noop();
        };
    }

    @Transactional
    static class TransactionalService {

        final JdbcClient db;

        TransactionalService(DataSource db) {
            this.db = JdbcClient.create(db);
        }

        void op() {
            System.out.println("operation: " + this.db.sql("select region from region_metadata")
                    .query(String.class).single());
        }

        void noop() {
            System.out.println("no operation: ");
        }

    }


    @Bean
    @Primary
    LazyConnectionDataSourceProxy lazyConnectionDataSourceProxy(DataSource dataSource) {
        return new LazyConnectionDataSourceProxy(dataSource);
    }

    @Bean
    DataSource dataSource() {
        return DbsApplication.createDataSource(
                Driver.class,
                HikariDataSource.class,
                "jdbc:postgresql://localhost:5435/postgres",
                "myuser",
                "secret",
                ClassLoader.getSystemClassLoader()
        );
    }


}
