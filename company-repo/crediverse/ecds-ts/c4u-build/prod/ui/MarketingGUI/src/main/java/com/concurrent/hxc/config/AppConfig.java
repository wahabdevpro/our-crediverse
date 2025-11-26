package com.concurrent.hxc.config;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.concurrent.hxc.dao.Organisation;
import com.concurrent.hxc.dao.OrganisationDAO;
import com.concurrent.hxc.dao.OrganisationDAOImpl;
import com.concurrent.hxc.dao.Visitor;
import com.concurrent.hxc.dao.VisitorDAO;
import com.concurrent.hxc.dao.VisitorDAOImpl;

@EnableWebMvc
@EnableTransactionManagement
@ComponentScan(basePackages = "com.concurrent.hxc.controllers")
@Configuration
public class AppConfig extends WebMvcConfigurerAdapter
{
	
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer)
	{
		// Enable the configurer
		configurer.enable();
	}

	// Indicate which directory to look under
	@Bean
	public InternalResourceViewResolver getInternalResourceViewResolver()
	{
		// Create the internal resource view resolver
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		
		// Set the directory to look under
		resolver.setPrefix("/WEB-INF/jsp/");
		
		// Set the extension of the files
		resolver.setSuffix(".jsp");
		
		// Return the resolver
		return resolver;
	}

	// Gets the connection to the database
	@Bean(name = "dataSource")
	public DataSource getDataSource()
	{
		// Create a basic data source
		BasicDataSource dataSource = new BasicDataSource();

		// Set the database information
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/demolab");
		dataSource.setUsername("root");
		dataSource.setPassword("ussdgw");

		// Return the data source
		return dataSource;
	}

	// Gets a session with the database
	@Autowired
	@Bean(name = "sessionFactory")
	public SessionFactory getSessionFactory(DataSource dataSource)
	{
		// Create the session builder
		LocalSessionFactoryBuilder sessionBuilder = new LocalSessionFactoryBuilder(dataSource);

		// Add the tables
		sessionBuilder.addAnnotatedClasses(Organisation.class, Visitor.class);
		
		// Set the hibernate properties
		sessionBuilder.setProperty("hibernate.show_sql", "true");
		sessionBuilder.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
		sessionBuilder.setProperty("hibernate.hbm2ddl.auto", "validate");

		// Build the session factory
		return sessionBuilder.buildSessionFactory();
	}

	// Gets a transaction manager for the transactions to the database
	@Autowired
	@Bean(name = "transactionManager")
	public HibernateTransactionManager getTransactionManager(SessionFactory sessionFactory)
	{
		// Create the transaction manager
		HibernateTransactionManager transactionManager = new HibernateTransactionManager(sessionFactory);

		// Return the transaction manager
		return transactionManager;
	}
	
	// Get an implementation of the organisation table/object
	@Autowired
	@Bean(name = "organisationDao")
	public OrganisationDAO getOrganisationDao(SessionFactory sessionFactory)
	{
		// Return the organisation DAO
		return new OrganisationDAOImpl(sessionFactory);
	}
	
	// Get an implementation of the visitor table/object
	@Autowired
	@Bean(name = "visitorDao")
	public VisitorDAO getVisitorDao(SessionFactory sessionFactory)
	{
		// Return the visitor DAO
		return new VisitorDAOImpl(sessionFactory);
	}

}
