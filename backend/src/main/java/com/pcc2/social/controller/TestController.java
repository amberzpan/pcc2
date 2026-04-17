package com.pcc2.social.controller;

import com.pcc2.social.common.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }
}