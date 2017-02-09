package mil.emp3.examples.geojson;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.cmapi.primitives.IGeoAltitudeMode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import mil.emp3.api.Overlay;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.json.geoJson.GeoJsonParser;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private IMap map = null;
    private Overlay overlay;
    private Spinner geoJson;

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
        final ArrayAdapter<CharSequence> geoJsonAdapter = ArrayAdapter.createFromResource(this,
                R.array.geojson, android.R.layout.simple_spinner_item);
        geoJson = (Spinner)findViewById(R.id.geojsonfile);
        geoJson.setAdapter(geoJsonAdapter);

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
                    InputStream stream = null;
                    try {
                        String selection = geoJson.getSelectedItem().toString();
                        ICamera camera = MainActivity.this.map.getCamera();
                        switch (selection) {
                            case "communes":
                                stream = getApplicationContext().getResources().openRawResource(R.raw.communes_69);
                                List<IFeature> featureList = GeoJsonParser.parse(stream);
                                for (IFeature feature : featureList) {
                                    MainActivity.this.overlay.addFeature(feature, true);
                                }
                                stream.close();
                                camera.setLatitude(45.7);
                                camera.setLongitude(5.2);
                                camera.setAltitude(5e5);
                                break;
                            case "random":
                                stream = getApplicationContext().getResources().openRawResource(R.raw.random_geoms);
                                featureList = GeoJsonParser.parse(stream);
                                for (IFeature feature : featureList) {
                                    MainActivity.this.overlay.addFeature(feature, true);
                                }
                                stream.close();
                                camera.setLatitude(48.0);
                                camera.setLongitude(-1);
                                camera.setAltitude(5e4);
                                break;
                            case "rhone":
                                stream = getApplicationContext().getResources().openRawResource(R.raw.rhone);
                                featureList = GeoJsonParser.parse(stream);
                                for (IFeature feature : featureList) {
                                    MainActivity.this.overlay.addFeature(feature, true);
                                }
                                stream.close();
                                camera.setLatitude(46.2);
                                camera.setLongitude(6.0);
                                camera.setAltitude(5e5);
                                break;
                            case "cmapi":
                                stream = getApplicationContext().getResources().openRawResource(R.raw.cmapi);
                                featureList = GeoJsonParser.parse(stream);
                                for (IFeature feature : featureList) {
                                    MainActivity.this.overlay.addFeature(feature, true);
                                }
                                camera.setLatitude(0);
                                camera.setLongitude(20);
                                camera.setAltitude(2e6);
                                break;
                        }
                        camera.apply(false);
                    } catch (IOException | EMP_Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (stream != null) {
                                stream.close();
                            }
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                }
            });
        }
    }
}
