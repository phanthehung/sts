package com.example.demo;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.transaction.interceptor.DelegatingTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

import javax.sql.DataSource;
import java.lang.reflect.AnnotatedElement;

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
		dataSource.setDriverClassName(env.getRequiredProperty("db.driver"));
		dataSource.setJdbcUrl(env.getRequiredProperty("db.url"));
		dataSource.setUsername(env.getRequiredProperty("db.username"));
		dataSource.setPassword(env.getRequiredProperty("db.password"));
		dataSource.setMaximumPoolSize(Integer.parseInt(env.getRequiredProperty("db.maximum-pool-size")));
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
	@Primary
	public TransactionAttributeSource transactionAttributeSource() {
		return new AnnotationTransactionAttributeSource() {

			@Nullable
			protected TransactionAttribute determineTransactionAttribute(AnnotatedElement element) {
				TransactionAttribute ta = super.determineTransactionAttribute(element);
				if (ta == null) {
					return null;
				} else {
					return new DelegatingTransactionAttribute(ta) {
						@Override
						public boolean rollbackOn(Throwable ex) {
							return super.rollbackOn(ex) || ex instanceof Exception;
						}
					};
				}
			}
		};
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

		SQLDialect dialect = SQLDialect.valueOf("MYSQL");
		jooqConfiguration.set(dialect);

		return jooqConfiguration;
	}

	@Bean
	public DSLContext dsl() {
		return new DefaultDSLContext(configuration());
	}
}