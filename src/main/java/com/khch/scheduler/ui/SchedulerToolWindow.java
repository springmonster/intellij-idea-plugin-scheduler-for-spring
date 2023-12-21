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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author KuangHaochuan
 * @version 1.0
 */
public class SchedulerToolWindow {

    private final ScheduledAnnotationScanner scanner = new ScheduledAnnotationScanner();
    private JButton refreshScanningBtn;
    private JPanel toolWindowContent;
    private JTree tree;
    private JScrollPane scheduledJScrollPane;
    private final Project project;

    public SchedulerToolWindow(Project project, ToolWindow toolWindow) {
        this.project = project;
        renderTreeWhenInit();
        initRefreshButton();
    }

    private void renderTreeWhenInit() {
        scheduledJScrollPane.setVisible(false);
        refreshScanningBtn.setVisible(false);

        tree.setCellRenderer(new TreeCellRenderer());
        new TreeSpeedSearch(tree);

        DumbService.getInstance(project).smartInvokeLater(this::scanForSearchingAllAnnotations);

        project.getMessageBus().connect().subscribe(SchedulerTopic.ACTION_SCAN_SERVICE, data -> {
            scanForSearchingAllAnnotations(data);
            scheduledJScrollPane.setVisible(true);
            refreshScanningBtn.setVisible(true);
        });

        this.tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    final int doubleClick = 2;
                    ScheduledModel node = getTreeNodeOfScheduledModel(tree);
                    if (node != null && e.getClickCount() == doubleClick) {
                        node.navigate(true);
                    }
                }
            }
        });
    }

    private void initRefreshButton() {
        refreshScanningBtn.addActionListener(e -> scanForSearchingAllAnnotations());
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

    private void scanForSearchingAllAnnotations(Map<String, List<ScheduledModel>> map) {
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

        DefaultTreeModel model = (DefaultTreeModel) this.tree.getModel();
        model.setRoot(root);

        expandAll(this.tree, new TreePath(this.tree.getModel().getRoot()), true);
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

    private void scanForSearchingAllAnnotations() {
        SchedulerTopic schedulerTopic = project.getMessageBus().syncPublisher(SchedulerTopic.ACTION_SCAN_SERVICE);
        DumbService.getInstance(this.project).runWhenSmart(() -> schedulerTopic.afterAction(getAllScheduledAnnotationMethods()));
    }

    @NotNull
    public Map<String, List<ScheduledModel>> getAllScheduledAnnotationMethods() {
        Map<String, List<ScheduledModel>> map = new HashMap<>();

        Module[] modules = ModuleManager.getInstance(project).getModules();

        for (Module module : modules) {
            List<ScheduledModel> scheduledModels = scanner.getScheduledAnnotationByModule(project, module);
            if (scheduledModels.isEmpty()) {
                continue;
            }
            map.put(module.getName(), scheduledModels);
        }
        return map;
    }

    public JPanel getContent() {
        return toolWindowContent;
    }

}
