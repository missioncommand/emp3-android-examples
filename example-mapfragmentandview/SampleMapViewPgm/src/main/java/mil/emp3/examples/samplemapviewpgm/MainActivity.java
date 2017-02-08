package mil.emp3.examples.samplemapviewpgm;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import mil.emp3.api.enums.Property;
import mil.emp3.api.events.CameraEvent;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEmpPropertyList;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.api.utils.EmpPropertyList;
import mil.emp3.examples.maptestfragment.CameraUtility;
import mil.emp3.examples.maptestfragment.MapFragmentAndViewActivity;

/**
 * Shows how a MapView(s) can be added to the display programmatically.
 */

public class MainActivity extends MapFragmentAndViewActivity {

    private static String TAG = MainActivity.class.getSimpleName();
    boolean map1Ready = false;
    boolean map2Ready = false;

    boolean restartingActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final IEmpPropertyList properties = new EmpPropertyList();

        // Initialize the name of the class for map engine and the APK that will contain that class.
        // If you are compiling the mapengine ito your APP then you don't need to set the APK name
        properties.put(Property.ENGINE_CLASSNAME.getValue(), "mil.emp3.worldwind.MapInstance");
        properties.put(Property.ENGINE_APKNAME.getValue(), "mil.emp3.worldwind");

        if (null != savedInstanceState) {
            Log.d(TAG, "onCreate onSavedInstanceState is NOT null");
            restartingActivity = true;
        } else {
            Log.d(TAG, "onCreate onSavedInstanceState is NULL");
        }

        // Maps will be added to the Linear Layout.
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, (float) 0.5);
        LinearLayout ll = (LinearLayout) findViewById(R.id.maps);

        // Infralte the map view and assign it a name.
        View mapView = getLayoutInflater().inflate(R.layout.map_view, null);
        mapView.setLayoutParams(p);
        map = (IMap) mapView;
        map.setName("map");


        Log.d(TAG, "onCreate onSavedInstanceState map ID " + map.getGeoId());

        try {
            map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                    Log.d(TAG, "mapStateChangeEvent worldwind 1 " + mapStateChangeEvent.getNewState());
                    map1Ready = true;
                    try {
                        onMapReady(map);
                        // Map shows West coast of US
                        map.setCamera(CameraUtility.buildCamera(33.9424368, -118.4081222, 2000000.0), false);
                    } catch (EMP_Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapStateChangeEventListener", e);
        }

        try {
            map.addCameraEventListener(new ICameraEventListener() {
                @Override
                public void onEvent(CameraEvent event) {
                    ICamera camera = event.getCamera();
                    Log.d(TAG, "Alt:Lat:Long:" + camera.getAltitude() + ":" + camera.getLatitude() + ":" + camera.getLongitude());
                    }
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addCameraEventListener", e);
        }

        // Assign a map engine to the MapView using swapMapEngine method of IMap. You shouldn't do that
        // if activity is just starting. Always assign the camera to some know value.
        if (!restartingActivity) {
            try {
                map.swapMapEngine(properties);
                // Map shows West coast of US
                map.setCamera(CameraUtility.buildCamera(33.9424368, -118.4081222, 2000000.0), false);

            } catch (EMP_Exception e) {
                Log.e(TAG, "map.swapMapEngine failed ", e);
            }
        }

        // Add the map to the layout.
        ll.addView(mapView);

        // Perform the same actions for second instance of the map as above and add it to the layout.
        View mapView2 = getLayoutInflater().inflate(R.layout.map_view, null);
        mapView2.setLayoutParams(p);

        map2 = (IMap) mapView2;
        map2.setName("map2");


        Log.d(TAG, "onCreate onSavedInstanceState map2 ID " + map2.getGeoId());

        try {
            map2.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                    Log.d(TAG, "mapStateChangeEvent worldwind 2 " + mapStateChangeEvent.getNewState());
                    map2Ready = true;
                    try {
                        onMapReady(map2);
                        // Map shows East coast of US
                        map2.setCamera(CameraUtility.buildCamera(40.7128, -74.0059, 2000000.0), false);
                    } catch (EMP_Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapStateChangeEventListener", e);
        }

        if (!restartingActivity) {
            try {
                map2.swapMapEngine(properties);
                // Map shows East coast of US
                map2.setCamera(CameraUtility.buildCamera(40.7128, -74.0059, 2000000.0), false);
            } catch (EMP_Exception e) {
                Log.e(TAG, "map2.swapMapEngine failed ", e);
            }
        }
        ll.addView(mapView2);
    }
}
