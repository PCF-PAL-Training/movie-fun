package org.superbiz.moviefun;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean getServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials getDatabaseService(@Value("${VCAP_SERVICES: NOT_SET}") String vcapService) {
        return new DatabaseServiceCredentials(vcapService);
    }

    @Bean
    public HikariDataSource albumDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql"));
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDataSource(dataSource);
        return hikariDataSource;
    }

    @Bean
    public HikariDataSource movieDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql"));
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDataSource(dataSource);
        return hikariDataSource;
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJPAVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        return hibernateJpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean albumsEntityManager(DataSource albumDataSource, HibernateJpaVendorAdapter hibernateJPAVendorAdapter) {

        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(albumDataSource);
        entityManagerFactoryBean.setJpaVendorAdapter(hibernateJPAVendorAdapter);
        entityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun.albums");
        entityManagerFactoryBean.setPersistenceUnitName("albums-unit");
        return entityManagerFactoryBean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean moviesEntityManager(DataSource movieDataSource, HibernateJpaVendorAdapter hibernateJPAVendorAdapter) {

        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(movieDataSource);
        entityManagerFactoryBean.setJpaVendorAdapter(hibernateJPAVendorAdapter);
        entityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun.movies");
        entityManagerFactoryBean.setPersistenceUnitName("movies-unit");
        return entityManagerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager albumsPlatformTransactionManager(EntityManagerFactory albumsEntityManager) {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager(albumsEntityManager);
        return jpaTransactionManager;
    }

    @Bean
    public PlatformTransactionManager moviesPlatformTransactionManager(EntityManagerFactory moviesEntityManager) {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager(moviesEntityManager);
        return jpaTransactionManager;
    }

    @Bean
    public TransactionOperations albumsTransactionOperations(PlatformTransactionManager albumsPlatformTransactionManager) {
        return new TransactionTemplate(albumsPlatformTransactionManager);
    }

    @Bean
    public TransactionOperations moviesTransactionOperations(PlatformTransactionManager moviesPlatformTransactionManager) {
        return new TransactionTemplate(moviesPlatformTransactionManager);
    }
}