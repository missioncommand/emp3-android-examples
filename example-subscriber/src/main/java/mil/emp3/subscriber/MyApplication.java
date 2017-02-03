package mil.emp3.subscriber;

import android.app.Application;
import android.util.Log;

import mil.coe.v3.COEv3;

/**
 * Created by deepakkarmarkar on 4/27/2016.
 */
public class MyApplication extends Application {
    private String TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e(TAG, "COEv3 Initialize");
        COEv3.initialize(getApplicationContext());
    }
}
