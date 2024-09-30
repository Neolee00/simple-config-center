package com.lgblogs.client.listener;

import com.lgblogs.client.event.ConfigRefreshEvent;
import org.springframework.context.ApplicationListener;

public interface ConfigRefreshEventListener extends ApplicationListener<ConfigRefreshEvent> {
    @Override
    default void onApplicationEvent(ConfigRefreshEvent event) {
        process(event);
    }

    void process(ConfigRefreshEvent event);
}
