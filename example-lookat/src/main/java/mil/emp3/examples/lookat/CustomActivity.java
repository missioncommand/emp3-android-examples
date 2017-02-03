package mil.emp3.examples.lookat;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoPosition;

import mil.emp3.api.Camera;
import mil.emp3.api.LookAt;
import mil.emp3.api.Text;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.events.MapViewChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.api.listeners.IMapViewChangeEventListener;

/* This app sets the lookAt point to LAX airport as seen from an aircraft above Santa Monica.
 * When you start the app, you see the horizon.  You can zoom in or out using pinch or change
 * tilt with touch.
 */

public class CustomActivity extends AppCompatActivity {

    private final static String TAG = CustomActivity.class.getSimpleName();
    private static final double OFFICIAL_SEMI_MAJOR_AXIS = 6378137.0;
    private static final double OFFICIAL_SEMI_MINOR_AXIS = 6356752.314245;
    private static final double OFFICIAL_EC2 = 6.69437999014E-3;
    private static final double INVERSE_FLATTENING = 298.257223563;

    private IMap map = null;

    private ILookAt lookAt = new LookAt();
    private ICamera camera = new Camera();
    private IGeoPosition aircraft = null;  // "camera location"
    private IGeoPosition airport = null;   // "lookAt point"
    private boolean firstTime = true;
    private TextView latitude;
    private TextView longitude;

    public double getRadiusAt(double latitude, double longitude) {
        // The radius for an ellipsoidal globe is a function of its latitude. The following solution was derived by
        // observing that the length of the ellipsoidal point at the specified latitude and longitude indicates the
        // radius at that location. The formula for the length of the ellipsoidal point was then converted into the
        // simplified form below.

        double f = 1 / INVERSE_FLATTENING;
        double equatorialRadius = OFFICIAL_SEMI_MAJOR_AXIS;
        double eccentricitySquared = 2 * f - f * f;
        double sinLat = Math.sin(Math.toRadians(latitude));
        double ec2 = eccentricitySquared;
        double rpm = equatorialRadius / Math.sqrt(1 - ec2 * sinLat * sinLat);
        return rpm * Math.sqrt(1 + (ec2 * ec2 - 2 * ec2) * sinLat * sinLat);
    }

    /**
     * Computes the azimuth angle (clockwise from North) for the great circle path between this location and a specified
     * location. This angle can be used as the starting azimuth for a great circle path beginning at this location, and
     * passing through the specified location. This function uses a spherical model, not elliptical.
     */
    private double greatCircleAzimuth(double lat1, double lon1, double lat2, double lon2) {

        if (lat1 == lat2 && lon1 == lon2) {
            return 0;
        }

        if (lon1 == lon2) {
            return lat1 > lat2 ? 180 : 0;
        }

        // Taken from "Map Projections - A Working Manual", page 30, equation 5-4b.
        // The atan2() function is used in place of the traditional atan(y/x) to simplify the case when x == 0.
        double y = Math.cos(lat2) * Math.sin(lon2 - lon1);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1);
        double azimuthRadians = Math.atan2(y, x);

        return Double.isNaN(azimuthRadians) ? 0 : Math.toDegrees(azimuthRadians);
    }

    /**
     * Computes the angular distance of the great circle path between this location and a specified location. In
     * radians, this angle is the arc length of the segment between the two locations. To compute a distance in meters
     * from this value, multiply the return value by the radius of the globe. This function uses a spherical model, not
     * elliptical.
     */
    private double greatCircleDistance(double lat1Radians, double lon1Radians, double lat2Radians, double lon2Radians) {
        if (lat1Radians == lat2Radians && lon1Radians == lon2Radians) {
            return 0;
        }

        // "Haversine formula," taken from http://en.wikipedia.org/wiki/Great-circle_distance#Formul.C3.A6
        double a = Math.sin((lat2Radians - lat1Radians) / 2.0);
        double b = Math.sin((lon2Radians - lon1Radians) / 2.0);
        double c = a * a + Math.cos(lat1Radians) * Math.cos(lat2Radians) * b * b;
        double distanceRadians = 2.0 * Math.asin(Math.sqrt(c));

        return Double.isNaN(distanceRadians) ? 0 : distanceRadians;
    }

    private void updateLookAt() {
        // Compute heading and distance from aircraft to airport
        double heading = greatCircleAzimuth(aircraft.getLatitude(),
                aircraft.getLongitude(),
                airport.getLatitude(),
                airport.getLongitude());
        double distanceRadians = greatCircleDistance(aircraft.getLatitude(),
                aircraft.getLongitude(),
                airport.getLatitude(),
                airport.getLongitude());
        double distance = distanceRadians * getRadiusAt(aircraft.getLatitude(), aircraft.getLongitude());

        // Compute lookAt settings
        double altitude = aircraft.getAltitude() - airport.getAltitude();
        double range = Math.sqrt(altitude * altitude + distance * distance);
        double tilt = Math.toDegrees(Math.atan(distance / aircraft.getAltitude()));

        // Apply the new view
        lookAt.setName("Main Cam");
        lookAt.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
        lookAt.setAltitude(airport.getAltitude());
        lookAt.setHeading(heading);
        lookAt.setLatitude(airport.getLatitude());
        lookAt.setLongitude(airport.getLongitude());
        lookAt.setRange(range);
        lookAt.setTilt(tilt);
        try {
            if (firstTime) {
                map.setLookAt(lookAt, false);
            } else {
                lookAt.apply(false);
            }
        } catch (EMP_Exception empe) {
            empe.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Setting custom activity");
        setContentView(R.layout.activity_custom);

        map = (IMap) findViewById(R.id.map);
        latitude = (TextView) findViewById(R.id.Latitude);
        longitude = (TextView) findViewById(R.id.Longitude);
        camera.setAltitude(10);

        try {
            map.setCamera(camera, false);
            map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                    Log.d(TAG, "mapStateChangeEvent " + mapStateChangeEvent.getNewState());
                }
            });
            map.addMapViewChangeEventListener(new IMapViewChangeEventListener() {
                @Override
                public void onEvent(MapViewChangeEvent event) {
                    Log.i(TAG, "MapView changed, LookAt at " + event.getLookAt().getLatitude() + ", "
                            + event.getLookAt().getLongitude());
                    latitude.setText("" + event.getLookAt().getLatitude());
                    longitude.setText("" + event.getLookAt().getLongitude());
                }
            });
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }

        try {
            map.addMapInteractionEventListener(new IMapInteractionEventListener() {
                @Override
                public void onEvent(MapUserInteractionEvent mapUserInteractionEvent) {
                    Log.d(TAG, "mapUserInteractionEvent " + mapUserInteractionEvent.getPoint().x);
                }
            });
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }

        /*
        Instantiate a lookAt and set the location and angle
         */

        // Create a view of LAX airport as seen from an aircraft above Santa Monica, CA.
        aircraft = new GeoPosition();   // Aircraft above Santa Monica airport, altitude in meters
        aircraft.setLatitude(34.0158333);
        aircraft.setLongitude(-118.4513056);
        aircraft.setAltitude(2500);
        airport = new GeoPosition();    // LAX airport, Los Angeles CA, altitude MSL
        airport.setLatitude(33.9424368);
        airport.setLongitude(-118.4081222);
        airport.setAltitude(38.7);

        updateLookAt();

        // The Alt+ button raises the altitude 20% each time it is pressed

        Button higher = (Button) findViewById(R.id.Higher);
        if (higher != null) {
            higher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    double initAltitude = airport.getAltitude();
                    initAltitude *= 1.2;
                    airport.setAltitude(initAltitude);
                    updateLookAt();
                }
            });
        } else {
            Log.e(TAG, "Lower button not found");
        }
        // The Alt- button lowers the altitude 20% each time it is pressed

        Button lower = (Button) findViewById(R.id.Lower);
        if (lower != null) {
            lower.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    double initAltitude = airport.getAltitude();
                    initAltitude /= 1.2;
                    airport.setAltitude(initAltitude);
                    updateLookAt();
                }
            });
        } else {
            Log.e(TAG, "Higher button not found");
        }
        // Moves the airport left 0.5 degrees
        // each time the button is pressed
        Button latN = (Button) findViewById(R.id.LatN);
        if (latN != null) {
            latN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double dPan = airport.getLatitude();
                    if (dPan < 90.0) {
                        dPan += 0.5;
                    } else
                        Log.e(TAG, "Lat>= 90");
                    airport.setLatitude(dPan);
                    updateLookAt();
                    Toast.makeText(CustomActivity.this, "LookAt, Camera latitude " + map.getLookAt().getLatitude() + ", "
                            + map.getCamera().getLatitude(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Log.e(TAG, "Lat+ button not found");
        }
        // Moves the airport right 0.5 degrees
        // each time the button is pressed
        Button latS = (Button) findViewById(R.id.LatS);
        if (latS != null) {
            latS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double dPan = airport.getLatitude();
                    if (dPan >= -90.0) {
                        dPan -= 0.5;
                    } else
                        Log.e(TAG, "Lat <= -90");
                    airport.setLatitude(dPan);
                    updateLookAt();
                    Toast.makeText(CustomActivity.this, "LookAt, Camera latitude " + map.getLookAt().getLatitude() + ", "
                            + map.getCamera().getLatitude(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Log.e(TAG, "Lat- button not found");
        }

        // Moves the airport left 0.5 degrees
        // each time the button is pressed
        Button lonW = (Button) findViewById(R.id.LonW);
        if (lonW != null) {
            lonW.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double dPan = airport.getLongitude();
                    dPan -= 0.5;
                    if (dPan < -180.0) {
                        dPan += 360.0;
                    }
                    airport.setLongitude(dPan);
                    updateLookAt();
                    Toast.makeText(CustomActivity.this, "LookAt, Camera longitude " + map.getLookAt().getLongitude() + ", "
                            + map.getCamera().getLongitude(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Log.e(TAG, "Lon- button not found");
        }
        // Moves the airport right 0.5 degrees
        // each time the button is pressed
        Button lonE = (Button) findViewById(R.id.LonE);
        if (lonE != null) {
            lonE.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double dPan = airport.getLongitude();
                    dPan += 0.5;
                    if (dPan >= 180.0) {
                        dPan -= 360.0;
                    }
                    airport.setLongitude(dPan);
                    updateLookAt();
                    Toast.makeText(CustomActivity.this, "LookAt, Camera longitude " + map.getLookAt().getLongitude() + ", "
                            + map.getCamera().getLongitude(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Log.e(TAG, "Lon+ button not found");
        }

        // Tilt up another 0.5 degrees, within limits
        Button tiltUp = (Button) findViewById(R.id.TiltUp);
        if (tiltUp != null) {
            tiltUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        double dTilt = lookAt.getTilt();
                        if (dTilt <= 85.0) {
                            dTilt += 5;
                            lookAt.setTilt(dTilt);
                            map.setLookAt(lookAt, false);
                        } else
                            Toast.makeText(CustomActivity.this, "Can't tilt any higher", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.e(TAG, "Tilt up button not found");
        }

        // Tilt down another 0.5 degrees, within limits
        Button tiltDown = (Button) findViewById(R.id.TiltDown);
        if (tiltDown != null) {
            tiltDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        double dTilt = lookAt.getTilt();
                        if (dTilt >= -85.0) {
                            dTilt -= 5;
                            lookAt.setTilt(dTilt);
                            map.setLookAt(lookAt, false);
                        } else
                            Toast.makeText(CustomActivity.this, "Can't tilt any lower", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.e(TAG, "Tilt down button not found");
        }

    }
}
