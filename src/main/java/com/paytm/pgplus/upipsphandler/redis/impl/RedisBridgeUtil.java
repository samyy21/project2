package com.paytm.pgplus.upipsphandler.redis.impl;

import com.paytm.pgplus.cache.redis.RedisClientService;
import com.paytm.pgplus.cache.redis.SessionRedisClientJedisService;
import com.paytm.pgplus.cache.util.CacheProperty;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RedisBridgeUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(com.paytm.pgplus.util.RedisBridgeUtil.class);
    @Autowired
    private IPgpFf4jClient ff4jClient;

    public RedisBridgeUtil() {
    }

    public static RedisClientService checkAndConnectSentinelRedis(String property) {
        String sentinelConnectProperty = getProperty(property);
        if (StringUtils.isBlank(sentinelConnectProperty)) {
            LOGGER.info("making {} to true", property);
            sentinelConnectProperty = "true";
        }

        boolean isAllowedConnectionWithSentinelRedis = BooleanUtils.toBoolean(sentinelConnectProperty);
        if (isAllowedConnectionWithSentinelRedis) {
            LOGGER.info("isAllowedConnectionWithSentinelRedis via property: {}", property);
            if (StringUtils.equals("redis-bridge.connect.sentinel", property)) {
                return RedisClientService.getInstance();
            }

            if (StringUtils.equals("redis-bridge.connect.sentinel.session", property)) {
                return SessionRedisClientJedisService.getInstance();
            }

            if (StringUtils.equals("redis-bridge.connect.sentinel.transactional", property)) {
                return RedisClientService.getInstance();
            }
        }

        return null;
    }

    private static String getProperty(String propertyName) {
        return CacheProperty.getProperties().getProperty(propertyName);
    }

    private Map<String, Object> getContextForFF4J(String key) {
        Map<String, Object> context = new HashMap();
        context.put("mid", key);
        return context;
    }

    private String getAppName() {
        return ConfigurationUtil.getProperty("redis-bridge.app.name", "DEFAULT-NONE");
    }

    private String getStatsdAppName() {
        return ConfigurationUtil.getProperty("statsd.app.name", "DEFAULT-NONE");
    }

    private String getBridgeAppName(String keyPrefix) {
        return ConfigurationUtil.getProperty(keyPrefix + "-bridge.app.name", "SESSION-DEFAULT-NONE");
    }

    public boolean isKeyReadFromClusterAllowed(String key) {
        return this.isKeyAllowed(this.getAppName() + ".RedisBridge.KeysReadFromNewCluster", key, false);
    }

    public boolean isKeyWriteToClusterAllowed(String key) {
        return this.isKeyAllowed(this.getAppName() + ".RedisBridge.KeysWriteToNewCluster", key, false);
    }

    public boolean isKeyReadFromClusterSessionRedisAllowed(String key) {
        return this.isKeyAllowed(this.getBridgeAppName("session") + ".RedisBridge.KeysReadFromNewCluster", key, false);
    }

    public boolean isKeyWriteToClusterSessionRedisAllowed(String key) {
        return this.isKeyAllowed(this.getBridgeAppName("session") + ".RedisBridge.KeysWriteToNewCluster", key, false);
    }

    public boolean isShortCircuitReadFromSentinel() {
        return this.isKeyAllowed("RedisBridge.ShortcircuitReadFromSentinel", this.getAppName(), false);
    }

    public boolean isShortCircuitWriteToSentinel() {
        return this.isKeyAllowed("RedisBridge.ShortcircuitWriteToSentinel", this.getAppName(), false);
    }

    public boolean isShortCircuitReadFromSessionSentinel() {
        return this.isKeyAllowed("RedisBridge.ShortcircuitReadFromSentinel", this.getBridgeAppName("session"), false);
    }

    public boolean isShortCircuitWriteToSessionSentinel() {
        return this.isKeyAllowed("RedisBridge.ShortcircuitWriteToSentinel", this.getBridgeAppName("session"), false);
    }

    public boolean whitelistToWriteOnSentinel(String key) {
        boolean whitelist = false;
        if (key.length() >= 6) {
            whitelist = this.isKeyAllowed("RedisBridge.WhitelistKeysPrefixToWriteOnSentinel", key.substring(0, 6), false);
        }

        return whitelist;
    }

    public boolean isKeyReadFromClusterTransactionalRedisAllowed(String key) {
        return this.isKeyAllowed(this.getBridgeAppName("transactional") + "." + "transactional" + ".RedisBridge.KeysReadFromNewCluster", key, false);
    }

    public boolean isKeyWriteToClusterTransactionalRedisAllowed(String key) {
        return this.isKeyAllowed(this.getBridgeAppName("transactional") + "." + "transactional" + ".RedisBridge.KeysWriteToNewCluster", key, false);
    }

    public boolean isShortCircuitReadFromTransactionalSentinel() {
        return this.isKeyAllowed("RedisBridge.ShortcircuitReadFromSentinel", this.getBridgeAppName("transactional"), false);
    }

    public boolean isShortCircuitWriteToTransactionalSentinel() {
        return this.isKeyAllowed("RedisBridge.ShortcircuitWriteToSentinel", this.getBridgeAppName("transactional"), false);
    }

    private boolean isKeyAllowed(String feature, String key, boolean defaultValue) {
        boolean isAllowed = defaultValue;

        try {
            isAllowed = this.ff4jClient.checkWithdefault(feature, this.getContextForFF4J("ALL"), defaultValue);
            if (!isAllowed) {
                isAllowed = this.ff4jClient.checkWithdefault(feature, this.getContextForFF4J(key), defaultValue);
            }
        } catch (Exception var6) {
            LOGGER.error("Error in getting data from FF4J for feature:{} for key:{}", feature, key);
        }

        return isAllowed;
    }

    public boolean isCircuitBreakerDisabled() {
        return this.isKeyAllowed("RedisBridge.circuitBreaker.disabled", this.getAppName(), false);
    }

    public boolean isSessionBridgeCircuitBreakerDisabled() {
        return this.isKeyAllowed("RedisBridge.circuitBreaker.disabled", this.getBridgeAppName("session"), false);
    }

    public boolean isTransactionalBridgeCircuitBreakerDisabled() {
        return this.isKeyAllowed("RedisBridge.circuitBreaker.disabled", this.getBridgeAppName("transactional"), false);
    }

    public boolean isStatsDLoggingEnabled() {
        return this.isKeyAllowed("Mapping_srv_statsd_logging_enabled", (String)null, false);
    }

    public boolean isStatsDLoggingEnabledForAppName() {
        return this.isKeyAllowed(this.getStatsdAppName() + ".statsd.logging.enabled", (String)null, false);
    }

    public String getAppNameForStatsD() {
        return this.getAppName();
    }
}

