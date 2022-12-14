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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.caches.KotlinShortNamesCache;

import java.util.*;

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
            List<ScheduledModel> childrenRequests = new ArrayList<>();

            PsiMethod[] psiMethods = psiClass.getMethods();
            for (PsiMethod psiMethod : psiMethods) {
                childrenRequests.addAll(getScheduledAnnotationMethod(psiMethod));
            }
            scheduledModels.addAll(childrenRequests);
        }

        return scheduledModels;
    }

    @NotNull
    private static List<PsiClass> getScheduledAnnotationPsiClassesOfJava(@NotNull Project project, @NotNull Module module) {
        GlobalSearchScope moduleScope = module.getModuleScope();

        List<PsiClass> psiClasses = new ArrayList<>();

        Collection<VirtualFile> virtualFiles = FilenameIndex.getAllFilesByExt(project, "java", moduleScope);

        virtualFiles.forEach(virtualFile -> {
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

        Collection<VirtualFile> virtualFiles = FilenameIndex.getAllFilesByExt(project, "kt", moduleScope);

        virtualFiles.forEach(virtualFile -> {
            PsiClass[] psiClasses = KotlinShortNamesCache.getInstance(project).getClassesByName(virtualFile.getNameWithoutExtension(), moduleScope);
            List<PsiClass> classList = Arrays.asList(psiClasses);
            if (!classList.isEmpty()) {
                psiClassList.add(classList.get(0));
            }
        });

        return psiClassList;
    }

    @NotNull
    private List<ScheduledModel> getScheduledAnnotationMethod(@NotNull PsiMethod method) {
        List<ScheduledModel> scheduledModels = new ArrayList<>();

        PsiAnnotation[] annotations = method.getModifierList().getAnnotations();

        for (PsiAnnotation annotation : annotations) {
            scheduledModels.addAll(getScheduledAnnotationMethod(annotation, method));
        }

        return scheduledModels;
    }

    @NotNull
    private List<ScheduledModel> getScheduledAnnotationMethod(@NotNull PsiAnnotation annotation, @Nullable PsiMethod psiMethod) {
        SpringScheduledAnnotation springScheduledAnnotation = SpringScheduledAnnotation.getByQualifiedName(
                annotation.getQualifiedName()
        );
        if (springScheduledAnnotation == null) {
            return Collections.emptyList();
        }
        List<ScheduledModel> methods = new ArrayList<>();
        methods.add(new ScheduledModel(psiMethod));

        return methods;
    }
}
