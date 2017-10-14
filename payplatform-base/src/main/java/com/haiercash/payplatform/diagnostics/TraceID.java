package com.haiercash.payplatform.diagnostics;

import com.bestvike.collection.ThreadLocalHashPool;
import com.bestvike.lang.Convert;
import com.bestvike.lang.DateUtils;
import com.bestvike.lang.StringUtils;
import com.bestvike.net.HostInfo;
import com.haiercash.payplatform.filter.RequestContext;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by 许崇雷 on 2017-10-14.
 */
public final class TraceID {
    public static final String NAME = "TraceID";
    private static final String EMPTY = "not support tracing for internal calls.";
    private static final String DATE_FORMAT = "yyyyMMdd-HHmmss";
    private static final char[] IP_LAST_BIT_SEPARATOR = new char[]{'.'};
    private static final String IP_LAST_BIT;
    private static final DynamicStringProperty PROPERTY_APPLICATION_NAME = DynamicPropertyFactory.getInstance().getStringProperty("spring.application.name", StringUtils.EMPTY);
    private static String APPLICATION_NAME;

    static {
        String[] ips = HostInfo.getIpAddress();
        if (ips.length < 1) {
            IP_LAST_BIT = HostInfo.getHostName();
        } else {
            String ip = ips[0];
            String[] bits = StringUtils.split(ip, IP_LAST_BIT_SEPARATOR, true);
            IP_LAST_BIT = bits[bits.length - 1];
        }
    }

    private TraceID() {
    }

    /**
     * 生成调用链 ID
     *
     * @return 调用链 ID
     */
    public static String generate() {
        if (StringUtils.isEmpty(APPLICATION_NAME))
            APPLICATION_NAME = Convert.toString(PROPERTY_APPLICATION_NAME.get()).toUpperCase();
        StringBuilder builder = new StringBuilder(25);
        builder.append(DateUtils.toString(DateUtils.now(), DATE_FORMAT))//年月日时分秒毫秒
                .append("-")
                .append(APPLICATION_NAME)
                .append("(")
                .append(IP_LAST_BIT)//IP 地址最后一个数字 0-255
                .append(")-")
                .append(Sequence.getAndIncrement());//序号
        return builder.toString();
    }

    /**
     * 获取当前请求上下文的调用链 ID
     *
     * @return 获取当前请求上下文的调用链 ID
     */
    public static String current() {
        if (!RequestContext.exists())
            return EMPTY;
        HttpServletRequest request = RequestContext.get().getRequest();
        if (request == null)
            return EMPTY;
        String traceID = request.getHeader(NAME);
        return StringUtils.isEmpty(traceID) ? EMPTY : traceID;
    }


    //序列号
    private static class Sequence {
        private Sequence() {
        }

        private static final ThreadLocalHashPool<CurrentDate, AtomicLong> POOL = ThreadLocalHashPool.withInitial(a -> new AtomicLong(1));

        private static long getAndIncrement() {
            return POOL.get(new CurrentDate()).getAndIncrement();
        }
    }

    //当前日期-年月日
    @Data
    private static class CurrentDate {
        private int year;
        private int month;
        private int day;

        private CurrentDate() {
            Calendar now = Calendar.getInstance();
            this.year = now.get(Calendar.YEAR);
            this.month = now.get(Calendar.MONTH) + 1;
            this.day = now.get(Calendar.DAY_OF_MONTH);
        }
    }
}
