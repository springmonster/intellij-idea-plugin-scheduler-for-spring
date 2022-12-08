/**
 * @author KuangHaochuan
 */
package com.khch.scheduler.scanner;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaShortClassNameIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.khch.scheduler.annotation.SpringScheduledAnnotation;
import com.khch.scheduler.model.ScheduledModel;
import com.khch.scheduler.utils.ScannerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
//        controllers.addAll(getAllRetrofitKotlinClass(project, module));

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
        GlobalSearchScope moduleScope = ScannerUtil.getModuleScope(module);

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

    public static List<String> getAnnotationAttributeValues(PsiAnnotation annotation, String attr) {
        PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue(attr);

        List<String> values = new ArrayList<>();
        //只有注解
        //一个值 class com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl
        //多个值  class com.intellij.psi.impl.source.tree.java.PsiArrayInitializerMemberValueImpl
        if (value instanceof PsiReferenceExpression) {
            PsiReferenceExpression expression = (PsiReferenceExpression) value;
            values.add(expression.getText());
        } else if (value instanceof PsiLiteralExpression) {
//            values.add(psiNameValuePair.getLiteralValue());
            values.add(((PsiLiteralExpression) value).getValue().toString());
        } else if (value instanceof PsiArrayInitializerMemberValue) {
            PsiAnnotationMemberValue[] initializers = ((PsiArrayInitializerMemberValue) value).getInitializers();

            for (PsiAnnotationMemberValue initializer : initializers) {
                values.add(initializer.getText().replaceAll("\\\"", ""));
            }
        }

        return values;
    }
}
