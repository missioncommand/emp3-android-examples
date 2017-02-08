package mil.emp3.examples.capabilities.utils;

import org.cmapi.primitives.GeoPosition;

/**
 * This is a convenience wrapper for GeoPosition
 */
public class MyGeoPosition extends GeoPosition {
    public MyGeoPosition(double latitude, double longitude, double altitude) {
        this.setLatitude(latitude);
        this.setLongitude(longitude);
        this.setAltitude(altitude);
    }
}