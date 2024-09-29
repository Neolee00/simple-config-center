package com.lgblogs.client.environment;

import org.springframework.core.env.MapPropertySource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;

public class ConfigCenterPropertySource extends MapPropertySource {


    public ConfigCenterPropertySource(String name, String yamlContent) {
        super(name, convertMysqlYamlToMap(yamlContent));
    }


    public static Map<String, Object> convertLocalYamlToMap(String yamlFileName) {
        Yaml yaml = new Yaml();
        InputStream inputStream = ConfigCenterPropertySource.class
                .getClassLoader()
                .getResourceAsStream(yamlFileName);
        Map<String, Object> yamlMap = yaml.load(inputStream);
        return yamlMap;
    }

    public static Map<String, Object> convertMysqlYamlToMap(String yamlContent) {
        Yaml yaml = new Yaml();
        StringReader reader = new StringReader(yamlContent);
        Map<String, Object> yamlMap = yaml.load(reader);
        return yamlMap;
    }

}
