package com.payplatform;

import com.haiercash.payplatform.Application;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * 依赖 Spring 框架测试
 *
 * @author 许崇雷
 * @date 2017/6/15
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class, properties = {"spring.config.location=classpath:/profile/", "spring.profiles.active=develop"})
@WebAppConfiguration
public class SpringTest {
}
