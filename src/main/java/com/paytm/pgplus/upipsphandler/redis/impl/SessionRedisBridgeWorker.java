package com.paytm.pgplus.upipsphandler.redis.impl;

import com.paytm.pgplus.upipsphandler.utils.AWSStatsDUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class SessionRedisBridgeWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionRedisBridgeWorker.class);
    @Autowired
    RedisBridgeUtil redisBridgeUtil;
    @Autowired
    SessionClusterOperations sessionClusterOperations;
    @Autowired
    private AWSStatsDUtils statsDUtil;

    SessionRedisBridgeWorker() {
    }

    void set(String key, Object value, long ttl) {
        try {
            this.sessionClusterOperations.set(key, value, ttl);
        } catch (Exception var6) {
            this.statsDUtil.pushExceptionToStatsD(500, var6.getClass().getSimpleName());
            LOGGER.error("Exception while writing on session cluster: ", var6);
        }

    }

    Object get(String key) {
        Object value = null;
        if (StringUtils.isBlank(key)) {
            return null;
        } else {
            try {
                value = this.sessionClusterOperations.get(key);
            } catch (Exception var4) {
                this.statsDUtil.pushExceptionToStatsD(500, var4.getClass().getSimpleName());
                LOGGER.error("Exception while reading from session cluster: ", var4);
            }

            return value;
        }
    }

    void del(String key) {
        if (!StringUtils.isBlank(key)) {
            try {
                this.sessionClusterOperations.del(key);
            } catch (Exception var3) {
                this.statsDUtil.pushExceptionToStatsD(500, var3.getClass().getSimpleName());
                LOGGER.error("Exception while deleting from session cluster: ", var3);
            }

        }
    }

    void hset(String key, String field, Object value, long ttlInSeconds) {
        if (!StringUtils.isBlank(key) && !StringUtils.isBlank(field)) {
            try {
                this.sessionClusterOperations.hset(key, field, value, ttlInSeconds);
            } catch (Exception var7) {
                this.statsDUtil.pushExceptionToStatsD(500, var7.getClass().getSimpleName());
                LOGGER.error("Exception while writing on session cluster: ", var7);
            }

        }
    }

    Object hget(String key, String field) {
        if (!StringUtils.isBlank(key) && !StringUtils.isBlank(field)) {
            Object value = null;

            try {
                value = this.sessionClusterOperations.hget(key, field);
            } catch (Exception var5) {
                this.statsDUtil.pushExceptionToStatsD(500, var5.getClass().getSimpleName());
                LOGGER.error("Exception while reading from session cluster: ", var5);
            }

            return value;
        } else {
            return null;
        }
    }

    void hdel(String key, String field) {
        if (!StringUtils.isBlank(key) && !StringUtils.isBlank(field)) {
            try {
                this.sessionClusterOperations.hdel(key, field);
            } catch (Exception var4) {
                this.statsDUtil.pushExceptionToStatsD(500, var4.getClass().getSimpleName());
                LOGGER.error("Exception while deleting from session cluster: ", var4);
            }

        }
    }

    boolean setnx(String key, Object value, long ttlInSeconds) {
        if (StringUtils.isBlank(key)) {
            return false;
        } else {
            boolean success = false;

            try {
                success = this.sessionClusterOperations.setnx(key, value, ttlInSeconds);
            } catch (Exception var7) {
                this.statsDUtil.pushExceptionToStatsD(500, var7.getClass().getSimpleName());
                LOGGER.error("Exception while writing on session cluster: ", var7);
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
                success = this.sessionClusterOperations.expire(key, ttlInSeconds);
            } catch (Exception var6) {
                this.statsDUtil.pushExceptionToStatsD(500, var6.getClass().getSimpleName());
                LOGGER.error("Exception while expiring on session cluster: ", var6);
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
                isExist = this.sessionClusterOperations.isExist(key);
            } catch (Exception var4) {
                this.statsDUtil.pushExceptionToStatsD(500, var4.getClass().getSimpleName());
                LOGGER.error("Exception while checking existence on session cluster: ", var4);
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
                value = this.sessionClusterOperations.increment(key);
            } catch (Exception var4) {
                this.statsDUtil.pushExceptionToStatsD(500, var4.getClass().getSimpleName());
                LOGGER.error("Exception while incrementing on session cluster: ", var4);
            }

            return value;
        }
    }

    boolean hsetIfExist(String key, String field, Object value) {
        if (!StringUtils.isBlank(key) && !StringUtils.isBlank(field)) {
            boolean success = false;

            try {
                success = this.sessionClusterOperations.hsetIfExist(key, field, value);
            } catch (Exception var6) {
                this.statsDUtil.pushExceptionToStatsD(500, var6.getClass().getSimpleName());
                LOGGER.error("Exception while writing if exist on session cluster: ", var6);
            }

            return success;
        } else {
            return false;
        }
    }

    Long ttl(String key) {
        Long ttl = -2L;
        if (StringUtils.isBlank(key)) {
            return ttl;
        } else {
            try {
                ttl = this.sessionClusterOperations.ttl(key);
            } catch (Exception var4) {
                this.statsDUtil.pushExceptionToStatsD(500, var4.getClass().getSimpleName());
                LOGGER.error("Exception while checking ttl on session cluster: ", var4);
            }

            return ttl;
        }
    }

    String getMid() {
        String mid = MDC.get("mid");
        if (StringUtils.isBlank(mid)) {
            mid = MDC.get("MID");
        }

        if (StringUtils.isBlank(mid)) {
            LOGGER.info("returning dummy MID");
            return "DUMMY_MID";
        } else {
            return mid;
        }
    }
}
