package mil.emp3.examples.samplemapfragmentpgm;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import mil.emp3.api.MapFragment;
import mil.emp3.api.enums.Property;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEmpPropertyList;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.api.utils.EmpPropertyList;
import mil.emp3.examples.maptestfragment.CameraUtility;
import mil.emp3.examples.maptestfragment.MapFragmentAndViewActivity;

/**
 * Shows how MapFragment(s) can be added to the display programmatically.
 */
public class MainActivity extends MapFragmentAndViewActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(null == savedInstanceState) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.map1_placeholder, new MapFragment(), "map1_placeholder");
            ft.replace(R.id.map2_placeholder, new MapFragment(), "map2_placeholder");
            ft.commit();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.d(TAG, "onPostCreate");

        // Initialize the name of the class for map engine and the APK that will contain that class.
        // In this example map engine is loaded from a separate APK so APK name is required and
        // You must installed the map engine APK on the target device.

        final IEmpPropertyList properties = new EmpPropertyList();
        properties.put(Property.ENGINE_CLASSNAME.getValue(), "mil.emp3.worldwind.MapInstance");
        properties.put(Property.ENGINE_APKNAME.getValue(), "mil.emp3.worldwind");

        FragmentManager fm = getFragmentManager();

        try {
            map = (IMap) fm.findFragmentByTag("map1_placeholder");
            map.setName("map1");
            if (null == savedInstanceState) {
                map.swapMapEngine(properties);
            }

            map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                    Log.d(TAG, "mapStateChangeEvent map " + mapStateChangeEvent.getNewState());
                    try {
                        onMapReady(map);
                        map.setCamera(CameraUtility.buildCamera(33.9424368, -118.4081222, 2000000.0), false);
                    } catch (EMP_Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (EMP_Exception | NullPointerException e) {
                Log.e(TAG, "map failed to initialize:", e);
        }

        try {
            map2 = (IMap) fm.findFragmentByTag("map2_placeholder");
            map2.setName("map2");
            if (null == savedInstanceState) {
                map2.swapMapEngine(properties);
            }
            map2.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                    Log.d(TAG, "mapStateChangeEvent map2 " + mapStateChangeEvent.getNewState());
                    try {
                        onMapReady(map2);
                        map2.setCamera(CameraUtility.buildCamera(40.7128, -74.0059, 2000000.0), false);
                    } catch (EMP_Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (EMP_Exception | NullPointerException e) {
            Log.e(TAG, "map2 failed to initialize:", e);
        }

    }
}
