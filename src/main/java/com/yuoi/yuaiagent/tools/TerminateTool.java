package com.yuoi.yuaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * 终止工具：让智能体能自行决定何时结束任务，避免无限循环。
 */
@Component
public class TerminateTool {

    @Tool(description = """
            Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.
            When you have finished all the tasks, call this tool to end the work.
            """)
    public String doTerminate() {
        return "任务结束";
    }
}
