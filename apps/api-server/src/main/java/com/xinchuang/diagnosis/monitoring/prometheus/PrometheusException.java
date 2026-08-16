package com.xinchuang.diagnosis.monitoring.prometheus;

/** 表示监控服务连接或返回数据不可用，不向调用方泄露底层网络细节。 */
public class PrometheusException extends RuntimeException {
    public PrometheusException(String message) { super(message); }
    public PrometheusException(String message, Throwable cause) { super(message, cause); }
}
