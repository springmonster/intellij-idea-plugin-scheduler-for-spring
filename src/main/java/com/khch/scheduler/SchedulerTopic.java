package com.khch.scheduler;

import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.Nullable;

/**
 * @author KuangHaochuan
 * @version 1.0
 */
public interface SchedulerTopic {
    
    Topic<SchedulerTopic> ACTION_SCAN_SERVICE = Topic.create("SchedulerTopic.ACTION_SCAN_SERVICE", SchedulerTopic.class);
    
    /**
     * after
     *
     * @param data data
     */
    void afterAction(@Nullable Object data);
}
