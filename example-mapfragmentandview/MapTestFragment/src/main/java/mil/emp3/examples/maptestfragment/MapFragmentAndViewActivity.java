package mil.emp3.examples.maptestfragment;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import mil.emp3.api.events.CameraEvent;
import mil.emp3.api.events.MapViewChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.api.listeners.IMapViewChangeEventListener;
import mil.emp3.examples.common.ExecuteTest;
import mil.emp3.examples.common.OnTestStatusUpdateListener;

abstract public class MapFragmentAndViewActivity extends Emp3Activity implements  MapTestMenuFragment.OnTestSelectedListener, OnTestStatusUpdateListener {
    private static String TAG = MapFragmentAndViewActivity.class.getSimpleName();

    protected TextView testStatus;
    protected mil.emp3.examples.maptestfragment.MapTestMenuFragment testMenuFragment;
    Handler handler = new Handler();
    protected IMap map, map2;

    @Override
    public void onTestSelected(String selectedTest) {
        String startingTest = ExecuteTest.onTestSelected(TAG, this, selectedTest, map, map2);
        if(null != startingTest) {
            onTestStatusUpdated(startingTest);
            testMenuFragment.updateSupportedUserActions(ExecuteTest.getUserActionImpl().getSupportedUserActions(), ExecuteTest.getUserActionImpl().getMoreActions());
        }
    }

    @Override
    public void onUserAction(String userAction) {
        Log.d(TAG, "onUserAction " + userAction);
        ExecuteTest.getUserActionImpl().actOn(userAction);
    }

    @Override
    public void onTestStatusUpdated(final String updatedStatus) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    testStatus.setText(updatedStatus);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onTestCompleted(final String completedTest) {
        if(null != testMenuFragment) {
            testMenuFragment.testComplete(completedTest);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    testStatus.setText(completedTest + " completed");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public Handler getHandler() {
        return handler;
    }

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
