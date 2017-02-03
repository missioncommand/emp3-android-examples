package mil.emp3.examples.common;

import org.cmapi.primitives.IGeoAltitudeMode;

import mil.emp3.api.interfaces.ICamera;

/**
 * Created by deepakkarmarkar on 6/10/2016.
 */
public class CameraUtility {
    private static final double INVERSE_FLATTENING = 298.257223563;
    private static final double OFFICIAL_SEMI_MAJOR_AXIS = 6378137.0;

    public static ICamera buildCamera(double latitude, double longitude, double altitude) {
        mil.emp3.api.Camera oCamera = new mil.emp3.api.Camera();
        oCamera.setName("Main Cam");
        oCamera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
        oCamera.setAltitude(altitude);
        oCamera.setHeading(0.0);
        oCamera.setLatitude(latitude);
        oCamera.setLongitude(longitude);
        oCamera.setRoll(0.0);
        oCamera.setTilt(0.0);
        return oCamera;
    }

    public static double getRadiusAt(double latitude, double longitude) {
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
    public static double greatCircleAzimuth(double lat1Radians, double lon1Radians, double lat2Radians, double lon2Radians) {

        if (lat1Radians == lat2Radians && lon1Radians == lon2Radians) {
            return 0;
        }

        if (lon1Radians == lon2Radians) {
            return lat1Radians > lat2Radians ? 180 : 0;
        }

        // Taken from "Map Projections - A Working Manual", page 30, equation 5-4b.
        // The atan2() function is used in place of the traditional atan(y/x) to simplify the case when x == 0.
        double y = Math.cos(lat2Radians) * Math.sin(lon2Radians - lon1Radians);
        double x = Math.cos(lat1Radians) * Math.sin(lat2Radians) - Math.sin(lat1Radians) * Math.cos(lat2Radians) * Math.cos(lon2Radians - lon1Radians);
        double azimuthRadians = Math.atan2(y, x);

        return Double.isNaN(azimuthRadians) ? 0 : Math.toDegrees(azimuthRadians);
    }

    /**
     * Computes the angular distance of the great circle path between this location and a specified location. In
     * radians, this angle is the arc length of the segment between the two locations. To compute a distance in meters
     * from this value, multiply the return value by the radius of the globe. This function uses a spherical model, not
     * elliptical.
     */
    public static double greatCircleDistance(double lat1Radians, double lon1Radians, double lat2Radians, double lon2Radians) {
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
}
