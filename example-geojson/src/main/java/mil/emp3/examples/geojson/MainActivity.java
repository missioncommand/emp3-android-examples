package mil.emp3.examples.geojson;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.cmapi.primitives.IGeoAltitudeMode;

import java.io.InputStream;
import mil.emp3.api.Overlay;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.json.geoJson.EmpGeoJsonParser;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private IMap map = null;
    protected Overlay overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        camera.setAltitude(2e6);
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
                                MainActivity.this.overlay = new Overlay();
                                MainActivity.this.map.addOverlay(MainActivity.this.overlay, true);
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

        Button okButton = (Button) findViewById(R.id.OKButton);
        if (okButton != null)

        {
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                String simpleGeoJsonString =
//                "{\"type\": \"Feature\",\"geometry\": {\"type\": \"Point\",\"coordinates\": [125.6, 10.1]}," +
//                "\"properties\": {\"name\": \"Dinagat Islands\"}}";
                    try (InputStream stream = getApplicationContext().getResources().openRawResource(R.raw.cmapi);){
                        // Sample geoJSON from CMAPI 1.2 document, edited for correctness
                        EmpGeoJsonParser emp = new EmpGeoJsonParser(stream);
                        ICamera camera = MainActivity.this.map.getCamera();
                        for (IFeature feature : emp.getFeatureList()) {
                            MainActivity.this.overlay.addFeature(feature, true);
                        }
                        camera.setLatitude(0);
                        camera.setLongitude(20);
                        camera.apply(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
