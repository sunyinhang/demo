package com.haiercash.spring.redis;

import com.bestvike.linq.exception.ArgumentOutOfRangeException;
import com.bestvike.linq.exception.InvalidOperationException;
import com.haiercash.core.threading.ThreadUtils;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁,非线程安全
 * Created by 许崇雷 on 2017-11-15.
 */
public final class RedisLock {
    private static final int DEFAULT_SLEEP_MILLIS = 100;//加锁等待时间片
    private final String name;//名称
    private final String key;//lock key
    private final int waitTime;//申请过程的等待时间,非正数不等待. 毫秒
    private boolean locked;//是否加锁

    /**
     * 构造函数, 默认申请过程的超时时间为 5000 毫秒
     *
     * @param name 锁名称, 会自动添加全局前缀
     */
    public RedisLock(String name) {
        this(name, 0);
    }

    /**
     * 构造函数
     *
     * @param name     锁名称, 会自动添加全局前缀
     * @param waitTime 申请过程的等待时间,非正数不等待. 毫秒
     */
    public RedisLock(String name, int waitTime) {
        Assert.hasLength(name, "name can not be empty");
        this.name = name;
        this.key = name + "_LOCK";
        this.waitTime = waitTime;
    }

    /**
     * 获取锁名称
     *
     * @return 锁名称
     */
    public String getName() {
        return name;
    }

    /**
     * 加锁
     *
     * @param leaseTime 最大持有时间
     * @param unit      时间单位
     * @return 加锁成功返回 true, 否则返回 false
     */
    public boolean lock(int leaseTime, TimeUnit unit) {
        if (leaseTime <= 0)
            throw new ArgumentOutOfRangeException("leaseTime", "leaseTime must greater than zero");
        if (this.locked)
            throw new InvalidOperationException("unable to lock repeatedly");
        //非等待
        long expireTimeout = TimeoutUtils.toMillis(leaseTime, unit);
        if (this.waitTime <= 0)
            return this.locked = RedisUtils.setnx(this.key, System.currentTimeMillis() + expireTimeout);
        //等待
        long waitEnd = System.currentTimeMillis() + this.waitTime;
        do {
            //尝试加锁,值为到期时间
            if (RedisUtils.setnx(this.key, System.currentTimeMillis() + expireTimeout))
                return this.locked = true;

            //别人已释放,立即进入下次循环,尝试加锁
            Long expireEndInRedis = RedisUtils.get(this.key, Long.class); //redis里的时间
            if (expireEndInRedis == null)
                continue;

            //未过期等待
            if (System.currentTimeMillis() <= expireEndInRedis) {
                ThreadUtils.sleep(DEFAULT_SLEEP_MILLIS);
                continue;
            }

            //过期覆盖. 可能覆盖两次, 但是因为什么相差了很少的时间，所以可以接受.如果获取到的为 null 说明之前被删,正好加锁成功
            Long expireEndInRedisNow = RedisUtils.getset(this.key, System.currentTimeMillis() + expireTimeout, Long.class);
            if (expireEndInRedisNow == null || Objects.equals(expireEndInRedis, expireEndInRedisNow))
                return this.locked = true;
            ThreadUtils.sleep(DEFAULT_SLEEP_MILLIS);
        } while (System.currentTimeMillis() <= waitEnd);
        //超过等待时间
        return this.locked = false;
    }

    /**
     * 解锁
     */
    public void unlock() {
        if (this.locked) {
            RedisUtils.del(this.key);
            this.locked = false;
        }
    }
}
