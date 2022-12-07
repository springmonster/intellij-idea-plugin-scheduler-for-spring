// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.khch.scheduler;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

/**
 * @author KuangHaochuan
 * @version 1.0
 */
public class SchedulerToolWindow {

    private JButton refreshScanningBtn;
    private JButton hideToolWindowButton;
    private JLabel currentDate;
    private JLabel currentTime;
    private JLabel timeZone;
    private JPanel myToolWindowContent;
    private JTree tree1;

    private Project project;

    public SchedulerToolWindow(Project project, ToolWindow toolWindow) {
        this.project = project;

        hideToolWindowButton.addActionListener(e -> toolWindow.hide(null));
//        refreshScanningBtn.addActionListener(e -> currentDateTime());
        refreshScanningBtn.addActionListener(e -> renderScheduledAnnotationTree());

//        this.currentDateTime();
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

//    public void currentDateTime() {
//        // Get current date and time
//        Calendar instance = Calendar.getInstance();
//        currentDate.setText(
//                instance.get(Calendar.DAY_OF_MONTH) + "/"
//                        + (instance.get(Calendar.MONTH) + 1) + "/"
//                        + instance.get(Calendar.YEAR)
//        );
//        currentDate.setIcon(new ImageIcon(getClass().getResource("/scheduler/Calendar-icon.png")));
//        int min = instance.get(Calendar.MINUTE);
//        String strMin = min < 10 ? "0" + min : String.valueOf(min);
//        currentTime.setText(instance.get(Calendar.HOUR_OF_DAY) + ":" + strMin);
//        currentTime.setIcon(new ImageIcon(getClass().getResource("/scheduler/Time-icon.png")));
//        // Get time zone
//        long gmt_Offset = instance.get(Calendar.ZONE_OFFSET); // offset from GMT in milliseconds
//        String str_gmt_Offset = String.valueOf(gmt_Offset / 3600000);
//        str_gmt_Offset = (gmt_Offset > 0) ? "GMT + " + str_gmt_Offset : "GMT - " + str_gmt_Offset;
//        timeZone.setText(str_gmt_Offset);
//        timeZone.setIcon(new ImageIcon(getClass().getResource("/scheduler/Time-zone-icon.png")));
//    }

    public JPanel getContent() {
        return myToolWindowContent;
    }

}
