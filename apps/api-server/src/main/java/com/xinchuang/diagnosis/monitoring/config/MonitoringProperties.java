package com.xinchuang.diagnosis.monitoring.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Prometheus 连接与目标映射的本地运行配置。 */
@Component
@ConfigurationProperties(prefix = "monitoring")
public class MonitoringProperties {
    private final Prometheus prometheus = new Prometheus();
    private final Targets targets = new Targets();

    public Prometheus getPrometheus() { return prometheus; }
    public Targets getTargets() { return targets; }

    public static class Prometheus {
        private String baseUrl = "";
        private Duration connectTimeout = Duration.ofSeconds(3);
        private Duration readTimeout = Duration.ofSeconds(8);
        private List<String> allowedHosts = new ArrayList<>();
        private List<String> allowedCidrs = new ArrayList<>();
        private Map<String, String> authRefs = Map.of();

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public Duration getConnectTimeout() { return connectTimeout; }
        public void setConnectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; }
        public Duration getReadTimeout() { return readTimeout; }
        public void setReadTimeout(Duration readTimeout) { this.readTimeout = readTimeout; }
        public List<String> getAllowedHosts() { return allowedHosts; }
        public void setAllowedHosts(List<String> value) { allowedHosts = value == null ? new ArrayList<>() : value; }
        public List<String> getAllowedCidrs() { return allowedCidrs; }
        public void setAllowedCidrs(List<String> value) { allowedCidrs = value == null ? new ArrayList<>() : value; }
        public Map<String, String> getAuthRefs() { return authRefs; }
        public void setAuthRefs(Map<String, String> value) { authRefs = value == null ? Map.of() : value; }
    }

    public static class Targets {
        private String nodeInstance = "";
        private String mysqlInstance = "";

        public String getNodeInstance() { return nodeInstance; }
        public void setNodeInstance(String nodeInstance) { this.nodeInstance = nodeInstance; }
        public String getMysqlInstance() { return mysqlInstance; }
        public void setMysqlInstance(String mysqlInstance) { this.mysqlInstance = mysqlInstance; }
    }
}
