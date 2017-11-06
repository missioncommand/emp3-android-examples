package mil.emp3.example_grids;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.cmapi.primitives.IGeoAltitudeMode;

import java.util.Map;

import mil.emp3.api.enums.MapGridTypeEnum;
import mil.emp3.api.enums.UserInteractionEventEnum;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getSimpleName();
    private IMap map;
    private MapGridTypeEnum current = MapGridTypeEnum.DMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                        map.setGridType(current);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            map.addMapInteractionEventListener(new IMapInteractionEventListener() {
                @Override
                public void onEvent(MapUserInteractionEvent event) {
                    try {
                        switch (current) {
                            case NONE:
                                current = MapGridTypeEnum.DMS;
                                break;
                            case DMS:
                                current = MapGridTypeEnum.UTM;
                                break;
                            case UTM:
                                current = MapGridTypeEnum.MGRS;
                                break;
                            case MGRS:
                                current = MapGridTypeEnum.DD;
                                break;
                            case DD:
                                current = MapGridTypeEnum.NONE;
                                break;
                            default:
                        }
                        map.setGridType(current);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }
}
