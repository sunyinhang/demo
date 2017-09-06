package com.haiercash.payplatform.common.config;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class InitListener implements ServletContextListener {
    protected Log logger = LogFactory.getLog(this.getClass());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext()).getAutowireCapableBeanFactory().autowireBean(this);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
