package mil.emp3.publisher;

import android.app.Application;

import mil.coe.v3.COEv3;

/**
 * Pattern to follow for COEv3 Android applications
 */
public class MyApplication extends Application {

    private static String TAG = MyApplication.class.getName();

    @Override
    public void onCreate(){
        super.onCreate();

        COEv3.initialize(getApplicationContext());
    }

}