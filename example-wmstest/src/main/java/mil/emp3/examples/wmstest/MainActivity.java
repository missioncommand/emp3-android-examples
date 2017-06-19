package mil.emp3.examples.wmstest;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.cmapi.primitives.IGeoAltitudeMode;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mil.emp3.api.WMS;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private WMS wmsService = null;
    private IMap map = null;
    private Spinner selectedLayers;
    private Spinner versionText;
    private Spinner tileFormatText;
    private Spinner transparentText;
    ArrayList<String> layers = new ArrayList<>();
    String layer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Setting custom activity");
        setContentView(R.layout.activity_main);
        ArrayAdapter<CharSequence> versionAdapter = ArrayAdapter.createFromResource(this,
                R.array.wms_versions, android.R.layout.simple_spinner_item);
        versionText = (Spinner)findViewById(R.id.VersionText);
        versionText.setAdapter(versionAdapter);
        ArrayAdapter<CharSequence> tileAdapter = ArrayAdapter.createFromResource(this,
                R.array.image_formats, android.R.layout.simple_spinner_item);
        tileFormatText = (Spinner)findViewById(R.id.TileFormatText);
        tileFormatText.setAdapter(tileAdapter);
        ArrayAdapter<CharSequence> booleanAdapter = ArrayAdapter.createFromResource(this,
                R.array.boolean_values, android.R.layout.simple_spinner_item);
        transparentText = (Spinner)findViewById(R.id.TransparentText);
        transparentText.setAdapter(booleanAdapter);
        // Cancel button exits the app
        Button cancelButton = (Button) findViewById(R.id.CancelButton);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
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
            map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
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
                }
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapStateChangeEventListener", e);
        }
        try {
            map.addMapInteractionEventListener(new IMapInteractionEventListener() {
                @Override
                public void onEvent(MapUserInteractionEvent mapUserInteractionEvent) {
                    Log.d(TAG, "mapUserInteractionEvent " + mapUserInteractionEvent.getPoint().x);
                }
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapInteractionEventListener", e);
        }

        Button loopButton = (Button) findViewById(R.id.LoopButton);
        if (loopButton != null) {
            loopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Runnable remove = new Runnable() {
                        @Override
                        public void run() {
                            try

                            {
                                for (int i = 0; i < 5; i++) {
                                    List<IMapService> mapServices = map.getMapServices();
                                    Log.i(TAG, "map services count before " + mapServices.size());
                                    for (IMapService mapService : mapServices) {
                                        if (mapService.getGeoId().equals(wmsService.getGeoId())) {
                                            map.removeMapService(wmsService);
                                            break;
                                        }
                                    }
                                    Log.i(TAG, "map services count after " + map.getMapServices().size());
                                    Thread.sleep(2000);
                                    map.addMapService(wmsService);
                                    Thread.sleep(2000);
                                }
                            } catch (
                                    Exception e)

                            {
                                e.printStackTrace();
                            }
                        }
                    };
                    Thread thread = new Thread(remove);
                    thread.start();
                }
            });
        }

        Button removeButton = (Button) findViewById(R.id.RemoveButton);
        if (removeButton != null) {
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        map.removeMapService(wmsService);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        // Pressing OK sets the WMS server
        // Only one WMS layer is displayed at a time
        // Changing the server parameters replaces any
        // previous server

        Button okButton = (Button) findViewById(R.id.OKButton);
        if (okButton != null)

        {
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (wmsService == null) {
                            EditText urlText = (EditText) findViewById(R.id.UrlText);
                            EditText layerName = (EditText) findViewById(R.id.LayerText);
                            EditText resolutionText = (EditText) findViewById(R.id.ResolutionText);
                            String url = urlText.getText().toString();
                            String version = versionText.getSelectedItem().toString();
                            WMSVersionEnum wmsVersion = WMSVersionEnum.valueOf(version);
                            String tileFormat = tileFormatText.getSelectedItem().toString();
                            boolean transparent = (transparentText.getSelectedItem().toString()).equals("true");
                            layer = layerName.getText().toString();
                            layers.add(layer);
                            wmsService = new WMS(url,
                                    wmsVersion,
                                    tileFormat.equals("null") ? null : tileFormat,  // tile format
                                    transparent,
                                    layers);
                            String resolution = resolutionText.getText().toString();
                            wmsService.setLayerResolution(Double.valueOf(resolution));
                        }
                        map.addMapService(wmsService);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}

