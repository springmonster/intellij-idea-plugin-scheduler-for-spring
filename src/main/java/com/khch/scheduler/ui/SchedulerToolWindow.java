// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.khch.scheduler.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.khch.scheduler.SchedulerTopic;
import com.khch.scheduler.model.ScheduledModel;
import com.khch.scheduler.scanner.ScheduledAnnotationScanner;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author KuangHaochuan
 * @version 1.0
 */
public class SchedulerToolWindow {

    private JButton refreshScanningBtn;
    private JPanel toolWindowContent;
    private JTree scheduledTree;
    private JButton testRuleBtn;
    private JTextArea ruleDisplayTxt;

    private Project project;

    public SchedulerToolWindow(Project project, ToolWindow toolWindow) {
        this.project = project;

        refreshScanningBtn.addActionListener(e -> renderScheduledAnnotationTree());

        renderTreeWhenInit();
    }

    private void renderTreeWhenInit() {
        DumbService.getInstance(this.project).smartInvokeLater(this::renderScheduledAnnotationTree);

        project.getMessageBus().connect().subscribe(SchedulerTopic.ACTION_SCAN_SERVICE, data -> {
            if (data instanceof Map) {
                //noinspection unchecked
                renderScheduledAnnotationTree((Map<String, List<ScheduledModel>>) data);
            }
        });
    }

    private void renderScheduledAnnotationTree(Map<String, List<ScheduledModel>> map) {
        AtomicInteger controllerCount = new AtomicInteger();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(controllerCount.get());

        map.forEach((moduleName, scheduledModels) -> {
            DefaultMutableTreeNode item = new DefaultMutableTreeNode(String.format(
                    "[%d]%s",
                    scheduledModels.size(),
                    moduleName
            ));
            scheduledModels.forEach(request -> {
                item.add(new DefaultMutableTreeNode(request));
                controllerCount.incrementAndGet();
            });
            root.add(item);
        });

        root.setUserObject(controllerCount.get());
        DefaultTreeModel model = (DefaultTreeModel) this.scheduledTree.getModel();
        model.setRoot(root);
        expandAll(this.scheduledTree, new TreePath(this.scheduledTree.getModel().getRoot()), true);
    }

    private void expandAll(JTree tree, @NotNull TreePath parent, boolean expand) {
        javax.swing.tree.TreeNode node = (javax.swing.tree.TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration<?> e = node.children(); e.hasMoreElements(); ) {
                javax.swing.tree.TreeNode n = (javax.swing.tree.TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }

        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    private void renderScheduledAnnotationTree() {
        SchedulerTopic schedulerTopic = project.getMessageBus().syncPublisher(SchedulerTopic.ACTION_SCAN_SERVICE);
        DumbService.getInstance(this.project).runWhenSmart(() -> schedulerTopic.afterAction(getAllScheduledAnnotationMethods()));
    }

    @NotNull
    public Map<String, List<ScheduledModel>> getAllScheduledAnnotationMethods() {
        Map<String, List<ScheduledModel>> map = new HashMap<>();

        Module[] modules = ModuleManager.getInstance(this.project).getModules();
        for (Module module : modules) {
            List<ScheduledModel> scheduledModels = getAllScheduledAnnotationMethods(project, module);
            if (scheduledModels.isEmpty()) {
                continue;
            }
            map.put(module.getName(), scheduledModels);
        }
        return map;
    }

    public List<ScheduledModel> getAllScheduledAnnotationMethods(@NotNull Project project, @NotNull Module module) {
        ScheduledAnnotationScanner scanner = new ScheduledAnnotationScanner();
        List<ScheduledModel> scheduledModel = scanner.getScheduledAnnotationByModule(project, module);
        if (!scheduledModel.isEmpty()) {
            return scheduledModel;
        }

        return Collections.emptyList();
    }

    public JPanel getContent() {
        return toolWindowContent;
    }

}
