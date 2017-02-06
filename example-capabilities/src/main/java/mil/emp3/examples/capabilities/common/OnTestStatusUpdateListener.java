package mil.emp3.examples.capabilities.common;

import android.os.Handler;

public interface OnTestStatusUpdateListener {
    void onTestStatusUpdated(String updatedStatus);
    void onTestCompleted(String completedTest);
    Handler getHandler();
}
