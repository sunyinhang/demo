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
    private final int waitMillis;//申请过程的等待毫秒数,大于零时才等待
    private boolean locked;//是否加锁

    /**
     * 构造函数, 默认不等待
     *
     * @param name 锁名称, 会自动添加全局前缀
     */
    public RedisLock(String name) {
        this(name, 0);
    }

    /**
     * 构造函数
     *
     * @param name       锁名称, 会自动添加全局前缀
     * @param waitMillis 申请过程的等待毫秒数,大于零时才等待
     */
    public RedisLock(String name, int waitMillis) {
        Assert.hasLength(name, "name can not be empty");
        this.name = name;
        this.key = name + "_LOCK";
        this.waitMillis = waitMillis;
    }

    /**
     * 获取锁名称
     *
     * @return 锁名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 加锁
     *
     * @param leaseTime 租借时间
     * @param unit      时间单位
     * @return 加锁成功返回 true, 否则返回 false
     */
    public boolean lock(int leaseTime, TimeUnit unit) {
        if (leaseTime <= 0)
            throw new ArgumentOutOfRangeException("leaseTime", "leaseTime must greater than zero");
        if (this.locked)
            throw new InvalidOperationException("unable to lock repeatedly");
        //加锁
        boolean shouldWait = this.waitMillis > 0;//应该等待
        long waitEnd = System.currentTimeMillis() + this.waitMillis;//等待截止时刻
        long leaseMillis = TimeoutUtils.toMillis(leaseTime, unit);//租借毫秒数
        do {
            switch (this.tryLock(leaseMillis)) {
                case SUCCESS:
                    return this.locked = true;
                case RETRY:
                    break;
                default:
                    if (shouldWait && System.currentTimeMillis() <= waitEnd) {
                        ThreadUtils.sleep(DEFAULT_SLEEP_MILLIS);
                        break;
                    }
                    return this.locked = false;
            }
        } while (true);
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

    /**
     * 尝试加锁
     *
     * @param leaseMillis 租借毫秒数
     * @return 加锁结果
     */
    private LockResult tryLock(long leaseMillis) {
        //尝试加锁,值为到期时间
        if (RedisUtils.setnx(this.key, System.currentTimeMillis() + leaseMillis))
            return LockResult.SUCCESS;
        //别人已释放,立即进入下次循环,尝试加锁
        Long expireEnd = RedisUtils.get(this.key, Long.class); //redis里的时间
        if (expireEnd == null)
            return LockResult.RETRY;
        //未过期等待
        if (System.currentTimeMillis() <= expireEnd)
            return LockResult.FAIL;
        //过期覆盖. 可能覆盖两次, 但是因为什么相差了很少的时间，所以可以接受.如果获取到的为 null 说明之前被删,正好加锁成功
        Long expireEndNow = RedisUtils.getset(this.key, System.currentTimeMillis() + leaseMillis, Long.class);
        if (expireEndNow == null || Objects.equals(expireEnd, expireEndNow))
            return LockResult.SUCCESS;
        return LockResult.FAIL;
    }

    //加锁结果
    private enum LockResult {
        //加锁失败,需要等待然后执行下次加锁
        FAIL,
        //加锁失败,无需等待立即执行下次加锁
        RETRY,
        //加锁成功
        SUCCESS
    }
}
