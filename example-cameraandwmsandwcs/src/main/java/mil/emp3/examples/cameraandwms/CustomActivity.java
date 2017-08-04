package mil.emp3.examples.cameraandwms;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import org.cmapi.primitives.IGeoAltitudeMode;

import java.net.MalformedURLException;
import java.util.ArrayList;

import mil.emp3.api.WMS;
import mil.emp3.api.WCS;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.examples.maptestfragment.CameraUtility;
import wei.mark.standout.StandOutWindow;

import mil.emp3.examples.cameraandwms.databinding.ActivityCustomBinding;

public class CustomActivity extends Activity {

    private final static String TAG = CustomActivity.class.getSimpleName();
    private WMS wmsService = null;
    private WMS oldWMSService = null;
    private WCS wcsService = null;
    private IMap map = null;
    private ActivityCustomBinding dataBinding;
    private String url;
    private String layer;
    private IGeoAltitudeMode.AltitudeMode altitudeMode = IGeoAltitudeMode.AltitudeMode.ABSOLUTE;
    private final mil.emp3.api.Camera camera = new mil.emp3.api.Camera();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Setting custom activity");
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_custom);
        dataBinding.setCwms(this);
        ArrayAdapter<CharSequence> versionAdapter = ArrayAdapter.createFromResource(this,
                R.array.wms_versions, android.R.layout.simple_spinner_item);
        dataBinding.VersionText.setAdapter(versionAdapter);
        ArrayAdapter<CharSequence> altitudeModeAdapter = ArrayAdapter.createFromResource(this,
                R.array.altitude_mode, android.R.layout.simple_spinner_item);
        dataBinding.AltitudeMode.setAdapter(altitudeModeAdapter);
        /*
        Instantiate a camera and set the location and angle
        The altitude here is set initially to 1000 km
         */

        camera.setName("Main Cam");
        camera.setAltitudeMode(altitudeMode);
        camera.setAltitude(1e5);
        camera.setHeading(0.0);
        camera.setLatitude(40.0);
        camera.setLongitude(-100.0);
        camera.setRoll(0.0);
        camera.setTilt(0.0);

        map = dataBinding.map;
        try {
            map.addMapStateChangeEventListener(mapStateChangeEvent -> {
                Log.d(TAG, "mapStateChangeEvent " + mapStateChangeEvent.getNewState());
                switch (mapStateChangeEvent.getNewState()) {
                    case MAP_READY:
                        try {
                            map.setCamera(camera, false);
                        } catch (EMP_Exception empe) {
                            empe.printStackTrace();
                        }
                        break;
                }
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapStateChangeEventListener", e);
        }
        try {
            map.addMapInteractionEventListener(mapUserInteractionEvent -> Log.d(TAG, "mapUserInteractionEvent " + mapUserInteractionEvent.getPoint().x));
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapInteractionEventListener", e);
        }


    }

    // Cancel button exits the app

    public void onClickCancel(View view) {
        finish();
    }

    // The Zoom- button zooms out 20% each time it is pressed
    // The altitude is limited to 100,000 km

    public void onClickZoomOut(View view) {
        ICamera camera = map.getCamera();
        double initAltitude = camera.getAltitude();
        if (initAltitude <= 1e8 / 1.2) {
            initAltitude *= 1.2;
            camera.setAltitude(initAltitude);
            camera.apply(false);
            Log.i(TAG, "camera altitude " + initAltitude + " latitude " + camera.getLatitude()
                    + " longitude " + camera.getLongitude());
        } else {
            Toast.makeText(CustomActivity.this, "Can't zoom out any more, altitude " + initAltitude, Toast.LENGTH_LONG).show();
        }
    }

    // The Zoom+ button zooms 20% each time it is pressed
    // The altitude is limited to 1 km

    public void onClickZoomIn(View view) {
        ICamera camera = map.getCamera();
        double initAltitude = camera.getAltitude();
        if (initAltitude >= 1.2) {
            initAltitude /= 1.2;
            camera.setAltitude(initAltitude);
            camera.apply(false);
            Log.i(TAG, "camera altitude " + initAltitude + " latitude " + camera.getLatitude()
                    + " longitude " + camera.getLongitude());
        } else {
            Toast.makeText(CustomActivity.this, "Can't zoom in any more, altitude " + initAltitude, Toast.LENGTH_LONG).show();
        }
    }

    // Pan left turns the camera left 5 degrees
    // each time the button is pressed

    public void onClickPanLeft(View v) {
        try {
            ICamera camera = map.getCamera();
            double dPan = camera.getHeading();

            dPan -= 5.0;
            if (dPan < 0.0) {
                dPan += 360.0;
            }

            camera.setHeading(dPan);
            camera.apply(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Pan right turns the camera right 5 degrees
    // each time the button is pressed

    public void onClickPanRight(View v) {
        try {
            ICamera camera = map.getCamera();
            double dPan = camera.getHeading();

            dPan += 5.0;
            if (dPan >= 360.0) {
                dPan -= 360.0;
            }

            camera.setHeading(dPan);
            camera.apply(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Tilt up another 5 degrees, within limits
    public void onClickTiltUp(View v) {
        try {
            ICamera camera = map.getCamera();
            double dTilt = camera.getTilt();

            if (dTilt <= 85.0) {
                dTilt += 5;
                camera.setTilt(dTilt);
                camera.apply(false);
            } else
                Toast.makeText(CustomActivity.this, "Can't tilt any higher", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Tilt down another 5 degrees, within limits

    public void onClickTiltDown(View v) {
        try {
            ICamera camera = map.getCamera();
            double dTilt = camera.getTilt();

            if (dTilt >= -85.0) {
                dTilt -= 5;
                camera.setTilt(dTilt);
                camera.apply(false);
            } else
                Toast.makeText(CustomActivity.this, "Can't tilt any lower", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickRollCCW(View v) {
        try {
            ICamera camera = map.getCamera();
            double dRoll = camera.getRoll();

            if (dRoll >= -175.0) {
                dRoll -= 5;
                camera.setTilt(dRoll);
                camera.apply(false);
            } else
                Toast.makeText(CustomActivity.this, "Can't tilt any lower", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onClickRollCW(View v) {
        try {
            ICamera camera = map.getCamera();
            double dRoll = camera.getRoll();

            if (dRoll <= 175.0) {
                dRoll += 5;
                camera.setTilt(dRoll);
                camera.apply(false);
            } else
                Toast.makeText(CustomActivity.this, "Can't tilt any lower", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onClickOK(View v) {

        try {
            url = dataBinding.UrlText.getText().toString();
            String version = dataBinding.VersionText.getSelectedItem().toString();
            WMSVersionEnum wmsVersion = WMSVersionEnum.valueOf(version);
            String altModeString = dataBinding.AltitudeMode.getSelectedItem().toString();
            IGeoAltitudeMode.AltitudeMode altMode = null;
            switch (altModeString) {
                case "CLAMP TO GROUND" :
                    altMode = IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND;
                    break;
                case "RELATIVE TO GROUND" :
                    altMode = IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND;
                    break;
                default:
                    altMode = IGeoAltitudeMode.AltitudeMode.ABSOLUTE;
                    break;
            }
            layer = dataBinding.LayerText.getText().toString();
            ArrayList<String> layers = new ArrayList<>();
            layers.add(layer);
            wmsService = new WMS(url + "wms",
                    wmsVersion,
                    "image/png",
                    true,
                    layers);
            String resolution = dataBinding.ResolutionText.getText().toString();
            wmsService.setLayerResolution(Double.valueOf(resolution));
            if (wmsService != null) {
                if (wmsService != oldWMSService) {
                    if (oldWMSService != null)
                        map.removeMapService(oldWMSService);
                    else
                        Log.i(TAG, "No previous WMS service");
                    map.addMapService(wmsService);
                    oldWMSService = wmsService;
                    if(!(dataBinding.addWCS.isEnabled() || dataBinding.removeWCS.isEnabled())){
                        dataBinding.addWCS.setEnabled(true);
                    }
                    if (altMode != altitudeMode) {
                        camera.setAltitudeMode(altMode);
                        altitudeMode = altMode;
                        camera.apply(false);
                    }
                } else {
                    Log.i(TAG, "Layer unchanged");
                }
            } else {
                Log.i(TAG, "Got null WMS service");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void onClickAddWCS(View v){
        try {
            try {
                wcsService = new WCS(url + "wcs", layer);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Log.i(TAG, wcsService.toString());
            map.addMapService(wcsService);
            ILookAt calculatedLookAt = CameraUtility.setupLookAt(37.5, -105.5, 5000,
                    37.577227, -105.485845, 4374);
            map.setLookAt(calculatedLookAt, false);

            dataBinding.removeWCS.setEnabled(true);
            dataBinding.addWCS.setEnabled(false);
        } catch (EMP_Exception ex) {
        }
    }
    public void onClickRemoveWCS(View v){
        try {
            map.removeMapService(wcsService);
            ILookAt calculatedLookAt = CameraUtility.setupLookAt(37.5, -105.5, 5000,
                    37.577227, -105.485845, 4374);
            map.setLookAt(calculatedLookAt, false);

            dataBinding.removeWCS.setEnabled(false);
            dataBinding.addWCS.setEnabled(true);
        } catch (EMP_Exception ex) {
        }
    }
}
