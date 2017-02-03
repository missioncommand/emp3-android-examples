package mil.emp3.examples.common;

import android.os.Handler;

/**
 * Created by deepakkarmarkar on 6/14/2016.
 */
public interface OnTestStatusUpdateListener {
    void onTestStatusUpdated(String updatedStatus);
    void onTestCompleted(String completedTest);
    Handler getHandler();
}
