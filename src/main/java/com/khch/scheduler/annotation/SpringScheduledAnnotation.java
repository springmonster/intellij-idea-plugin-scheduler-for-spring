package com.khch.scheduler.annotation;

import org.jetbrains.annotations.Nullable;

/**
 * @author KuangHaochuan
 */
public enum SpringScheduledAnnotation {

    SCHEDULED("org.springframework.scheduling.annotation.Scheduled");

    private final String qualifiedName;

    SpringScheduledAnnotation(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    @Nullable
    public static SpringScheduledAnnotation getByQualifiedName(String qualifiedName) {
        for (SpringScheduledAnnotation springScheduledAnnotation : SpringScheduledAnnotation.values()) {
            if (springScheduledAnnotation.getQualifiedName().equals(qualifiedName)) {
                return springScheduledAnnotation;
            }
        }
        return null;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }
}
