package mil.emp3.examples.samplemapfragment;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.widget.TextView;

import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.examples.common.CameraUtility;
import mil.emp3.examples.maptestfragment.MapFragmentAndViewActivity;

public class MainActivity extends MapFragmentAndViewActivity {

    private final static String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        testStatus = (TextView) findViewById(R.id.TestStatus);
        FragmentManager fm = getFragmentManager();
        map = (IMap) fm.findFragmentById(R.id.map1);
        if(null != map) {
            try {
                map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                    @Override
                    public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                        Log.d(TAG, "mapStateChangeEvent map1 " + mapStateChangeEvent.getNewState());
                        try {
                            onMapReady(map);
                            map.setCamera(CameraUtility.buildCamera(33.9424368, -118.4081222, 2000000.0), false);
                        } catch (EMP_Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        }

        map2 = (IMap) fm.findFragmentById(R.id.map2);
        if(null != map2) {
            try {
                map2.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                    @Override
                    public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                        Log.d(TAG, "mapStateChangeEvent map2 " + mapStateChangeEvent.getNewState());
                        try {
                            onMapReady(map2);
                            map2.setCamera(CameraUtility.buildCamera(33.9424368, -118.4081222, 2000000.0), false);
                        } catch (EMP_Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        }

        testMenuFragment = ( mil.emp3.examples.maptestfragment.MapTestMenuFragment) getFragmentManager().findFragmentById(R.id.fooFragment);
    }
}
