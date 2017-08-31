package mil.emp3.examples.wmts;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import org.cmapi.primitives.IGeoAltitudeMode;

import mil.emp3.api.WMTS;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private WMTS wmtsService = null;
    private WMTS oldWMTSService = null;
    private IMap map = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Setting custom activity");
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

        // The Zoom- button zooms out 20% each time it is pressed
        // The altitude is limited to 100,000 km
        Button zoomOutButton = (Button) findViewById(R.id.ZoomOut);
        if (zoomOutButton != null) {
            zoomOutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ICamera camera = map.getCamera();
                    double initAltitude = camera.getAltitude();
                    if (initAltitude <= 1e8 / 1.2) {
                        initAltitude *= 1.2;
                        camera.setAltitude(initAltitude);
                        camera.apply(false);
                        Log.i(TAG, "camera altitude " + initAltitude + " latitude " + camera.getLatitude()
                                + " longitude " + camera.getLongitude());
                    } else {
                        Toast.makeText(MainActivity.this, "Can't zoom out any more, altitude " + initAltitude, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Log.e(TAG, "Zoom out button not found");
        }
        // The Zoom+ button zooms 20% each time it is pressed
        // The altitude is limited to 1 km
        Button zoomInButton = (Button) findViewById(R.id.ZoomIn);
        if (zoomInButton != null) {
            zoomInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ICamera camera = map.getCamera();
                    double initAltitude = camera.getAltitude();
                    if (initAltitude >= 1.2) {
                        initAltitude /= 1.2;
                        camera.setAltitude(initAltitude);
                        camera.apply(false);
                        Log.i(TAG, "camera altitude " + initAltitude + " latitude " + camera.getLatitude()
                                + " longitude " + camera.getLongitude());
                    } else {
                        Toast.makeText(MainActivity.this, "Can't zoom in any more, altitude " + initAltitude, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Log.e(TAG, "Zoom in button not found");
        }
        // Pan left turns the camera left 5 degrees
        // each time the button is pressed
        Button panLeft = (Button) findViewById(R.id.PanLeft);
        if (panLeft != null) {
            panLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
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
            });
        } else {
            Log.e(TAG, "Pan left button not found");
        }
        // Pan right turns the camera right 5 degrees
        // each time the button is pressed
        Button panRight = (Button) findViewById(R.id.PanRight);
        if (panRight != null) {
            panRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
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
            });
        } else {
            Log.e(TAG, "Pan right button not found");
        }

        // Tilt up another 5 degrees, within limits
        Button tiltUp = (Button) findViewById(R.id.TiltUp);
        if (tiltUp != null) {
            tiltUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        double dTilt = camera.getTilt();

                        if (dTilt <= 85.0) {
                            dTilt += 5;
                            camera.setTilt(dTilt);
                            camera.apply(false);
                        } else
                            Toast.makeText(MainActivity.this, "Can't tilt any higher", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.e(TAG, "Tilt up button not found");
        }

        // Tilt down another 5 degrees, within limits
        Button tiltDown = (Button) findViewById(R.id.TiltDown);
        if (tiltDown != null) {
            tiltDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        double dTilt = camera.getTilt();

                        if (dTilt >= -85.0) {
                            dTilt -= 5;
                            camera.setTilt(dTilt);
                            camera.apply(false);
                        } else
                            Toast.makeText(MainActivity.this, "Can't tilt any lower", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.e(TAG, "Tilt down button not found");
        }

        Button rollCCW = (Button) findViewById(R.id.rollCCW);
        if (rollCCW != null) {
            rollCCW.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    try {
                        double dRoll = camera.getRoll();

                        if (dRoll >= -175.0) {
                            dRoll -= 5;
                            camera.setTilt(dRoll);
                            camera.apply(false);
                        } else
                            Toast.makeText(MainActivity.this, "Can't tilt any lower", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        Button rollCW = (Button) findViewById(R.id.rollCW);
        if (rollCW != null) {
            rollCW.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    try {
                        double dRoll = camera.getRoll();

                        if (dRoll <= 175.0) {
                            dRoll += 5;
                            camera.setTilt(dRoll);
                            camera.apply(false);
                        } else
                            Toast.makeText(MainActivity.this, "Can't tilt any lower", Toast.LENGTH_SHORT).show();
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
                    EditText urlText = (EditText) findViewById(R.id.UrlText);
                    EditText layerName = (EditText) findViewById(R.id.LayerText);

                    try {
                        String url = urlText.getText().toString();
                        String layer = layerName.getText().toString();
                        ArrayList<String> layers = new ArrayList<>();
                        layers.add(layer);
                        wmtsService = new WMTS(
                                url,
                                null, null, layers);
                        map.addMapService(MainActivity.this.wmtsService);
                        ICamera camera = map.getCamera();
                        camera.setLatitude(64.27);
                        camera.setLongitude(10.12);
                        camera.setAltitude(225000);
                        camera.apply(false);
                        if (wmtsService != null) {
                            if (wmtsService != oldWMTSService) {
                                if (oldWMTSService != null)
                                    map.removeMapService(oldWMTSService);
                                else
                                    Log.i(TAG, "No previous WMTS service");
                                map.addMapService(wmtsService);
                                oldWMTSService = wmtsService;
                            } else {
                                Log.i(TAG, "Layer unchanged");
                            }
                        } else {
                            Log.i(TAG, "Got null WMTS service");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
