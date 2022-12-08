package com.khch.scheduler.ui;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.khch.scheduler.model.ScheduledModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author KuangHaochuan
 * @version 1.0
 */
public class TreeCellRenderer extends ColoredTreeCellRenderer {

    @Override
    public void customizeCellRenderer(
            @NotNull JTree tree, Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row, boolean hasFocus) {
        Object obj = ((DefaultMutableTreeNode) value).getUserObject();
        if (obj instanceof ScheduledModel) {
            ScheduledModel node = (ScheduledModel) obj;
            setIcon(node);
            append(node.toString(), SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES);
        } else if (obj instanceof String) {
            append((String) obj, SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES);
        } else if (obj instanceof Integer) {
            append(String.format("Find %s @Scheduled", obj));
        }
    }

    private void setIcon(@Nullable ScheduledModel node) {
        if (node == null) {
            return;
        }
        setIcon(new ImageIcon(getClass().getResource("/scheduler/schedule.png")));
    }
}
