package com.khch.scheduler.model;

import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.Nullable;


/**
 * @author KuangHaochuan
 * @version 1.0
 */
public class ScheduledModel {
    private PsiMethod psiMethod;


    public ScheduledModel(@Nullable PsiMethod psiMethod) {
        this.psiMethod = psiMethod;
    }

    public void navigate(boolean requestFocus) {
        if (psiMethod != null) {
            psiMethod.navigate(requestFocus);
        }
    }
}
