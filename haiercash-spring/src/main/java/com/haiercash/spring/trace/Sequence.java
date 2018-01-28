package com.haiercash.spring.trace;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by 许崇雷 on 2018-01-27.
 */
public final class Sequence implements ISequence {
    private final ReentrantLock lock = new ReentrantLock();
    private volatile LocalDate lastDate;
    private volatile AtomicLong counter = new AtomicLong(1L);

    @Override
    public long getAndIncrement() {
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