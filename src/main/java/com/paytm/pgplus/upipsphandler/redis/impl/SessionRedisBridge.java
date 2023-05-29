package com.paytm.pgplus.upipsphandler.redis.impl;

import com.paytm.pgplus.redis.IRedisBridge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("sessionRedisBridge")
public class SessionRedisBridge implements IRedisBridge {
    @Autowired
    SessionRedisBridgeWorker redisBridgeWorker;

    public SessionRedisBridge() {
    }

    public void set(String key, Object value, long ttl) {
        this.redisBridgeWorker.set(key, value, ttl);
    }

    public void setAsync(String key, Object value, long ttl) {
        throw new UnsupportedOperationException("Not supported in SessionRedisBridge");
    }

    public Object get(String key) {
        return this.redisBridgeWorker.get(key);
    }

    public void del(String key) {
        this.redisBridgeWorker.del(key);
    }

    public void hset(String key, String field, Object value, long ttlInSeconds) {
        this.redisBridgeWorker.hset(key, field, value, ttlInSeconds);
    }

    public Object hget(String key, String field) {
        return this.redisBridgeWorker.hget(key, field);
    }

    public void hdel(String key, String field) {
        this.redisBridgeWorker.hdel(key, field);
    }

    public boolean setnx(String key, Object value, long ttlInSeconds) {
        return this.redisBridgeWorker.setnx(key, value, ttlInSeconds);
    }

    public boolean expire(String key, long ttlInSeconds) {
        return this.redisBridgeWorker.expire(key, ttlInSeconds);
    }

    public boolean isExist(String key) {
        return this.redisBridgeWorker.isExist(key);
    }

    public Object increment(String key) {
        return this.redisBridgeWorker.increment(key);
    }

    public boolean hsetIfExist(String key, String field, Object value) {
        return this.redisBridgeWorker.hsetIfExist(key, field, value);
    }

    public Long ttl(String key) {
        return this.redisBridgeWorker.ttl(key);
    }

    public void setAsyncWithHighTtlInSeconds(String key, Object value, long ttl) {
        throw new UnsupportedOperationException("Not supported in sessionRedisBridge");
    }
}

