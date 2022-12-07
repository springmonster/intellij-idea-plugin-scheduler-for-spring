package com.khch.scheduler;

import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @author KuangHaochuan
 * @version 1.0
 */
public class ScheduledModel {
    private String path;

    private PsiMethod psiMethod;


    public ScheduledModel(@Nullable String path, @Nullable PsiMethod psiMethod) {
        if (path != null) {
            this.setPath(path);
        }
        this.psiMethod = psiMethod;
    }

    public ScheduledModel(@Nullable String path) {
        if (path != null) {
            this.setPath(path);
        }
    }

    public void navigate(boolean requestFocus) {
        if (psiMethod != null) {
            psiMethod.navigate(requestFocus);
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(@NotNull String path) {
        path = path.trim();
        if (!path.startsWith("/") && !path.contains("https") && !path.contains("http")) {
            path = "/" + path;
        }
        this.path = path;
    }
}
