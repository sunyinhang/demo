package com.haiercash.spring.mail.bugreport;

import com.haiercash.core.http.HtmlUtils;
import com.haiercash.core.lang.DateUtils;
import com.haiercash.spring.boot.ApplicationProperties;
import com.haiercash.spring.boot.ApplicationUtils;
import com.haiercash.spring.context.RequestContext;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.mail.core.Mail;
import com.haiercash.spring.mail.core.MailType;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by 许崇雷 on 2017-11-21.
 */
public final class BugReportMailFactory {
    private static final String CONTENT_HEAD = "<head><meta charset=\"UTF-8\"><style>body{font-family:微软雅黑;}table{margin-top:10px;border-collapse:collapse;border:1px solid #aaa;}table th{vertical-align:baseline;padding:6px 15px 6px 6px;background-color:#d5d5d5;border:1px solid #aaa;word-break:keep-all;white-space:nowrap;text-align:left;}table td{vertical-align:text-top;padding:6px 15px 6px 6px;background-color:#efefef;border:1px solid #aaa;word-break:break-all;white-space:pre-wrap;}</style></head>";
    private final BugReportProperties properties;

    public BugReportMailFactory(BugReportProperties properties) {
        this.properties = properties;
    }

    public Mail createMail(BugReportLevel level, String content) {
        if (level == null)
            level = BugReportLevel.UNKNOWN;
        ApplicationProperties applicationProperties = ApplicationUtils.getProperties();
        Mail mail = new Mail();
        mail.setDisplay(String.format("%s (%s)", applicationProperties.getDescription(), applicationProperties.getName()));
        mail.setToList(this.properties.getTo());
        mail.setCcList(this.properties.getCc());
        mail.setBccList(this.properties.getBcc());
        mail.setSubject(this.properties.getSubject());
        StringBuilder builder = new StringBuilder(1000);
        builder.append(CONTENT_HEAD);
        builder.append("<body>");
        builder.append("<h1>").append(this.properties.getSubject()).append("</h1>");
        builder.append("<hr>");
        builder.append("<table>");
        builder.append("<tr><th>名称</th><th>说明</th></tr>");
        builder.append("<tr><td>级别</td><td>").append(level.getDescription()).append("</td></tr>");
        builder.append("<tr><td>实例</td><td>").append(applicationProperties.getDescription()).append(" (").append(applicationProperties.getName()).append(")").append("</td></tr>");
        builder.append("<tr><td>版本</td><td>").append(applicationProperties.getVersion()).append("</td></tr>");
        builder.append("<tr><td>时间</td><td>").append(DateUtils.nowString()).append("</td></tr>");
        builder.append("<tr><td>线程</td><td>").append(Thread.currentThread().getName()).append("</td></tr>");
        if (ThreadContext.exists()) {
            builder.append("<tr><td>追踪</td><td>").append(ThreadContext.getTraceID()).append("</td></tr>");
        }
        if (RequestContext.exists()) {
            HttpServletRequest request = RequestContext.getRequest();
            builder.append("<tr><td>接口</td><td>").append(request.getMethod().toUpperCase()).append(" ").append(request.getServletPath()).append("</td></tr>");
        }
        builder.append("<tr><td>详细</td><td>").append(HtmlUtils.htmlEscape(content)).append("</td></tr>");
        builder.append("</table>");
        builder.append("</body>");
        mail.setContent(builder.toString());
        mail.setMailType(MailType.HTML);
        return mail;
    }
}
