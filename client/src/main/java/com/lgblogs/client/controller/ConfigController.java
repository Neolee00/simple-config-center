package com.lgblogs.client.controller;

import com.lgblogs.client.environment.ConfigCenterEnvironmentProcessor;
import com.lgblogs.client.environment.ConfigCenterPropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/config")
public class ConfigController {

    @GetMapping("/get")
    public String get() {
        String configContent = ConfigCenterEnvironmentProcessor.reqConfig();
        ConfigCenterPropertySource configCenterPropertySource = new ConfigCenterPropertySource("configCenterPropertySource", configContent);
        return "hello world";
    }
}
