// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.khch.scheduler.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.TreeSpeedSearch;
import com.khch.scheduler.SchedulerTopic;
import com.khch.scheduler.model.ScheduledModel;
import com.khch.scheduler.scanner.ScheduledAnnotationScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private JScrollPane scheduledJScrollPane;

    private Project project;

    public SchedulerToolWindow(Project project, ToolWindow toolWindow) {
        this.project = project;
        renderTreeWhenInit();
        initRefreshButton();
    }

    private void initRefreshButton() {
        refreshScanningBtn.addActionListener(e -> renderScheduledAnnotationTree());
    }

    private void renderTreeWhenInit() {
        scheduledJScrollPane.setVisible(false);
        refreshScanningBtn.setVisible(false);

        this.scheduledTree.setCellRenderer(new TreeCellRenderer());
        new TreeSpeedSearch(this.scheduledTree);

        DumbService.getInstance(this.project).smartInvokeLater(this::renderScheduledAnnotationTree);

        project.getMessageBus().connect().subscribe(SchedulerTopic.ACTION_SCAN_SERVICE, data -> {
            if (data instanceof Map) {
                //noinspection unchecked
                renderScheduledAnnotationTree((Map<String, List<ScheduledModel>>) data);
                scheduledJScrollPane.setVisible(true);
                refreshScanningBtn.setVisible(true);
            }
        });

        this.scheduledTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    final int doubleClick = 2;
                    ScheduledModel node = getTreeNodeOfScheduledModel(scheduledTree);
                    if (node != null && e.getClickCount() == doubleClick) {
                        node.navigate(true);
                    }
                }
            }
        });
    }

    @Nullable
    private ScheduledModel getTreeNodeOfScheduledModel(@NotNull JTree tree) {
        DefaultMutableTreeNode sel = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (sel == null) {
            return null;
        }
        Object object = sel.getUserObject();
        if (!(object instanceof ScheduledModel)) {
            return null;
        }
        return (ScheduledModel) object;
    }

    private void renderScheduledAnnotationTree(Map<String, List<ScheduledModel>> map) {
        AtomicInteger controllerCount = new AtomicInteger();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(controllerCount.get());

        map.forEach((moduleName, scheduledModels) -> {
            DefaultMutableTreeNode item = new DefaultMutableTreeNode(String.format(
                    "[%d] %s",
                    scheduledModels.size(),
                    moduleName
            ));
            scheduledModels.forEach(model -> {
                item.add(new DefaultMutableTreeNode(model));
                controllerCount.incrementAndGet();
            });
            root.add(item);
        });

        root.setUserObject(String.format("[%d] %s", controllerCount.get(), this.project.getName()));

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
