package mil.emp3.examples.helloemp3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.cmapi.primitives.IGeoAltitudeMode;

import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IMapStateChangeEventListener;

/**
 * You must install emp3-android-worldwind-version.apk to the target device for this application to work.
 */
public class HelloEmp3Activity extends AppCompatActivity {
    private static String TAG = HelloEmp3Activity.class.getSimpleName();
    IMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_emp3);

        map = (IMap) findViewById(R.id.map);
        try {
            map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                    Log.d(TAG, "mapStateChangeEvent map" + mapStateChangeEvent.getNewState());
                    try {
                        mil.emp3.api.Camera camera = new mil.emp3.api.Camera();
                        camera.setName("Main Cam");
                        camera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
                        camera.setAltitude(2000000.0);
                        camera.setHeading(0.0);
                        camera.setLatitude(33.9424368);
                        camera.setLongitude(-118.4081222);
                        camera.setRoll(0.0);
                        camera.setTilt(0.0);
                        map.setCamera(camera, false);
                    } catch (EMP_Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }
}
