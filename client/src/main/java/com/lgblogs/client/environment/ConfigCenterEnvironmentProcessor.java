package com.lgblogs.client.environment;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.web.client.RestTemplate;

public class ConfigCenterEnvironmentProcessor implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        String configContent = reqConfig();
        propertySources.addLast(new ConfigCenterPropertySource("configCenterPropertySource", configContent));
    }

    public static String reqConfig() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.getForObject("http://localhost:8081/config", String.class);
        } catch (Exception e) {
            return "useLocalCache: true";
        }
    }
}
