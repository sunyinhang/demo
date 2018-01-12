package com.haiercash.spring.trace;

import com.haiercash.core.collection.iterator.CharSequenceIterable;
import com.haiercash.core.lang.CharUtils;
import com.haiercash.core.lang.DateUtils;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.net.HostInfo;
import com.haiercash.spring.boot.ApplicationUtils;
import com.haiercash.spring.context.RequestContext;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 调用链 Id 生成器
 * Created by 许崇雷 on 2017-10-14.
 */
public final class TraceId {
    public static final String NAME = "TraceId";
    public static final String NAME_HEADER = "Trace-Id";
    private static final String DATE_FORMAT = "yyyyMMdd-HHmmss";
    private static final char[] IP_LAST_BIT_SEPARATOR = new char[]{'.'};
    private static final String IP_LAST_BIT;
    private static final int LEN_APPLICATION_NAME = 3;
    private static final String APPLICATION_NAME;
    private static final int MAX_LENGTH = 50;

    static {
        //初始化 IP_LAST_BIT
        String[] ips = HostInfo.getIpAddress();
        if (ips.length < 1) {
            IP_LAST_BIT = HostInfo.getHostName();
        } else {
            String ip = ips[0];
            String[] bits = StringUtils.split(ip, IP_LAST_BIT_SEPARATOR, true);
            IP_LAST_BIT = bits[bits.length - 1];
        }
        //初始化 APPLICATION_NAME
        String appName = ApplicationUtils.getProperties().getName();
        APPLICATION_NAME = (appName.length() <= LEN_APPLICATION_NAME ? appName : appName.substring(0, LEN_APPLICATION_NAME)).toUpperCase();
    }

    private TraceId() {
    }

    /**
     * 生成调用链 Id. 优先从请求上下文中获取, 如果无效则重新生成
     *
     * @return 调用链 Id
     */
    public static String generate() {
        String traceId = RequestContext.exists() ? RequestContext.getRequest().getHeader(TraceId.NAME_HEADER) : null;
        return TraceId.verify(traceId) ? traceId : generateCore();
    }

    //生成调用链 Id
    @SuppressWarnings("StringBufferReplaceableByString")
    private static String generateCore() {
        StringBuilder builder = new StringBuilder(50);
        builder.append(DateUtils.toString(DateUtils.now(), DATE_FORMAT))//年月日时分秒毫秒
                .append("-")
                .append(APPLICATION_NAME)//程序名前三位
                .append("-")
                .append(IP_LAST_BIT)//IP 地址最后一个数字 0-255
                .append("-")
                .append(Sequence.getAndIncrement());//序号
        return builder.toString();
    }

    //验证调用链 Id 是否有效
    private static boolean verify(String traceId) {
        if (StringUtils.isEmpty(traceId) || traceId.length() > MAX_LENGTH)
            return false;
        for (Character ch : new CharSequenceIterable(traceId)) {
            if (CharUtils.isAsciiAlphanumeric(ch) || ch.equals('-') || ch.equals('_'))
                continue;
            return false;
        }
        return true;
    }

    //序列号
    private static class Sequence {
        private static final ReentrantLock lock = new ReentrantLock();
        private static volatile LocalDate lastDate;
        private static volatile AtomicLong counter = new AtomicLong(1L);

        private Sequence() {
        }

        private static long getAndIncrement() {
            lock.lock();
            try {
                LocalDate now = LocalDate.now();
                if (!now.equals(lastDate)) {
                    lastDate = now;
                    counter.set(1L);
                }
                return counter.getAndIncrement();
            } finally {
                lock.unlock();
            }
        }
    }
}
