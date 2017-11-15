package com.haiercash.spring.redis;

import com.bestvike.linq.exception.InvalidOperationException;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.threading.ThreadUtils;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁,非线程安全
 * Created by 许崇雷 on 2017-11-15.
 */
public final class RedisLock {
    private static final int DEFAULT_SLEEP_MILLIS = 100;//加锁等待时间片
    private final String name;//名称
    private final int acquireTimeout;//申请过程的超时时间
    private boolean locked;//是否加锁

    /**
     * 构造函数, 默认申请过程的超时时间为 5000 毫秒
     *
     * @param name 锁名称, 会自动添加全局前缀
     */
    public RedisLock(String name) {
        this(name, 5000);
    }

    /**
     * 构造函数
     *
     * @param name           锁名称, 会自动添加全局前缀
     * @param acquireTimeout 申请过程的超时时间, 毫秒
     */
    public RedisLock(String name, int acquireTimeout) {
        Assert.hasLength(name, "name can not be empty");
        if (acquireTimeout <= 0)
            throw new InvalidOperationException("acquireTimeout must greater than zero");
        this.name = name;
        this.acquireTimeout = acquireTimeout;
    }

    /**
     * 加锁
     *
     * @param timeout 最大持有时间
     * @param unit    单位
     * @return 加锁成功返回 true, 否则返回 false
     */
    public boolean lock(int timeout, TimeUnit unit) {
        long expireTimeout = TimeoutUtils.toMillis(timeout, unit);
        long acquireEnd = System.currentTimeMillis() + this.acquireTimeout;
        do {
            //尝试加锁,值为到期时间
            long expireEnd = System.currentTimeMillis() + expireTimeout;
            String expireEndStr = String.valueOf(expireEnd);
            if (RedisUtils.setnx(this.name, expireEndStr))
                return this.locked = true;

            //别人已释放,立即进入下次循环,尝试加锁
            String expireEndInRedis = RedisUtils.getString(this.name); //redis里的时间
            if (expireEndInRedis == null)
                continue;

            //未超时等待
            if (System.currentTimeMillis() < Long.parseLong(expireEndInRedis)) {
                ThreadUtils.sleep(DEFAULT_SLEEP_MILLIS);
                continue;
            }

            //这里值会被覆盖，但是因为什么相差了很少的时间，所以可以接受.如果获取到的为 null 说明之前被删,正好加锁成功
            String expireEndInRedisNow = RedisUtils.getsetString(this.name, expireEndStr);
            if (expireEndInRedisNow == null || StringUtils.equals(expireEndInRedis, expireEndInRedisNow))
                return this.locked = true;
            ThreadUtils.sleep(DEFAULT_SLEEP_MILLIS);
        } while (System.currentTimeMillis() < acquireEnd);

        return false;
    }

    /**
     * 解锁
     */
    public void unlock() {
        if (this.locked) {
            RedisUtils.del(this.name);
            this.locked = false;
        }
    }
}
