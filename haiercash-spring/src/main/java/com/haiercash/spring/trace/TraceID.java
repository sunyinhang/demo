package com.haiercash.spring.trace;

import com.haiercash.core.lang.DateUtils;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.net.HostInfo;
import com.haiercash.spring.boot.ApplicationUtils;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 调用链 ID 生成器
 * Created by 许崇雷 on 2017-10-14.
 */
public final class TraceID {
    public static final String NAME = "TraceID";
    private static final String DATE_FORMAT = "yyyyMMdd-HHmmss";
    private static final char[] IP_LAST_BIT_SEPARATOR = new char[]{'.'};
    private static final String IP_LAST_BIT;
    private static final int LEN_APPLICATION_NAME = 3;
    private static final String APPLICATION_NAME;

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

    private TraceID() {
    }

    /**
     * 生成调用链 ID
     *
     * @return 调用链 ID
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    public static String generate() {
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
