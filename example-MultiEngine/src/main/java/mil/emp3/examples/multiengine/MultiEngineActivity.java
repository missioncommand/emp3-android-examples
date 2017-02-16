package mil.emp3.examples.multiengine;

import android.app.FragmentManager;
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
public class MultiEngineActivity extends AppCompatActivity {

    private static String TAG = MultiEngineActivity.class.getSimpleName();
    private IMap map;
    private IMap map2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_engine);

        FragmentManager fm = getFragmentManager();
        map = (IMap) fm.findFragmentById(R.id.wwmap);
        if(null != map) {
            try {
                map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                    @Override
                    public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                        Log.d(TAG, "mapStateChangeEvent map1 " + mapStateChangeEvent.getNewState());
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

        // If you are running on Android platform 6 and higher make sure you have turned on the 'Storage'
        // permissions. You can do that by going to settings, Application, ApplicationsManage and then
        // selecting MultiEngine APK.
        map2 = (IMap) fm.findFragmentById(R.id.wwmap2);
        if(null != map2) {
            try {
                map2.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                    @Override
                    public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                        Log.d(TAG, "mapStateChangeEvent map2 " + mapStateChangeEvent.getNewState());
                        try {
                            mil.emp3.api.Camera camera = new mil.emp3.api.Camera();
                            camera.setName("Main Cam");
                            camera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
                            camera.setAltitude(2000000.0);
                            camera.setHeading(0.0);
                            camera.setLatitude(40.7128);
                            camera.setLongitude(-74.0059);
                            camera.setRoll(0.0);
                            camera.setTilt(0.0);
                            map2.setCamera(camera, false);
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
}
