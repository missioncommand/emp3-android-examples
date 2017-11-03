package mil.emp3.example_minimap;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import org.cmapi.primitives.IGeoAltitudeMode;

import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IMapStateChangeEventListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MiniMap";
    private IMap map;
    private MiniMapDialog miniMapDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            map = (IMap) findViewById(R.id.map);
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

                    MainActivity.this.miniMapDialog = new MiniMapDialog();
                    MainActivity.this.miniMapDialog.setMap(MainActivity.this.map);
                    MainActivity.this.miniMapDialog.show(MainActivity.this.getFragmentManager(), null);

                }

            });
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }
}
