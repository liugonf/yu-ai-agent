package com.yuoi.yuaiagent.agent.model;

/**
 * 智能体执行状态。
 */
public enum AgentState {
    /** 空闲，可开始执行 */
    IDLE,
    /** 正在执行 */
    RUNNING,
    /** 已正常完成 */
    FINISHED,
    /** 执行出错 */
    ERROR
}
