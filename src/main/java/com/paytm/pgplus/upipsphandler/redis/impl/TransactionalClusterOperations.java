package com.paytm.pgplus.upipsphandler.redis.impl;

import com.paytm.pgplus.cache.redis.cluster.IRedisClusterClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
class TransactionalClusterOperations {
    @Autowired(
            required = false
    )
    @Qualifier("transactionalRedisClusterClientLettuceService")
    IRedisClusterClient redisClusterClient;

    TransactionalClusterOperations() {
    }

    void set(String key, Object value, long ttl) {
        this.redisClusterClient.set(key, value, ttl);
    }

    Object get(String key) {
        return this.redisClusterClient.get(key);
    }

    void del(String key) {
        this.redisClusterClient.delete(key);
    }

    void hset(String key, String field, Object value, long ttlInSeconds) {
        this.redisClusterClient.hset(key, field, value, ttlInSeconds);
    }

    Object hget(String key, String field) {
        return this.redisClusterClient.hget(key, field);
    }

    void hdel(String key, String field) {
        this.redisClusterClient.hdel(key, field);
    }

    boolean setnx(String key, Object value, long ttlInSeconds) {
        return this.redisClusterClient.setnx(key, value, ttlInSeconds);
    }

    boolean expire(String key, long ttlInSeconds) {
        return this.redisClusterClient.expire(key, ttlInSeconds);
    }

    boolean isExist(String key) {
        return this.redisClusterClient.isExist(key);
    }

    Object increment(String key) {
        return this.redisClusterClient.incr(key);
    }

    boolean hsetIfExist(String key, String field, Object value) {
        return this.redisClusterClient.hsetIfExist(key, field, value);
    }

    Long ttl(String key) {
        return this.redisClusterClient.ttl(key);
    }
}
