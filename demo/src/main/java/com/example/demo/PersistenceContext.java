package com.example.demo;

import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

@Configuration
@ComponentScan 
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
public class PersistenceContext {

	@Autowired
	private Environment env;

	@Bean(destroyMethod = "close")
	public DataSource dataSource() {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setDriverClassName(env.getRequiredProperty("spring.datasource.driver"));
		dataSource.setJdbcUrl(env.getRequiredProperty("spring.datasource.url"));
		dataSource.setUsername(env.getRequiredProperty("spring.datasource.username"));
		dataSource.setPassword(env.getRequiredProperty("spring.datasource.password"));
		dataSource.setMaximumPoolSize(Integer.valueOf(env.getRequiredProperty("spring.datasource.maximum-pool-size")));
		return dataSource;
	}

	@Bean
	public LazyConnectionDataSourceProxy lazyConnectionDataSource() {
		return new LazyConnectionDataSourceProxy(dataSource());
	}

	@Bean
	public TransactionAwareDataSourceProxy transactionAwareDataSource() {
		return new TransactionAwareDataSourceProxy(lazyConnectionDataSource());
	}

	@Bean
	public DataSourceTransactionManager transactionManager() {
		return new DataSourceTransactionManager(lazyConnectionDataSource());
	}

	@Bean
	public DataSourceConnectionProvider connectionProvider() {
		return new DataSourceConnectionProvider(transactionAwareDataSource());
	}

	@Bean
	public JOOQToSpringExceptionTransformer jooqToSpringExceptionTransformer() {
		return new JOOQToSpringExceptionTransformer();
	}

	@Bean
	public DefaultConfiguration configuration() {
		DefaultConfiguration jooqConfiguration = new DefaultConfiguration();

		jooqConfiguration.set(connectionProvider());
		jooqConfiguration.set(new DefaultExecuteListenerProvider(jooqToSpringExceptionTransformer()));

//		SQLDialect dialect = SQLDialect.valueOf("MySql");
		jooqConfiguration.set(SQLDialect.MYSQL);

		return jooqConfiguration;
	}

	@Bean
	public DefaultDSLContext dslContext() {
		return new DefaultDSLContext(configuration());
	}
}