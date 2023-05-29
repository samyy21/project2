package com.paytm.pgplus.upipsphandler.redis.impl;

import com.paytm.pgplus.upipsphandler.utils.AWSStatsDUtils;
import com.paytm.pgplus.util.RedisBridgeUtil;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
class TransactionalRedisBridgeWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(com.paytm.pgplus.upipsphandler.redis.impl.TransactionalRedisBridgeWorker.class);
    @Autowired
    TransactionalClusterOperations transactionalClusterOperations;
    @Autowired
    private AWSStatsDUtils statsDUtil;

    RedisBridgeUtil redisBridgeUtil = new RedisBridgeUtil();

    TransactionalRedisBridgeWorker() {
    }

    void set(String key, Object value, long ttl) {
        try {
            this.transactionalClusterOperations.set(key, value, ttl);
        } catch (CallNotPermittedException var6) {
            this.statsDUtil.pushExceptionToStatsD(500, var6.getClass().getSimpleName());
            LOGGER.error(MessageFormat.format("Error while performing operation on transactional cluster: {0}", var6.getMessage()));
        } catch (Exception var7) {
            this.statsDUtil.pushExceptionToStatsD(500, var7.getClass().getSimpleName());
            LOGGER.error("Exception while writing on transactional cluster: ", var7);
        }

    }

    Object get(String key) {
        Object value = null;
        if (StringUtils.isBlank(key)) {
            return null;
        } else {
            try {
                value = this.transactionalClusterOperations.get(key);
            } catch (CallNotPermittedException var4) {
                this.statsDUtil.pushExceptionToStatsD(500, var4.getClass().getSimpleName());
                LOGGER.error(MessageFormat.format("Error while performing operation on transactional cluster: {0}", var4.getMessage()));
            } catch (Exception var5) {
                this.statsDUtil.pushExceptionToStatsD(500, var5.getClass().getSimpleName());
                LOGGER.error("Exception while reading from transactional cluster: ", var5);
            }

            return value;
        }
    }

    void del(String key) {
        if (!StringUtils.isBlank(key)) {
            try {
                this.transactionalClusterOperations.del(key);
            } catch (CallNotPermittedException var3) {
                this.statsDUtil.pushExceptionToStatsD(500, var3.getClass().getSimpleName());
                LOGGER.error(MessageFormat.format("Error while performing operation on transactional cluster: {0}", var3.getMessage()));
            } catch (Exception var4) {
                this.statsDUtil.pushExceptionToStatsD(500, var4.getClass().getSimpleName());
                LOGGER.error("Exception while deleting from transactional cluster: ", var4);
            }

        }
    }

    void hset(String key, String field, Object value, long ttlInSeconds) {
        if (!StringUtils.isBlank(key) && !StringUtils.isBlank(field)) {
            try {
                this.transactionalClusterOperations.hset(key, field, value, ttlInSeconds);
            } catch (CallNotPermittedException var7) {
                this.statsDUtil.pushExceptionToStatsD(500, var7.getClass().getSimpleName());
                LOGGER.error(MessageFormat.format("Error while performing operation on transactional cluster: {0}", var7.getMessage()));
            } catch (Exception var8) {
                this.statsDUtil.pushExceptionToStatsD(500, var8.getClass().getSimpleName());
                LOGGER.error("Exception while writing on transactional cluster: ", var8);
            }

        }
    }

    Object hget(String key, String field) {
        if (!StringUtils.isBlank(key) && !StringUtils.isBlank(field)) {
            Object value = null;

            try {
                value = this.transactionalClusterOperations.hget(key, field);
            } catch (CallNotPermittedException var5) {
                this.statsDUtil.pushExceptionToStatsD(500, var5.getClass().getSimpleName());
                LOGGER.error(MessageFormat.format("Error while performing operation on transactional cluster: {0}", var5.getMessage()));
            } catch (Exception var6) {
                this.statsDUtil.pushExceptionToStatsD(500, var6.getClass().getSimpleName());
                LOGGER.error("Exception while reading from transactional cluster: ", var6);
            }

            return value;
        } else {
            return null;
        }
    }

    void hdel(String key, String field) {
        if (!StringUtils.isBlank(key) && !StringUtils.isBlank(field)) {
            try {
                this.transactionalClusterOperations.hdel(key, field);
            } catch (CallNotPermittedException var4) {
                this.statsDUtil.pushExceptionToStatsD(500, var4.getClass().getSimpleName());
                LOGGER.error(MessageFormat.format("Error while performing operation on transactional cluster: {0}", var4.getMessage()));
            } catch (Exception var5) {
                this.statsDUtil.pushExceptionToStatsD(500, var5.getClass().getSimpleName());
                LOGGER.error("Exception while deleting from transactional cluster: ", var5);
            }

        }
    }

    boolean setnx(String key, Object value, long ttlInSeconds) {
        if (StringUtils.isBlank(key)) {
            return false;
        } else {
            boolean success = false;

            try {
                success = this.transactionalClusterOperations.setnx(key, value, ttlInSeconds);
            } catch (CallNotPermittedException var7) {
                this.statsDUtil.pushExceptionToStatsD(500, var7.getClass().getSimpleName());
                LOGGER.error(MessageFormat.format("Error while performing operation on transactional cluster: {0}", var7.getMessage()));
            } catch (Exception var8) {
                this.statsDUtil.pushExceptionToStatsD(500, var8.getClass().getSimpleName());
                LOGGER.error("Exception while writing on transactional cluster: ", var8);
            }

            return success;
        }
    }

    boolean expire(String key, long ttlInSeconds) {
        if (StringUtils.isBlank(key)) {
            return false;
        } else {
            boolean success = false;

            try {
                success = this.transactionalClusterOperations.expire(key, ttlInSeconds);
            } catch (CallNotPermittedException var6) {
                this.statsDUtil.pushExceptionToStatsD(500, var6.getClass().getSimpleName());
                LOGGER.error(MessageFormat.format("Error while performing operation on transactional cluster: {0}", var6.getMessage()));
            } catch (Exception var7) {
                this.statsDUtil.pushExceptionToStatsD(500, var7.getClass().getSimpleName());
                LOGGER.error("Exception while expiring on transactional cluster: ", var7);
            }

            return success;
        }
    }

    boolean isExist(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        } else {
            boolean isExist = false;

            try {
                isExist = this.transactionalClusterOperations.isExist(key);
            } catch (CallNotPermittedException var4) {
                this.statsDUtil.pushExceptionToStatsD(500, var4.getClass().getSimpleName());
                LOGGER.error(MessageFormat.format("Error while performing operation on transactional cluster: {0}", var4.getMessage()));
            } catch (Exception var5) {
                this.statsDUtil.pushExceptionToStatsD(500, var5.getClass().getSimpleName());
                LOGGER.error("Exception while checking existence on transactional cluster: ", var5);
            }

            return isExist;
        }
    }

    Object increment(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        } else {
            Object value = null;

            try {
                value = this.transactionalClusterOperations.increment(key);
            } catch (CallNotPermittedException var4) {
                this.statsDUtil.pushExceptionToStatsD(500, var4.getClass().getSimpleName());
                LOGGER.error(MessageFormat.format("Error while performing operation on transactional cluster: {0}", var4.getMessage()));
            } catch (Exception var5) {
                this.statsDUtil.pushExceptionToStatsD(500, var5.getClass().getSimpleName());
                LOGGER.error("Exception while incrementing on transactional cluster: ", var5);
            }

            return value;
        }
    }

    boolean hsetIfExist(String key, String field, Object value) {
        if (!StringUtils.isBlank(key) && !StringUtils.isBlank(field)) {
            boolean success = false;

            try {
                success = this.transactionalClusterOperations.hsetIfExist(key, field, value);
            } catch (CallNotPermittedException var6) {
                this.statsDUtil.pushExceptionToStatsD(500, var6.getClass().getSimpleName());
                LOGGER.error(MessageFormat.format("Error while performing operation on transactional cluster: {0}", var6.getMessage()));
            } catch (Exception var7) {
                this.statsDUtil.pushExceptionToStatsD(500, var7.getClass().getSimpleName());
                LOGGER.error("Exception while writing if exist on transactional cluster: ", var7);
            }

            return success;
        } else {
            return false;
        }
    }
}

