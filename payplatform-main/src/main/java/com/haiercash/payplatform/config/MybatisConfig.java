package com.haiercash.payplatform.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.StringUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.mapper.MapperScannerConfigurer;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;

@Configuration
public class MybatisConfig implements ApplicationContextAware {
    // @Autowired
    // DataSource dataSource;
    protected Log logger = LogFactory.getLog(this.getClass());

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    }

    @Value("${datasources.payplatform.datasource.url:}")
    private String url;
    @Value("${datasources.payplatform.datasource.username:}")
    private String username;
    @Value("${datasources.payplatform.datasource.password:}")
    private String password;
    @Value("${datasources.payplatform.datasource.driverClassName:}")
    private String driverClassName;
    @Value("${datasources.payplatform.datasource.initialSize:}")
    private Integer initialSize;
    @Value("${datasources.payplatform.datasource.minIdle:}")
    private Integer minIdle;
    @Value("${datasources.payplatform.datasource.maxActive:}")
    private Integer maxActive;
    @Value("${datasources.payplatform.datasource.maxWait:}")
    private Integer maxWait;
    @Value("${datasources.payplatform.datasource.timeBetweenEvictionRunsMillis:}")
    private Integer timeBetweenEvictionRunsMillis;
    @Value("${datasources.payplatform.datasource.minEvictableIdleTimeMillis:}")
    private Integer minEvictableIdleTimeMillis;
    @Value("${datasources.payplatform.datasource.validationQuery:}")
    private String validationQuery;
    @Value("${datasources.payplatform.datasource.testWhileIdle:}")
    private Boolean testWhileIdle;
    @Value("${datasources.payplatform.datasource.testOnBorrow:}")
    private Boolean testOnBorrow;
    @Value("${datasources.payplatform.datasource.testOnReturn:}")
    private Boolean testOnReturn;
    @Value("${datasources.payplatform.datasource.poolPreparedStatements:}")
    private Boolean poolPreparedStatements;
    @Value("${datasources.payplatform.datasource.filters:}")
    private String filters;
    @Value("${datasources.payplatform.datasource.connectionProperties:}")
    private String connectionProperties;

    @Bean(name="dataSource")
    //@ConfigurationProperties(prefix = "datasources.payplatform.datasource")
    @Primary
    public DataSource dataSource() {
        // return DataSourceBuilder.create().build();
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);//用户名
        dataSource.setPassword(password);//密码
        dataSource.setDriverClassName(driverClassName);
        if (initialSize != null) {
            dataSource.setInitialSize(initialSize);
        }
        if (minIdle != null) {
            dataSource.setMinIdle(minIdle);
        }
        if (maxActive != null) {
            dataSource.setMaxActive(maxActive);
        }
        if (maxWait != null) {
            dataSource.setMaxWait(maxWait);
        }
        if (timeBetweenEvictionRunsMillis != null) {
            dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        }
        if (minEvictableIdleTimeMillis != null) {
            dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        }
        if (validationQuery != null) {
            dataSource.setValidationQuery(validationQuery);
        }
        if (testWhileIdle != null) {
            dataSource.setTestWhileIdle(testWhileIdle);
        }
        if (testOnBorrow != null) {
            dataSource.setTestOnBorrow(testOnBorrow);
        }
        if (testOnReturn != null) {
            dataSource.setTestOnReturn(testOnReturn);
        }
        if (poolPreparedStatements != null) {
            dataSource.setPoolPreparedStatements(poolPreparedStatements);
        }
        if (filters != null) {
            try {
                dataSource.setFilters(filters);
            } catch (SQLException e) {
                logger.warn(e);
            }
        }
        if (connectionProperties != null) {
            dataSource.setConnectionProperties(connectionProperties);
        }
        return dataSource;
    }

    @Value("${datasources.payplatform.mybatis.dialect}")
    private String dialect;
    @Value("${datasources.payplatform.mybatis.aliasesPackage}")
    private String aliasesPackage;
    @Value("${datasources.payplatform.mybatis.mapperLocations}")
    private String mapperLocations;
    @Value("${datasources.payplatform.mybatis.underscoreToCamelCase}")
    private Boolean underscoreToCamelCase;
    @Bean(name="sqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactoryBean() {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource());
        sqlSessionFactoryBean.setTypeAliasesPackage(aliasesPackage);
        sqlSessionFactoryBean.setDialect(dialect);
        //添加XML目录
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            if (!StringUtils.isEmpty(mapperLocations)) {
                String[] paths = mapperLocations.split(",");
                Resource[] resources = null;
                for (String path : paths) {
                    resources = (Resource[]) ArrayUtils.addAll(resources, resolver.getResources(path));
                }
                Arrays.sort(resources, new ResourceComparator());
                sqlSessionFactoryBean.setMapperLocations(resources);
            }
            // sqlSessionFactoryBean.setMapperLocations(resolver.getResources(mapperLocations));
            sqlSessionFactoryBean.getObject().getConfiguration().setMapUnderscoreToCamelCase(underscoreToCamelCase);
            sqlSessionFactoryBean.getObject().getConfiguration().setMapUnderscoreToCamelCaseForMap(underscoreToCamelCase);
//            sqlSessionFactoryBean.setConfigLocation(resolver.getResource("classpath:mybatis-config.xml"));
            return sqlSessionFactoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Bean(name="sqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    public class ResourceComparator implements Comparator<Resource> {
        @Override
        public int compare(Resource r1, Resource r2) {
            if (r1.getFilename().equals("AbstractMapper.xml")) {
                return -1;
            } else if (r2.getFilename().equals("AbstractMapper.xml")) {
                return 1;
            }
            return 0;
        }
    }

    @Configuration
    @AutoConfigureAfter(MybatisConfig.class)
    public static class MyBatisMapperScannerConfig implements ApplicationContextAware {
        private String basePackage;
        private Boolean underscoreToCamelCase;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            basePackage = applicationContext.getEnvironment().getProperty("datasources.payplatform.mybatis.basePackage");
            underscoreToCamelCase = Boolean.parseBoolean(applicationContext.getEnvironment().getProperty("datasources.payplatform.mybatis.underscoreToCamelCase"));
        }
        @Bean(name = "mapperScannerConfigurer")
        @Primary
        public MapperScannerConfigurer mapperScannerConfigurer() {
            MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
            mapperScannerConfigurer.setCamelhumpToUnderline(underscoreToCamelCase);
            mapperScannerConfigurer.setBasePackage(basePackage);
            mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
            return mapperScannerConfigurer;
        }
    }
}