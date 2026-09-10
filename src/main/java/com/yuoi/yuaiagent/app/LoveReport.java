package com.yuoi.yuaiagent.app;

import java.util.List;

/**
 * 恋爱报告（结构化输出目标类型）。
 *
 * <p>由大模型按 JSON Schema 输出后，被 {@code BeanOutputConverter} 自动映射为该对象：
 * 一个个性化标题 + 一组具体建议。</p>
 */
public record LoveReport(String title, List<String> suggestions) {
}
