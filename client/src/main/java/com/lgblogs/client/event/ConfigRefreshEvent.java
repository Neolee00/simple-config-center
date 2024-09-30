package com.lgblogs.client.event;

import org.springframework.context.ApplicationEvent;

public class ConfigRefreshEvent extends ApplicationEvent {
    private final String content;

    public ConfigRefreshEvent(String source) {
        super(source);
        this.content = source;
    }

    public String getContent() {
        return this.content;
    }
}
