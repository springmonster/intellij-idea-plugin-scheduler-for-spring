package com.khch.scheduler.scanner;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.java.stubs.index.JavaShortClassNameIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.khch.scheduler.annotation.SpringScheduledAnnotation;
import com.khch.scheduler.model.ScheduledModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.caches.KotlinShortNamesCache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author KuangHaochuan
 * @version 1.0
 */
public class ScheduledAnnotationScanner {

    @NotNull
    public List<ScheduledModel> getScheduledAnnotationByModule(@NotNull Project project, @NotNull Module module) {
        List<ScheduledModel> scheduledModels = new ArrayList<>();

        List<PsiClass> psiClasses = getScheduledAnnotationPsiClassesOfJava(project, module);
        psiClasses.addAll(getScheduledAnnotationPsiClassesOfKotlin(project, module));

        if (psiClasses.isEmpty()) {
            return scheduledModels;
        }

        for (PsiClass psiClass : psiClasses) {
            PsiMethod[] psiMethods = psiClass.getMethods();
            for (PsiMethod psiMethod : psiMethods) {
                ScheduledModel scheduledModel = getScheduledAnnotationMethod(psiMethod);
                if (scheduledModel != null) {
                    scheduledModels.add(scheduledModel);
                }
            }
        }

        return scheduledModels;
    }

    @NotNull
    private static List<PsiClass> getScheduledAnnotationPsiClassesOfJava(@NotNull Project project, @NotNull Module module) {
        GlobalSearchScope moduleScope = module.getModuleScope();

        List<PsiClass> psiClasses = new ArrayList<>();

        Collection<VirtualFile> java = FilenameIndex.getAllFilesByExt(project, "java", moduleScope);

        java.forEach(virtualFile -> {
            List<PsiClass> psiClassList = (List<PsiClass>) JavaShortClassNameIndex.getInstance().get(virtualFile.getNameWithoutExtension(), project, moduleScope);
            if (psiClassList != null && !psiClassList.isEmpty()) {
                psiClasses.add(psiClassList.get(0));
            }
        });

        return psiClasses;
    }

    @NotNull
    private static List<PsiClass> getScheduledAnnotationPsiClassesOfKotlin(@NotNull Project project, @NotNull Module module) {
        GlobalSearchScope moduleScope = module.getModuleScope();

        List<PsiClass> psiClassList = new ArrayList<>();

        Collection<VirtualFile> kt = FilenameIndex.getAllFilesByExt(project, "kt", moduleScope);

        kt.forEach(virtualFile -> {
            PsiClass[] psiClasses = KotlinShortNamesCache.getInstance(project).getClassesByName(virtualFile.getNameWithoutExtension(), moduleScope);
            List<PsiClass> classList = Arrays.asList(psiClasses);
            if (!classList.isEmpty()) {
                psiClassList.add(classList.get(0));
            }
        });

        return psiClassList;
    }

    private ScheduledModel getScheduledAnnotationMethod(@NotNull PsiMethod method) {
        PsiAnnotation[] annotations = method.getModifierList().getAnnotations();

        for (PsiAnnotation annotation : annotations) {
            if (SpringScheduledAnnotation.SCHEDULED.getQualifiedName().equals(annotation.getQualifiedName())) {
                return new ScheduledModel(method);
            }
        }

        return null;
    }
}
