package com.haiercash.payplatform.datasource.mybatis;

import com.bestvike.collection.CollectionUtils;
import com.bestvike.lang.StringUtils;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.Linq;
import com.bestvike.linq.util.Comparer;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.util.Comparator;
import java.util.Objects;

/**
 * Created by 许崇雷 on 2017-11-06.
 */
@Configuration
@EnableConfigurationProperties(MyBatisProperties.class)
public class MyBatisAutoConfiguration {
    private final MyBatisProperties properties;

    public MyBatisAutoConfiguration(MyBatisProperties properties) {
        this.properties = properties;
    }

    private static IEnumerable<Resource> getResources(ResourcePatternResolver resolver, String locationPattern) {
        try {
            return Linq.asEnumerable(resolver.getResources(locationPattern));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    SqlSessionFactory sqlSessionFactory(DataSource dataSource) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setTypeAliasesPackage(this.properties.getAliasesPackage());
        sqlSessionFactoryBean.setDialect(this.properties.getDialect());
        //添加XML目录
        try {
            if (CollectionUtils.isNotEmpty(this.properties.getMapperLocations())) {
                ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                Resource[] resources = Linq.asEnumerable(this.properties.getMapperLocations())
                        .selectMany(location -> getResources(resolver, location))
                        .orderBy(Resource::getFilename, new ResourceComparator())
                        .toArray(Resource.class);
                sqlSessionFactoryBean.setMapperLocations(resources);
            }
            SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBean.getObject();
            sqlSessionFactory.getConfiguration().setMapUnderscoreToCamelCase(this.properties.getUnderscoreToCamelCase());
            sqlSessionFactory.getConfiguration().setMapUnderscoreToCamelCaseForMap(this.properties.getUnderscoreToCamelCase());
            return sqlSessionFactory;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    private static class ResourceComparator implements Comparator<String> {
        private Comparator<String> comparer = Comparer.Default();

        @Override
        public int compare(String left, String right) {
            if (Objects.equals(left, right))
                return 0;
            else if (StringUtils.equals(left, "AbstractMapper.xml"))
                return -1;
            else if (StringUtils.equals(right, "AbstractMapper.xml"))
                return 1;
            else
                return comparer.compare(left, right);
        }
    }
}
