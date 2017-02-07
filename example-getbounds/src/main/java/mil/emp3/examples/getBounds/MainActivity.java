package mil.emp3.examples.getBounds;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.examples.maptestfragment.MapFragmentAndViewActivity;

public class MainActivity extends MapFragmentAndViewActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testStatus = (TextView) findViewById(R.id.TestStatus);
        map = (IMap) findViewById(R.id.map);
        try {
            map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                    Log.d(TAG, "mapStateChangeEvent map" + mapStateChangeEvent.getNewState());
                }
            });
        }catch (EMP_Exception e) {
            Log.e(TAG, "addMapStateChangeEventListener", e);
        }

        map2 = (IMap) findViewById(R.id.map2);
        try {
            map2.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                    Log.d(TAG, "mapStateChangeEvent map" + mapStateChangeEvent.getNewState());
                }
            });
        }catch (EMP_Exception e) {
            Log.e(TAG, "addMapStateChangeEventListener", e);
        }

        Runnable test = new mil.emp3.examples.GetBoundsTest(this, map, map2);

        if(null != test) {
            Thread testThread = new Thread(test);
            testThread.start();
        }

        onTestStatusUpdated("Starting GetBoundsTest");
    }
}
