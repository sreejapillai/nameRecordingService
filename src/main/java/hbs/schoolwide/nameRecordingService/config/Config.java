package hbs.schoolwide.nameRecordingService.config;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Log4jConfigurer;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by spillai on 2/23/2017.
 */
@Configuration
@EnableWebMvc
@EnableTransactionManagement
@ComponentScan({"hbs.schoolwide.nameRecordingService.dto", "hbs.schoolwide.nameRecordingService.service", "hbs.schoolwide.nameRecordingService.controller"})
@MapperScan({"hbs.schoolwide.nameRecordingService.mapper"})
@ImportResource("classpath:/hbs/common/appnaccess/appnAccessClientConfig.xml")
@PropertySource(value = "classpath:applicationConfiguration.properties", ignoreResourceNotFound = false)
public class Config extends WebMvcConfigurerAdapter{

    //private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    String LOGGING_LOCATION = "loggingConfig.location";

    @Autowired
    private Environment env;

    public Config() {
        super();
    }

    @Bean
    public MethodInvokingFactoryBean log4jInit() {
        System.out.println("initializing logging from file " + env.getProperty(LOGGING_LOCATION));
        final MethodInvokingFactoryBean bean = new MethodInvokingFactoryBean();
        bean.setTargetClass(Log4jConfigurer.class);
        bean.setTargetMethod("initLogging");
        bean.setArguments(new Object[]{ env.getProperty(LOGGING_LOCATION) });
        return bean;
    }

    @Bean
    public ViewResolver viewResolver(){
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        return resolver;
    }

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver createMultipartResolver() {
        CommonsMultipartResolver resolver=new CommonsMultipartResolver();
        resolver.setDefaultEncoding("utf-8");
        return resolver;
    }

    @Bean
    public DataSource dataSource() throws Exception {
        Context ctx = new InitialContext();
        return (DataSource) ctx.lookup("java:/comp/env/jdbc/nameRecord");
    }

    @Bean
    public DataSourceTransactionManager transactionManager() throws Exception {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());

        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(resourcePatternResolver.getResources("classpath*:/mapper/**/*.xml"));

        Properties mbProps = new Properties();
        mbProps.put("lazyLoadingEnabled", true);
        mbProps.put("logImpl", "SLF4J");
        sessionFactory.setConfigurationProperties(mbProps);

        return sessionFactory;


    }
}
