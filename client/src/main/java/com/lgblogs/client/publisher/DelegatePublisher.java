package com.lgblogs.client.publisher;

import org.springframework.beans.BeansException;
import org.springframework.context.*;

public class DelegatePublisher implements ApplicationEventPublisher, ApplicationContextAware {
    private ConfigurableApplicationContext context;

    @Override
    public void publishEvent(ApplicationEvent event) {
        context.publishEvent(event);
    }

    @Override
    public void publishEvent(Object event) {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = (ConfigurableApplicationContext) applicationContext;
    }
}
