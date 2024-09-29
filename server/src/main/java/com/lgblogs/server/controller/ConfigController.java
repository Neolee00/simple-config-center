package com.lgblogs.server.controller;

import com.lgblogs.server.core.entity.Config;
import com.lgblogs.server.core.service.ConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping( "/config")
public class ConfigController {
    @Resource
    private ConfigService configService;

    @GetMapping
    public String get() {
        return Optional.ofNullable(configService.getById(1)).map(Config::getConfigContent).orElse("空的");
    }

    @GetMapping("/in")
    public String in() {
        Config entity = new Config();
        entity.setConfigContent("useLocalCache=false");
        entity.setCreateTime(new Date());
        configService.save(entity);
        return "success";
    }
}
