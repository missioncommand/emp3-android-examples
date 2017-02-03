package mil.emp3.examples.samplemapviewpgm;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import mil.emp3.api.Camera;
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
import mil.emp3.examples.common.CameraUtility;
import mil.emp3.examples.maptestfragment.MapFragmentAndViewActivity;
import mil.emp3.examples.maptestfragment.MapTestMenuFragment;

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
        properties.put(Property.ENGINE_CLASSNAME.getValue(), "mil.emp3.worldwind.MapInstance");
        properties.put(Property.ENGINE_APKNAME.getValue(), "mil.emp3.worldwind");


        // Begin the transaction
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        // Replace the contents of the container with the new fragment
        testMenuFragment = new MapTestMenuFragment();
        ft.replace(R.id.your_placeholder, testMenuFragment);
        // or ft.add(R.id.your_placeholder, new FooFragment());
        // Complete the changes added above
        ft.commit();

        if (null != savedInstanceState) {
            Log.d(TAG, "onCreate onSavedInstanceState is NOT null");
            restartingActivity = true;
        } else {
            Log.d(TAG, "onCreate onSavedInstanceState is NULL");
        }

        testStatus = (TextView) findViewById(R.id.TestStatus);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, (float) 0.5);
        LinearLayout ll = (LinearLayout) findViewById(R.id.maps);
        View mapView = getLayoutInflater().inflate(R.layout.map_view, null);
        mapView.setLayoutParams(p);
        map = (IMap) mapView;
        map.setName("map");


        Log.d(TAG, "onCreate onSavedInstanceState map ID " + map.getGeoId());

        try {
            map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                    Log.d(TAG, "mapStateChangeEvent arcgis " + mapStateChangeEvent.getNewState());
                    map1Ready = true;
                    try {
                        onMapReady(map);
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

        if (!restartingActivity) {
            try {
                map.swapMapEngine(properties);
                // map.swapMapEngine("mil.emp3.arcgis.MapInstance", "mil.emp3.arcgisapk");

                final ICamera c = new Camera();
                c.setAltitude(1e7);
                map.setCamera(c, false);

            } catch (EMP_Exception e) {
                Log.e(TAG, "map.swapMapEngine failed ", e);
            }
        }
        // IMap empMapView = new MapView(getApplicationContext(),"mil.emp3.worldwind", "mil.emp3.worldwind.MapInstance" );

        ll.addView(mapView);

        View mapView2 = getLayoutInflater().inflate(R.layout.map_view, null);
        mapView2.setLayoutParams(p);

        map2 = (IMap) mapView2;
        map2.setName("map2");


        Log.d(TAG, "onCreate onSavedInstanceState map2 ID " + map2.getGeoId());

        try {
            map2.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                    Log.d(TAG, "mapStateChangeEvent openstreet " + mapStateChangeEvent.getNewState());
                    map2Ready = true;
                    try {
                        onMapReady(map2);
                        map2.setCamera(CameraUtility.buildCamera(33.9424368, -118.4081222, 2000000.0), false);
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
                // map2.swapMapEngine("mil.emp3.openstreet.MapInstance", "mil.emp3.openstreetapk");
                // map.swapMapEngine("mil.emp3.arcgis.MapInstance", "mil.emp3.arcgisapk");

                final ICamera c = new Camera(); //TODO remove me thx
                c.setAltitude(1e7);
                map2.setCamera(c, false);
                
            } catch (EMP_Exception e) {
                Log.e(TAG, "map2.swapMapEngine failed ", e);
            }
        }
        ll.addView(mapView2);
    }
}
