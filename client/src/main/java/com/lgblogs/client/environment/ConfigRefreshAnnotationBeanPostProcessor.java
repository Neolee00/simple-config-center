package com.lgblogs.client.environment;

import com.lgblogs.client.annotation.ConfigRefresh;
import com.lgblogs.client.event.ConfigRefreshEvent;
import com.lgblogs.client.listener.ConfigRefreshEventListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

public class ConfigRefreshAnnotationBeanPostProcessor implements BeanFactoryAware, EnvironmentAware, BeanPostProcessor, ConfigRefreshEventListener {

    private Map<String, List<ConfigRefreshTarget>> placeholderNacosValueTargetMap = new ConcurrentHashMap<>();
    private ConfigurableEnvironment environment;
    private ConfigurableListableBeanFactory beanFactory;
    private BeanExpressionResolver exprResolver;
    private BeanExpressionContext exprContext;

    private static final String SPEL_PREFIX = "#{";

    private static final String PLACEHOLDER_PREFIX = "${";

    private static final String PLACEHOLDER_SUFFIX = "}";

    private static final char PLACEHOLDER_MATCH_PREFIX = '{';

    private static final char PLACEHOLDER_MATCH_SUFFIX = '}';

    private static final String VALUE_SEPARATOR = ":";

    private void doWithFields(final Object bean, final String beanName) {
        ReflectionUtils.doWithFields(bean.getClass(),
                new ReflectionUtils.FieldCallback() {
                    @Override
                    public void doWith(Field field) throws IllegalArgumentException {
                        ConfigRefresh annotation = getAnnotation(field, ConfigRefresh.class);
                        doWithAnnotation(beanName, bean, annotation, field.getModifiers(),
                                null, field);
                    }
                });
    }

    private void doWithAnnotation(String beanName, Object bean, ConfigRefresh annotation, int modifiers, Method method, Field field) {
        if (annotation != null) {
            if (Modifier.isStatic(modifiers)) return;
            String placeholder = resolvePlaceholder(annotation.value());
            if (placeholder == null) return;
            ConfigRefreshTarget target = new ConfigRefreshTarget(bean, beanName, method, field, annotation.value());
            placeholderNacosValueTargetMap.computeIfAbsent(placeholder, e -> new CopyOnWriteArrayList<>()).add(target);
        }
    }

    private String resolvePlaceholder(String placeholder) {
        if (!placeholder.startsWith(PLACEHOLDER_PREFIX) && !placeholder.startsWith(SPEL_PREFIX)) {
            return null;
        }

        if (!placeholder.endsWith(PLACEHOLDER_SUFFIX)) {
            return null;
        }

        if (placeholder.length() <= PLACEHOLDER_PREFIX.length()
                + PLACEHOLDER_SUFFIX.length()) {
            return null;
        }
        int beginIndex = placeholder.indexOf(PLACEHOLDER_PREFIX);
        if (beginIndex == -1) {
            return null;
        }
        beginIndex = beginIndex + PLACEHOLDER_PREFIX.length();
        int endIndex = placeholder.indexOf(PLACEHOLDER_SUFFIX, beginIndex);
        if (endIndex == -1) {
            return null;
        }
        placeholder = placeholder.substring(beginIndex, endIndex);

        int separatorIndex = placeholder.indexOf(VALUE_SEPARATOR);
        if (separatorIndex != -1) {
            return placeholder.substring(0, separatorIndex);
        }

        return placeholder;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        doWithFields(bean, beanName);
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }


    @Override
    public void process(ConfigRefreshEvent event) {
        String configContent = event.getContent();
        ConfigCenterPropertySource source = new ConfigCenterPropertySource("configContentFromEvent", configContent);
        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.replace("configCenterPropertySource", source);
        for (Map.Entry<String, List<ConfigRefreshTarget>> entry : placeholderNacosValueTargetMap.entrySet()) {
            String key = environment.resolvePlaceholders(entry.getKey());
            String newValue = environment.getProperty(key);
            if (newValue == null) {
                continue;
            }
            List<ConfigRefreshTarget> targetList = entry.getValue();
            for (ConfigRefreshTarget target : targetList) {
                Object evaluatedValue = resolveNotifyValue(target.configRefreshExpr, key, newValue);
                setField(target, evaluatedValue);
            }
        }
    }

    private void setField(ConfigRefreshTarget target, Object evaluatedValue) {
        Object bean = target.bean;
        Field field = target.field;
        String fieldName = field.getName();
        try {
            ReflectionUtils.makeAccessible(field);
            field.set(bean, beanFactory.getTypeConverter().convertIfNecessary(evaluatedValue, field.getType(), field));
        } catch (Exception e) {
            System.out.println(String.format("刷新Bean属性时报错，Bean名称为：%s，属性名为：%s \r\n %s", bean.getClass().getSimpleName(), fieldName, e));
        }
    }

    private Object resolveNotifyValue(String nacosValueExpr, String key, String newValue) {
        String spelExpr = nacosValueExpr.replaceAll("\\$\\{" + key + PLACEHOLDER_SUFFIX, newValue);
        return resolveStringValue(spelExpr);
    }

    private Object resolveStringValue(String strVal) {
        String value = beanFactory.resolveEmbeddedValue(strVal);
        if (exprResolver != null && value != null) {
            return exprResolver.evaluate(value, exprContext);
        }
        return value;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
        this.exprResolver = ((ConfigurableListableBeanFactory) beanFactory).getBeanExpressionResolver();
        this.exprContext = new BeanExpressionContext((ConfigurableListableBeanFactory) beanFactory, null);
    }

    public class ConfigRefreshTarget {
        private final Object bean;

        private final String beanName;

        private final Method method;

        private final Field field;

        private final String configRefreshExpr;

        ConfigRefreshTarget(Object bean, String beanName, Method method, Field field, String configRefreshExpr) {
            this.bean = bean;

            this.beanName = beanName;

            this.method = method;

            this.field = field;

            this.configRefreshExpr = resolveExpr(configRefreshExpr);
        }

        private String resolveExpr(String configRefreshExpr) {
            try {
                int replaceHolderBegin = configRefreshExpr.indexOf(PLACEHOLDER_PREFIX) + PLACEHOLDER_PREFIX.length();
                int replaceHolderEnd = replaceHolderBegin;
                for (int i = 0; replaceHolderEnd < configRefreshExpr.length(); replaceHolderEnd++) {
                    char ch = configRefreshExpr.charAt(replaceHolderEnd);
                    if (PLACEHOLDER_MATCH_PREFIX == ch) {
                        i++;
                    } else if (PLACEHOLDER_MATCH_SUFFIX == ch && --i == -1) {
                        break;
                    }
                }
                String replaceHolder = configRefreshExpr.substring(replaceHolderBegin, replaceHolderEnd);
                int separatorIndex = replaceHolder.indexOf(VALUE_SEPARATOR);
                if (separatorIndex != -1) {
                    return configRefreshExpr.substring(0, separatorIndex + replaceHolderBegin) + configRefreshExpr.substring(replaceHolderEnd);
                }
                return configRefreshExpr;
            } catch (Exception e) {
                throw new IllegalArgumentException("The expr format is illegal, expr: " + configRefreshExpr, e);
            }
        }
    }
}
