package mil.emp3.examples.wmstest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

import org.cmapi.primitives.IGeoAltitudeMode;

import io.reactivex.schedulers.Schedulers;
import mil.emp3.api.WMS;
import mil.emp3.api.enums.WMSVersionEnum;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;

// The only place this is really useful is the observeOn call
// Elsewhere it Android data binding could have been used

import com.jakewharton.rxbinding2.view.RxView;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private WMS wmsService = null;
    private IMap map = null;
    private Spinner versionText;
    private Spinner tileFormatText;
    private Spinner transparentText;
    private EditText delayText;
    ArrayList<String> layers = new ArrayList<>();
    String layer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Setting custom activity");
        setContentView(R.layout.activity_main);
        ArrayAdapter<CharSequence> versionAdapter = ArrayAdapter.createFromResource(this,
                R.array.wms_versions, android.R.layout.simple_spinner_item);
        versionText = (Spinner) findViewById(R.id.VersionText);
        versionText.setAdapter(versionAdapter);
        ArrayAdapter<CharSequence> tileAdapter = ArrayAdapter.createFromResource(this,
                R.array.image_formats, android.R.layout.simple_spinner_item);
        tileFormatText = (Spinner) findViewById(R.id.TileFormatText);
        tileFormatText.setAdapter(tileAdapter);
        ArrayAdapter<CharSequence> booleanAdapter = ArrayAdapter.createFromResource(this,
                R.array.boolean_values, android.R.layout.simple_spinner_item);
        transparentText = (Spinner) findViewById(R.id.TransparentText);
        transparentText.setAdapter(booleanAdapter);
        // Cancel button exits the app
        Button cancelButton = (Button) findViewById(R.id.CancelButton);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(view -> finish());
        } else {
            Log.e(TAG, "Cancel Button not found");
        }
        /*
        Instantiate a camera and set the location and angle
        The altitude here is set initially to 1000 km
         */

        final mil.emp3.api.Camera camera = new mil.emp3.api.Camera();
        camera.setName("Main Cam");
        camera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
        camera.setAltitude(1e6);
        camera.setHeading(0.0);
        camera.setLatitude(40.0);
        camera.setLongitude(-100.0);
        camera.setRoll(0.0);
        camera.setTilt(0.0);

        map = (IMap) findViewById(R.id.map);
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
                    default:
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

        Button loopButton = (Button) findViewById(R.id.LoopButton);

        RxView.clicks(loopButton)
                .observeOn(Schedulers.newThread())
                .subscribe(aVoid -> {
                            delayText = (EditText) findViewById(R.id.DelayText);
                            String delayStr = delayText.getText().toString();
                            final int delay = Integer.parseInt(delayStr);

                            try

                            {
                                for (int i = 0; i < 5; i++) {

                                    map.removeMapService(wmsService);
                                    Thread.sleep(delay);
                                    map.addMapService(wmsService);
                                    Thread.sleep(delay);
                                }

                            } catch (
                                    Exception e)

                            {
                                e.printStackTrace();
                            }

                        }


                );


        Button removeButton = (Button) findViewById(R.id.RemoveButton);

        RxView.clicks(removeButton)
                .subscribe(aVoid -> {
                    try {
                        map.removeMapService(wmsService);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        // Pressing OK sets the WMS server
        // Only one WMS layer is displayed at a time
        // Changing the server parameters replaces any
        // previous server

        Button okButton = (Button) findViewById(R.id.OKButton);

        RxView.clicks(okButton)
                .subscribe(aVoid -> {
                    try {
                        if (wmsService == null) {
                            EditText urlText = (EditText) findViewById(R.id.UrlText);
                            EditText layerName = (EditText) findViewById(R.id.LayerText);
                            String url = urlText.getText().toString();
                            String version = versionText.getSelectedItem().toString();
                            WMSVersionEnum wmsVersion = WMSVersionEnum.valueOf(version);
                            String tileFormat = tileFormatText.getSelectedItem().toString();
                            boolean transparent = (transparentText.getSelectedItem().toString()).equals("true");
                            layer = layerName.getText().toString();
                            layers.add(layer);
                            wmsService = new WMS(url,
                                    wmsVersion,
                                    "image/png",  // tile format
                                    transparent,
                                    layers);
                            wmsService.setLayerResolution(1.0);
                        }
                        map.addMapService(wmsService);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

}

