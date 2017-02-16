package mil.emp3.examples.maptestfragment;

import android.util.Log;

import mil.emp3.api.events.CameraEvent;
import mil.emp3.api.events.MapViewChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.api.listeners.IMapViewChangeEventListener;

abstract public class MapFragmentAndViewActivity extends Emp3Activity {
    private static String TAG = MapFragmentAndViewActivity.class.getSimpleName();

    protected IMap map, map2;

    public void onMapReady(IMap xMap) {
        try {
            xMap.addCameraEventListener(new ICameraEventListener() {
                @Override
                public void onEvent(CameraEvent event) {
                    Log.d(TAG, "Camera Am:" + event.getCamera().getAltitudeMode() +
                            " At:" + event.getCamera().getAltitude() +
                            " Lt:" + event.getCamera().getLatitude() +
                            " Ln:" + event.getCamera().getLongitude() +
                            " Hd:" + event.getCamera().getHeading() +
                            " Tl:" + event.getCamera().getTilt() +
                            " Rl:" + event.getCamera().getRoll());
                }
            });

            xMap.addMapViewChangeEventListener(new IMapViewChangeEventListener() {
                @Override
                public void onEvent(MapViewChangeEvent event) {
                    Log.d(TAG, "Camera Am:" + event.getCamera().getAltitudeMode() +
                            " At:" + event.getCamera().getAltitude() +
                            " Lt:" + event.getCamera().getLatitude() +
                            " Ln:" + event.getCamera().getLongitude() +
                            " Hd:" + event.getCamera().getHeading() +
                            " Tl:" + event.getCamera().getTilt() +
                            " Rl:" + event.getCamera().getRoll());
                }
            });
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }
}
