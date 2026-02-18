package org.javamaster.httpclient.impl.utils;

import consulo.project.Project;
import consulo.project.ui.notification.NotificationDisplayType;
import consulo.project.ui.notification.NotificationGroup;
import consulo.project.ui.wm.ToolWindowId;
import consulo.project.ui.wm.ToolWindowManager;

/**
 * Notification pop-up
 *
 * @author yudong
 */
public class NotifyUtil {
    private static final NotificationGroup STICKY_STICKY_BALLOON =
        new NotificationGroup("HttpClient.STICKY_BALLOON", NotificationDisplayType.STICKY_BALLOON, true);
    private static final String TOOL_WINDOW_ID = ToolWindowId.SERVICES;

    public static void notifyInfo(Project project, String message) {
        notifyServicesBalloon(project, message, consulo.ui.NotificationType.INFO);
    }

    public static void notifyWarn(Project project, String message) {
        notifyServicesBalloon(project, message, consulo.ui.NotificationType.WARNING);
    }

    public static void notifyError(Project project, String message) {
        notifyServicesBalloon(project, message, consulo.ui.NotificationType.ERROR);
    }

    private static void notifyServicesBalloon(Project project, String message, consulo.ui.NotificationType type) {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);

        toolWindowManager.notifyByBalloon(TOOL_WINDOW_ID, type, "<div style='font-size:13pt'>" + message + "</div>");
    }

    public static void notifyCornerSuccess(Project project, String message) {
        STICKY_STICKY_BALLOON.createNotification("Tip", message, consulo.project.ui.notification.NotificationType.INFORMATION, null).notify(project);
    }

    public static void notifyCornerWarn(Project project, String message) {
        STICKY_STICKY_BALLOON.createNotification("Tip", message, consulo.project.ui.notification.NotificationType.WARNING, null).notify(project);
    }

    public static void notifyCornerError(Project project, String message) {
        STICKY_STICKY_BALLOON.createNotification("Tip", message, consulo.project.ui.notification.NotificationType.ERROR, null).notify(project);
    }
}
