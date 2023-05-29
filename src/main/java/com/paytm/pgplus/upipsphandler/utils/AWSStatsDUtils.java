package com.paytm.pgplus.upipsphandler.utils;

import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AWSStatsDUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(com.paytm.pgplus.stats.util.AWSStatsDUtils.class);
    private StatsDClient statsClient;
    public static Set<String> appNamesSet = new HashSet();

    public AWSStatsDUtils() {
        if (this.isEnvAWS()) {
            this.statsClient = new NonBlockingStatsDClient(this.getStatsdAppName(), this.getHostName(), this.getPortName());
            String facadeAppNamesForTrimming = ConfigurationUtil.getProperty("facade.app.name.for.trimming", "mapping-service,mapping-service-mif-migration");
            if (StringUtils.isNotBlank(facadeAppNamesForTrimming)) {
                appNamesSet = (Set) Stream.of(facadeAppNamesForTrimming.trim().split("\\s*,\\s*")).collect(Collectors.toSet());
            }
        }

    }

    boolean isEnvAWS() {
        return "AWS".equalsIgnoreCase(ConfigurationUtil.getProperty("env.name", "DC"));
    }

    public String getStatsdAppName() {
        return ConfigurationUtil.getProperty("statsd.app.name", "DEFAULT-NONE");
    }

    private String getHostName() {
        return ConfigurationUtil.getProperty("statsd.app.host", "localhost");
    }

    private int getPortName() {
        String port = ConfigurationUtil.getProperty("statsd.app.port");
        if (port == null) {
            return 8130;
        } else {
            try {
                return Integer.parseInt(port);
            } catch (Exception var3) {
                LOGGER.error("Invalid port passed");
                return 8130;
            }
        }
    }

    public void pushExceptionToStatsD(int errorCode, String exceptionName) {
        if (this.isEnvAWS() && this.isStatsDLoggingEnabledForAppName()) {
            String[] tags = new String[]{"exception_type:" + exceptionName, null};

            try {
                if (errorCode >= 400 && errorCode <= 599) {
                    tags[1] = "response_code:" + errorCode;
                } else {
                    tags[1] = "response_code:" + errorCode / 100 + "XX";
                }

                this.statsClient.increment("exception", tags);
            } catch (Exception var5) {
                LOGGER.error("Exception occurred while pushing logs to statsd" + var5.getMessage());
            }

        }
    }

    public void pushApiResponseTimeEventLog(Map<String, String> apiLogInfo) {
        if (this.isEnvAWS() && this.isStatsDLoggingEnabledForAppName()) {
            try {
                if (null != apiLogInfo && !apiLogInfo.isEmpty()) {
                    apiLogInfo = this.buildlogRequest(apiLogInfo);
                    this.statsClient.recordExecutionTime("stats.latency", Long.parseLong((String)apiLogInfo.get("execution_time")), new String[]{"uri:" + (String)apiLogInfo.get("api_name")});
                    apiLogInfo.remove("execution_time");
                    String[] tags = new String[apiLogInfo.size()];
                    int apiKey = 0;

                    Map.Entry entry;
                    for(Iterator var4 = apiLogInfo.entrySet().iterator(); var4.hasNext(); tags[apiKey++] = (String)entry.getKey() + ":" + (String)entry.getValue()) {
                        entry = (Map.Entry)var4.next();
                    }

                    this.statsClient.increment("api_stats", tags);
                }
            } catch (Exception var6) {
                LOGGER.error("Exception occurred while pushing logs to statsd" + var6.getMessage());
            }

        }
    }

    public void pushCustomAPIstats(Map<String, String> apiLogInfo) {
        if (this.isEnvAWS() && this.isStatsDLoggingEnabledForAppName()) {
            try {
                if (null != apiLogInfo && !apiLogInfo.isEmpty()) {
                    try {
                        Long executionTime = Long.parseLong((String)apiLogInfo.get("execution_time"));
                        this.statsClient.recordExecutionTime("stats.latency", executionTime, new String[]{"uri:" + (String)apiLogInfo.get("api_name")});
                    } catch (Exception var6) {
                        LOGGER.error("Exception occurred while recording Execution Time" + var6.getMessage());
                    }

                    String[] tags = new String[apiLogInfo.size()];
                    int apiKey = 0;

                    Map.Entry entry;
                    for(Iterator var4 = apiLogInfo.entrySet().iterator(); var4.hasNext(); tags[apiKey++] = (String)entry.getKey() + ":" + (String)entry.getValue()) {
                        entry = (Map.Entry)var4.next();
                    }

                    this.statsClient.increment("api_stats", tags);
                }
            } catch (Exception var7) {
                LOGGER.error("Exception occurred while pushing api logs to statsd" + var7.getMessage());
            }

        }
    }

    public void pushFacadeApiResponseTimeEventLog(Map<String, String> apiLogInfo) {
        if (this.isEnvAWS() && this.isStatsDFacadeLoggingEnabledForAppName()) {
            try {
                if (null != apiLogInfo && !apiLogInfo.isEmpty()) {
                    this.statsClient.recordExecutionTime("facade.stats.latency", Long.parseLong((String)apiLogInfo.get("execution_time")), new String[]{"uri:" + (String)apiLogInfo.get("down_stream_url")});
                    apiLogInfo.remove("execution_time");
                    String[] tags = new String[apiLogInfo.size()];
                    int apiKey = 0;

                    Map.Entry entry;
                    for(Iterator var4 = apiLogInfo.entrySet().iterator(); var4.hasNext(); tags[apiKey++] = (String)entry.getKey() + ":" + (String)entry.getValue()) {
                        entry = (Map.Entry)var4.next();
                    }

                    this.statsClient.increment("facade.api.stats", tags);
                }
            } catch (Exception var6) {
                LOGGER.error("Exception occurred while pushing logs to statsd" + var6.getMessage());
            }

        }
    }

    public void pushFacadeExceptionToStatsD(Map<String, String> apiLogInfo) {
        if (this.isEnvAWS() && this.isStatsDFacadeLoggingEnabledForAppName()) {
            try {
                if (null != apiLogInfo && !apiLogInfo.isEmpty()) {
                    String[] tags = new String[apiLogInfo.size()];
                    int apiKey = 0;

                    Map.Entry entry;
                    for(Iterator var4 = apiLogInfo.entrySet().iterator(); var4.hasNext(); tags[apiKey++] = (String)entry.getKey() + ":" + (String)entry.getValue()) {
                        entry = (Map.Entry)var4.next();
                    }

                    this.statsClient.increment("facade.exception", tags);
                }
            } catch (Exception var6) {
                LOGGER.error("Exception occurred while pushing logs to statsd" + var6.getMessage());
            }

        }
    }

    public void pushResponse(String apiName, Map<String, String> response) {
        if (this.isEnvAWS() && this.isStatsDFacadeLoggingEnabledForAppName()) {
            try {
                if (null != response && !response.isEmpty()) {
                    String[] tags = new String[response.size()];
                    int apiKey = 0;

                    Map.Entry entry;
                    for(Iterator var5 = response.entrySet().iterator(); var5.hasNext(); tags[apiKey++] = (String)entry.getKey() + ":" + (String)entry.getValue()) {
                        entry = (Map.Entry)var5.next();
                    }

                    this.statsClient.increment(apiName, tags);
                }
            } catch (Exception var7) {
                LOGGER.error("Exception occurred while pushing logs to statsd" + var7.getMessage());
            }

        }
    }

    private Map<String, String> buildlogRequest(Map<String, String> apiLogInfo) {
        Map<String, String> requestLogMap = new HashMap();
        requestLogMap.put("execution_time", apiLogInfo.get("execution_time"));
        requestLogMap.put("http_method", apiLogInfo.get("http_method"));
        requestLogMap.put("api_name", apiLogInfo.get("api_name"));
        if (apiLogInfo.get("http_response_codes") != null) {
            try {
                int statusCode = Integer.parseInt((String)apiLogInfo.get("http_response_codes"));
                if (statusCode >= 400 && statusCode <= 599) {
                    requestLogMap.put("response_code", String.valueOf(statusCode));
                } else {
                    requestLogMap.put("response_code", statusCode / 100 + "XX");
                }
            } catch (Exception var4) {
                LOGGER.error("Invalid response code : {}" + (String)apiLogInfo.get("http_response_codes"));
            }
        }

        return requestLogMap;
    }

    public void pushException(String type) {
        if (this.isEnvAWS() && this.isStatsDLoggingEnabledForAppName()) {
            try {
                String[] tags = new String[]{"exception_type:" + type};
                this.statsClient.increment("exception", tags);
            } catch (Exception var3) {
                LOGGER.error("Exception occurred while pushing statsd metrics" + var3.getMessage());
            }

        }
    }

    public void statsDForKafkaPushTime(String timeElapsed) {
        LOGGER.debug("statsD working in statsDForKafkaPushTime");
        if (org.apache.commons.lang.StringUtils.isNumeric(timeElapsed)) {
            this.statsClient.recordExecutionTime("stats.kafka.push.time", Long.parseLong(timeElapsed), new String[0]);
        }

    }

    public void statsDForQueueBrokerException(String exceptionName) {
        LOGGER.info("statsD working in statsDForQueueBrokerException");
        String[] tags = new String[]{"exception:" + exceptionName};
        this.statsClient.increment("statsd.exception", tags);
    }

    public boolean isStatsDLoggingEnabledForAppName() {
        return Boolean.valueOf(ConfigurationUtil.getProperty(this.getStatsdAppName() + ".statsd.enable.for.app", "false"));
    }

    public boolean isStatsDFacadeLoggingEnabledForAppName() {
        return Boolean.valueOf(ConfigurationUtil.getProperty(this.getStatsdAppName() + ".statsd.enable.for.facade", "false"));
    }

    private boolean isKeyAllowed(String feature, String key, boolean defaultValue) {
        boolean isAllowed = defaultValue;

        try {
            if (!isAllowed) {
            }
        } catch (Exception var6) {
            LOGGER.error("Error in getting data from FF4J for feature:{} for key:{}", feature, key);
        }

        return defaultValue;
    }

    private Map<String, Object> getContextForFF4J(String key) {
        Map<String, Object> context = new HashMap();
        context.put("mid", key);
        return context;
    }

    public void statsDForNotiSchedulerGlobalExceptionHandler(String exceptionName) {
        LOGGER.info("statsD working in statsDForNotiSchedulerGlobalExceptionHandler");
        this.statsClient.increment("stats.scheduler.exception", new String[]{"exception:" + exceptionName});
    }

    public void statsDForNotiSchedulerServiceProcessCount(String serviceName) {
        LOGGER.info("statsD working in statsDForNotiSchedulerServiceProcessCount");
        this.statsClient.increment("stats.scheduler.service.count", new String[]{"serviceName:" + serviceName});
    }

    public void pushJsonPtcApiDetails(Map<String, String> apiLogInfo) {
        if (this.isStatsDLoggingEnabledForAppName()) {
            try {
                if (null != apiLogInfo && !apiLogInfo.isEmpty()) {
                    this.recordExecutionTime(apiLogInfo);
                    apiLogInfo.remove("execution_time");
                    String[] tags = new String[apiLogInfo.size()];
                    int apiKey = 0;

                    Map.Entry entry;
                    for(Iterator var4 = apiLogInfo.entrySet().iterator(); var4.hasNext(); tags[apiKey++] = (String)entry.getKey() + ":" + (String)entry.getValue()) {
                        entry = (Map.Entry)var4.next();
                    }

                    this.statsClient.increment("ptc_api_stats_details", tags);
                }
            } catch (Exception var6) {
                LOGGER.error("Exception occurred while pushing logs to statsd: {}", var6.getMessage());
            }

        }
    }

    public void pushFormPtcLatencyDetails(Map<String, String> apiLogInfo) {
        if (this.isStatsDLoggingEnabledForAppName()) {
            try {
                if (null != apiLogInfo && !apiLogInfo.isEmpty()) {
                    this.recordExecutionTime(apiLogInfo);
                }
            } catch (Exception var3) {
                LOGGER.error("Exception occurred while pushing logs to statsd: {}", var3.getMessage());
            }

        }
    }

    private void recordExecutionTime(Map<String, String> apiLogInfo) {
        String[] executionTimeTags = new String[]{"uri:" + (String)apiLogInfo.get("api_name"), "payment_mode:" + (String)apiLogInfo.get("payment_mode"), "request_type:" + (String)apiLogInfo.get("request_type"), "mid:" + (String)apiLogInfo.get("mid")};
        this.statsClient.recordExecutionTime("ptc.stats.latency", Long.parseLong((String)apiLogInfo.get("execution_time")), executionTimeTags);
    }

    public void pushFormPtcSrDetails(Map<String, String> apiLogInfo) {
        if (this.isStatsDLoggingEnabledForAppName()) {
            try {
                if (null != apiLogInfo && !apiLogInfo.isEmpty()) {
                    String[] tags = new String[apiLogInfo.size()];
                    int apiKey = 0;

                    Map.Entry entry;
                    for(Iterator var4 = apiLogInfo.entrySet().iterator(); var4.hasNext(); tags[apiKey++] = (String)entry.getKey() + ":" + (String)entry.getValue()) {
                        entry = (Map.Entry)var4.next();
                    }

                    this.statsClient.increment("ptc_api_stats_details", tags);
                }
            } catch (Exception var6) {
                LOGGER.error("Exception occurred while pushing logs to statsd: {}", var6.getMessage());
            }

        }
    }

    public void pushSourceApiEventLog(Map<String, String> apiLogInfo) {
        if (this.isEnvAWS() && this.isStatsDFacadeLoggingEnabledForAppName()) {
            try {
                if (null != apiLogInfo && !apiLogInfo.isEmpty()) {
                    String[] tags = new String[apiLogInfo.size()];
                    int apiKey = 0;

                    Map.Entry entry;
                    for(Iterator var4 = apiLogInfo.entrySet().iterator(); var4.hasNext(); tags[apiKey++] = (String)entry.getKey() + ":" + (String)entry.getValue()) {
                        entry = (Map.Entry)var4.next();
                    }

                    LOGGER.info("PUSHING LOGS TO STATSD : {}", tags.toString());
                    this.statsClient.increment("api_source_stats", tags);
                }
            } catch (Exception var6) {
                LOGGER.error("Exception occurred while pushing logs to statsd" + var6.getMessage());
            }

        }
    }
}

