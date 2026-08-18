package com.xinchuang.diagnosis.monitoring.security;

import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.monitoring.config.MonitoringProperties;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PrometheusEndpointValidator {
    private final MonitoringProperties properties;

    public PrometheusEndpointValidator(MonitoringProperties properties) { this.properties = properties; }

    public URI validate(String rawUrl) {
        if (!StringUtils.hasText(rawUrl)) throw new BusinessException("Prometheus 地址不能为空");
        final URI uri;
        try { uri = URI.create(rawUrl.trim()); } catch (IllegalArgumentException ex) { throw new BusinessException("Prometheus 地址格式不正确"); }
        if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
            throw new BusinessException("Prometheus 仅支持 HTTP 或 HTTPS");
        }
        if (uri.getUserInfo() != null || !StringUtils.hasText(uri.getHost()) || uri.getQuery() != null || uri.getFragment() != null) {
            throw new BusinessException("Prometheus 地址包含不安全内容");
        }
        if (!isAllowedHost(uri.getHost())) throw new BusinessException("Prometheus 地址不在服务端允许范围内");
        return URI.create(uri.toString().replaceFirst("/+$", ""));
    }

    private boolean isAllowedHost(String host) {
        String normalized = host.toLowerCase(java.util.Locale.ROOT);
        boolean hostAllowed = properties.getPrometheus().getAllowedHosts().stream()
            .map(value -> value.toLowerCase(java.util.Locale.ROOT).trim())
            .anyMatch(value -> normalized.equals(value) || normalized.endsWith("." + value));
        try {
            List<InetAddress> addresses = Arrays.asList(InetAddress.getAllByName(host));
            for (InetAddress address : addresses) {
                if (isForbidden(address)) return false;
                if (isPrivate(address) && !matchesCidr(address, properties.getPrometheus().getAllowedCidrs())) return false;
                if (!hostAllowed && !matchesCidr(address, properties.getPrometheus().getAllowedCidrs())) return false;
            }
            return hostAllowed || addresses.stream().allMatch(address -> matchesCidr(address, properties.getPrometheus().getAllowedCidrs()));
        } catch (UnknownHostException ex) {
            return false;
        }
    }

    private boolean isForbidden(InetAddress address) {
        String value = address.getHostAddress();
        return address.isLoopbackAddress() || address.isLinkLocalAddress() || address.isAnyLocalAddress()
            || value.equals("169.254.169.254") || value.equals("100.100.100.200");
    }

    private boolean isPrivate(InetAddress address) {
        return address.isSiteLocalAddress() || address.isLinkLocalAddress()
            || address.isLoopbackAddress() || address.isAnyLocalAddress();
    }

    private boolean matchesCidr(InetAddress address, List<String> cidrs) {
        for (String cidr : cidrs) {
            String[] parts = cidr.trim().split("/", 2);
            try {
                InetAddress network = InetAddress.getByName(parts[0]);
                int prefix = parts.length == 2 ? Integer.parseInt(parts[1]) : network.getAddress().length * 8;
                byte[] actual = address.getAddress();
                byte[] expected = network.getAddress();
                if (actual.length != expected.length || prefix < 0 || prefix > actual.length * 8) continue;
                int full = prefix / 8, bits = prefix % 8;
                boolean match = true;
                for (int i = 0; i < full; i++) match &= actual[i] == expected[i];
                if (bits > 0) match &= (actual[full] & (0xFF << (8 - bits))) == (expected[full] & (0xFF << (8 - bits)));
                if (match) return true;
            } catch (RuntimeException | UnknownHostException ignored) { }
        }
        return false;
    }
}
