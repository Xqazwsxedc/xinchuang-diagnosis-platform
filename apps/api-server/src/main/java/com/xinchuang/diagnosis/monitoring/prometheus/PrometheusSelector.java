package com.xinchuang.diagnosis.monitoring.prometheus;

import com.xinchuang.diagnosis.common.exception.BusinessException;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public final class PrometheusSelector {
    private static final Pattern KEY = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final Pattern VALUE = Pattern.compile("[A-Za-z0-9_./:@-]{1,128}");

    private PrometheusSelector() { }

    /**
     * 将资源级 label 映射格式化为 PromQL selector，并校验键和值的字符范围。
     * 使用排序后的键保证相同配置生成稳定的查询字符串，便于日志和测试比对。
     */
    public static String format(Map<String, String> selector, String name) {
        if (selector == null || selector.isEmpty()) throw new BusinessException(name + "不能为空");
        return new TreeMap<>(selector).entrySet().stream().map(entry -> {
            if (!KEY.matcher(entry.getKey()).matches()) throw new BusinessException(name + "包含非法 label 键");
            if (entry.getValue() == null || !VALUE.matcher(entry.getValue()).matches()) throw new BusinessException(name + "包含非法 label 值");
            return entry.getKey() + "=\"" + entry.getValue() + "\"";
        }).reduce((left, right) -> left + "," + right).map(value -> "{" + value + "}").orElseThrow();
    }

    /** 保留单 selector 调用语义，实际格式化规则与 {@link #format(Map, String)} 相同。 */
    public static String formatSingle(Map<String, String> selector, String name) { return format(selector, name); }
}
