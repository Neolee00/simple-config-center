package com.lgblogs.client.controller;

import com.lgblogs.client.annotation.ConfigRefresh;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @ConfigRefresh("${useLocalCache:false}")
    private boolean flag;

    @GetMapping("/get")
    public boolean get() {
        return flag;
    }
}
