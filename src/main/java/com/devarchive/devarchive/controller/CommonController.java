package com.devarchive.devarchive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import ch.qos.logback.core.model.Model;

@Controller
public class CommonController {
    
    // 이 매핑이 없으면 404가 뜹니다!
    @GetMapping("/common/alert")
    public String alert(Model model) {
        // model에 담긴 값은 Security가 넘겨준 그대로 사용됩니다.
        return "common/alert";
    }
}