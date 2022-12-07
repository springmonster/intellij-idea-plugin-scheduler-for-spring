package com.khch.scheduler.utils;

import com.intellij.openapi.module.Module;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

public class ScannerUtil {
    public static GlobalSearchScope getModuleScope(@NotNull Module module) {
        return module.getModuleScope();
    }
}
