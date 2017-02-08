package mil.emp3.examples.samplemapview;

import android.os.Bundle;
import android.util.Log;

import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.examples.maptestfragment.CameraUtility;
import mil.emp3.examples.maptestfragment.MapFragmentAndViewActivity;

/**
 * Shows how MapView(s) can be added to the display via a layout configuration file.
 */
public class MainActivity extends MapFragmentAndViewActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        map = (IMap) findViewById(R.id.map);
        try {
            map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                    Log.d(TAG, "mapStateChangeEvent map" + mapStateChangeEvent.getNewState());
                    try {
                        if(null == savedInstanceState) {
                            onMapReady(map);
                            // Map shows West coast of US
                            map.setCamera(CameraUtility.buildCamera(33.9424368, -118.4081222, 2000000.0), false);
                        }
                    } catch (EMP_Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }

        map2 = (IMap) findViewById(R.id.map2);
        try {
            map2.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                    Log.d(TAG, "mapStateChangeEvent map" + mapStateChangeEvent.getNewState());
                    try {
                        if(null == savedInstanceState) {
                            onMapReady(map2);
                            // Map shows East coast of US
                            map2.setCamera(CameraUtility.buildCamera(40.7128, -74.0059, 2000000.0), false);
                        }
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
