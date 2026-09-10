package com.yuoi.yuaiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ReAct 智能体：把单步执行分解为“思考（think）”和“行动（act）”两个抽象步骤，
 * 实现 ReAct（推理 + 行动）模式。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class ReActAgent extends BaseAgent {

    /** 思考：处理当前状态并决定下一步是否行动。 */
    public abstract boolean think();

    /** 行动：执行思考决定的行为。 */
    public abstract String act();

    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return "思考完成 - 无需行动";
            }
            return act();
        } catch (Exception e) {
            return "步骤执行失败: " + e.getMessage();
        }
    }
}
