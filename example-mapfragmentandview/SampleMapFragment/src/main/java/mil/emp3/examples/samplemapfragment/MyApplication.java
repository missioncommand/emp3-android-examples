package mil.emp3.examples.samplemapfragment;

import android.app.Application;

/**
 * Created by deepakkarmarkar on 4/27/2016.
 */
public class MyApplication extends Application {
    private String TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        // To test with COEv3 jar injection uncomment the following two line and also update the build.gradle following
        // guidelines provided in that file.
//        Log.e(TAG, "COEv3 Initialize");
//        COEv3.initialize(getApplicationContext());
    }
}
