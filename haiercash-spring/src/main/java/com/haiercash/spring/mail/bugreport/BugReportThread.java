package com.haiercash.spring.mail.bugreport;

import com.haiercash.core.threading.ThreadUtils;
import com.haiercash.spring.mail.MailUtils;
import com.haiercash.spring.mail.core.Mail;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by 许崇雷 on 2017-11-21.
 */
public final class BugReportThread extends Thread {
    private final Log logger = LogFactory.getLog(BugReportThread.class);
    private final BugReportProperties properties;
    private final BlockingQueue<Mail> queue;
    private final int sendIntervalMillis;

    public BugReportThread(BugReportProperties properties) {
        this.properties = properties;
        this.queue = new LinkedBlockingQueue<>(this.properties.getQueueSize());//固定容量
        this.sendIntervalMillis = this.properties.getSendIntervalSeconds() * 1000;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (true) {
            try {
                Mail mail = this.queue.take();
                if (this.properties.getEnabled() != null && this.properties.getEnabled()) {
                    MailUtils.send(mail);
                    continue;
                }
                this.logger.warn("由于未启用 bug report 功能，跳过了发送邮件");
            } catch (Exception e) {
                this.logger.warn("bug report 发送邮件失败", e);
            } finally {
                ThreadUtils.sleep(this.sendIntervalMillis);
            }
        }
    }

    public void sendAsync(Mail mail) {
        try {
            if (this.properties.getEnabled() != null && this.properties.getEnabled()) {
                this.queue.offer(mail);
                return;
            }
            this.logger.warn("由于未启用 bug report 功能，跳过了入发送队列");
        } catch (Exception e) {
            this.logger.warn("bug report 入发送队列失败", e);
        }
    }
}
