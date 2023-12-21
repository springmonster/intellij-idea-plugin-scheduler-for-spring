package com.khch.scheduler.annotation;

/**
 * @author KuangHaochuan
 */
public enum SpringScheduledAnnotation {

    SCHEDULED("org.springframework.scheduling.annotation.Scheduled");

    private final String qualifiedName;

    SpringScheduledAnnotation(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }
}
