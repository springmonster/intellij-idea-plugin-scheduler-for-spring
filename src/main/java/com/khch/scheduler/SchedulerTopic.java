package com.khch.scheduler;

import com.intellij.util.messages.Topic;
import com.khch.scheduler.model.ScheduledModel;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

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
    void afterAction(@Nullable Map<String, List<ScheduledModel>> data);
}
