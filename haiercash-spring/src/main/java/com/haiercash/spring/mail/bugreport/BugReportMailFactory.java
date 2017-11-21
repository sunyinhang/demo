package com.haiercash.spring.mail.bugreport;

import com.haiercash.core.lang.DateUtils;
import com.haiercash.core.lang.Environment;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.context.RequestContext;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.mail.Mail;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by 许崇雷 on 2017-11-21.
 */
public final class BugReportMailFactory {
    private static final DynamicStringProperty PROPERTY_APPLICATION_NAME = DynamicPropertyFactory.getInstance().getStringProperty("spring.application.name", StringUtils.EMPTY);
    private static String APPLICATION_NAME;
    private final BugReportProperties properties;

    public BugReportMailFactory(BugReportProperties properties) {
        this.properties = properties;
    }

    public Mail createMail(String content) {
        Mail mail = new Mail();
        if (StringUtils.isEmpty(APPLICATION_NAME))
            APPLICATION_NAME = PROPERTY_APPLICATION_NAME.get();
        mail.setDisplay(APPLICATION_NAME);
        mail.setToList(this.properties.getTo());
        mail.setCcList(this.properties.getCc());
        mail.setBccList(this.properties.getBcc());
        mail.setSubject(this.properties.getSubject());
        StringBuilder builder = new StringBuilder();
        //构建内容
        Thread thread = Thread.currentThread();
        builder.append("时间: ").append(DateUtils.nowString()).append(Environment.NewLine);
        builder.append("线程: ").append(thread.getName()).append(Environment.NewLine);
        if (ThreadContext.exists()) {
            builder.append("追踪: ").append(ThreadContext.getTraceID()).append(Environment.NewLine);
        }
        if (RequestContext.exists()) {
            HttpServletRequest request = RequestContext.getRequest();
            builder.append("接口: ").append(request.getMethod().toUpperCase()).append(" ").append(request.getServletPath()).append(Environment.NewLine);
        }
        builder.append("详细: ").append(content);
        mail.setContent(builder.toString());
        return mail;
    }
}
