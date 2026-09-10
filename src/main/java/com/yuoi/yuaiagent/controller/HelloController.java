package com.yuoi.yuaiagent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "测试接口")
@RestController
@RequestMapping("/test")
public class   HelloController {

    @Operation(summary = "sayHello")
    @GetMapping
    public String hello() {
        return "Knife4j 配置成功！";
    }
}








