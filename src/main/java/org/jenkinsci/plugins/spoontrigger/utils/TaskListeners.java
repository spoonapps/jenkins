package org.jenkinsci.plugins.spoontrigger.utils;

import com.google.common.base.Throwables;
import hudson.model.TaskListener;

public final class TaskListeners {

    public static void logFatalError(TaskListener listener, IllegalStateException ex) {
        final String msg = getFailureMessage(ex);
        listener.fatalError(msg);
    }

    private static String getFailureMessage(IllegalStateException ex) {
        if (ex.getCause() == null) {
            return ex.getMessage();
        }

        return String.format("%s%n%s", ex.getMessage(), Throwables.getStackTraceAsString(ex));
    }
}
