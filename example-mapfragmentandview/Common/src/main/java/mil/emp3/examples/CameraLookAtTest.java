package mil.emp3.examples;

import android.app.Activity;
import android.util.Log;

import org.cmapi.primitives.IGeoAltitudeMode;

import java.util.Locale;

import mil.emp3.api.Camera;
import mil.emp3.api.LookAt;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.examples.common.CameraUtility;
import mil.emp3.examples.common.TestBase;

public class CameraLookAtTest extends TestBase implements Runnable {

    public CameraLookAtTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public void run() {
        try {
            setCameraTest();
            setLookAtTest();
            applyCameraTest();
            applyLookAtTest();
        } catch (Exception e) {
            Log.d(TAG, "run:" , e);
        } finally {
            testComplete();
        }
    }

    private void setCameraTest() {
        try {
            startTest("setCameraTest");
            try {
                updateMilStdSymbolPosition(p1, 40.0, -70.0);
                updateMilStdSymbolPosition(p1_1, 40.0, -80.0);
                updateMilStdSymbolPosition(p2, 45.0, -70.0);
                updateMilStdSymbolPosition(p3, 45.0, -80.0);
                updateDesignator(p1); updateDesignator(p1_1); updateDesignator(p2); updateDesignator(p3);

                m1.setMidDistanceThreshold(5000000.0);
                m1.setFarDistanceThreshold(10000000.0);

                m1.addOverlay(o1, true);

                o1.addFeature(p1, true);
                o1.addFeature(p2, true);
                o1.addFeature(p3, true);
                o1.addFeature(p1_1, true);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            ICamera camera = m1.getCamera();
            String status  = String.format(Locale.US, "Starting Pos " +
                            "Camera " + " (L:N:A %1$6.3f %2$6.3f %3$6.0f H:T:R %4$6.3f %5$6.3f %6$6.3f) ",
                    camera.getLatitude(), camera.getLongitude(), camera.getAltitude(),
                    camera.getHeading(), camera.getTilt(), camera.getRoll());
            updateStatus(status);
            Thread.sleep(waitInterval);

            ICamera myCamera = new Camera();
            myCamera.copySettingsFrom(camera);

            setupCameraTest(myCamera, 40.0, -70.0, 1000.0,0.0, 0.0, 0.0, "TRUCK0");
            setupCameraTest(myCamera, 40.0, -80.0, 1000.0,0.0, 0.0, 0.0, "TRUCK1");
            setupCameraTest(myCamera, 45.0, -70.0, 1000.0,0.0, 0.0, 0.0, "TRUCK2");
            setupCameraTest(myCamera, 45.0, -80.0, 1000.0,0.0, 0.0, 0.0, "TRUCK3");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    private void setupCameraTest(ICamera myCamera, double lat, double lon, double alt, double tilt, double roll, double heading, String featureSeen) {
        try {
            myCamera.setLatitude(lat);
            myCamera.setLongitude(lon);
            myCamera.setAltitude(alt);
            myCamera.setTilt(tilt);
            myCamera.setRoll(roll);
            myCamera.setHeading(heading);

            updateStatus("setCamera for " + featureSeen);
            m1.setCamera(myCamera, false);
            Thread.sleep(small_waitInterval);

            ICamera camera = m1.getCamera();
            String status  = String.format(Locale.US, "Camera.set Target " + featureSeen +
                    " Camera " + " (L:N:A %1$6.3f %2$6.3f %3$6.0f H:T:R %4$6.3f %5$6.3f %6$6.3f) ",
                    camera.getLatitude(), camera.getLongitude(), camera.getAltitude(),
                    camera.getHeading(), camera.getTilt(), camera.getRoll());

            updateStatus(status);
            Thread.sleep(waitInterval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    private void setLookAtTest() {
        try {

            startTest("setLookAtTest");

            try {

                updateMilStdSymbolPosition(p1, 33.9424368, -118.4081222);
                updateMilStdSymbolPosition(p1_1, 40.0, -80.0);
                updateMilStdSymbolPosition(p2, 45.0, -70.0);
                updateMilStdSymbolPosition(p3, 45.0, -80.0);
                updateDesignator(p1); updateDesignator(p1_1); updateDesignator(p2); updateDesignator(p3);

                m1.setMidDistanceThreshold(5000000.0);
                m1.setFarDistanceThreshold(10000000.0);

                m1.addOverlay(o1, true);

                o1.addFeature(p1, true);
                o1.addFeature(p2, true);
                o1.addFeature(p3, true);
                o1.addFeature(p1_1, true);

            } catch (EMP_Exception e) {
                e.printStackTrace();
            }

            Thread.sleep(small_waitInterval);

            setupLookAt(34.0158333, -118.4513056, 2500.0, 33.9424368, -118.4081222, 0.0, "TRUCK0");
            setupLookAt(39.9, -80.1, 2000.0, 40.0, -80.0, 0.0, "TRUCK1");
            setupLookAt(45.2, -69.5, 4000.0, 45.0, -70.0, 0.0, "TRUCK2");
            setupLookAt(45.3, -80.3, 5000.0, 45.0, -80.0, 0.0, "TRUCK3");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            endTest();
        }
    }

    private void setupLookAt(double sourceLat, double sourceLon, double sourceAlt, double targetLat, double targetLon, double targetAlt,
                             String featureSeen) {
        try {
            LookAt lookAt = new LookAt();

            try {
                updateStatus("LokAt.set set camera to point of interest");
                m1.setCamera(CameraUtility.buildCamera(sourceLat, sourceLon, sourceAlt), false);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(small_waitInterval);

            ICamera camera1 = m1.getCamera();
            String camera  = String.format(Locale.US, "LookAt.set Camera L:N:A %1$6.3f %2$6.3f %3$6.0f T:R:H %4$6.3f %5$6.3f %6$6.3f no feature seen",
                    camera1.getLatitude(), camera1.getLongitude(), camera1.getAltitude(),
                    camera1.getTilt(), camera1.getRoll(), camera1.getHeading());
            updateStatus(camera);
            Thread.sleep(waitInterval);

            try {
                // Compute heading and distance from aircraft to airport
                double heading = CameraUtility.greatCircleAzimuth(Math.toRadians(sourceLat), Math.toRadians(sourceLon),
                        Math.toRadians(targetLat), Math.toRadians(targetLon));
                double distanceRadians = CameraUtility.greatCircleDistance(Math.toRadians(sourceLat), Math.toRadians(sourceLon),
                        Math.toRadians(targetLat), Math.toRadians(targetLon));
                double distance = distanceRadians * CameraUtility.getRadiusAt(sourceLat, sourceLon);
                System.err.println(" CHECK heading " + heading + " distanceRadians " + distanceRadians + " distance" + distance);
                // Compute camera settings
                double altitude = sourceAlt - targetAlt;
                double range = Math.sqrt(altitude * altitude + distance * distance);
                double tilt = Math.toDegrees(Math.atan(distance / sourceAlt));
                System.err.println("CHECK altitude " + altitude + " range " + range + " tilt " + tilt);

                // Apply the new view
                lookAt.setName("Main Cam");
                lookAt.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
                lookAt.setAltitude(targetAlt);
                lookAt.setHeading(heading);
                lookAt.setLatitude(targetLat);
                lookAt.setLongitude(targetLon);
                lookAt.setRange(range);
                lookAt.setTilt(tilt);

                updateStatus("lookAt.set to " + featureSeen);
                m1.setLookAt(lookAt, false);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(small_waitInterval);

            ICamera camera2 = m1.getCamera();
            String status  = String.format(Locale.US, "LookAt.set " + featureSeen + " (L:N:A %1$6.3f %2$6.3f %3$6.0f) " +
                            "Camera " + " (L:N:A %4$6.3f %5$6.3f %6$6.0f H:T:R %7$6.3f %8$6.3f %9$6.3f) ",
                    lookAt.getLatitude(), lookAt.getLongitude(), lookAt.getAltitude(),
                    camera2.getLatitude(), camera2.getLongitude(), camera2.getAltitude(),
                    camera2.getHeading(), camera2.getTilt(), camera2.getRoll());

            updateStatus(status);
            Thread.sleep(waitInterval);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void applyCameraTest() {
        try {

            startTest("applyCameraTest");

            try {

                updateMilStdSymbolPosition(p1, 40.0, -70.0);
                updateMilStdSymbolPosition(p1_1, 40.0, -80.0);
                updateMilStdSymbolPosition(p2, 45.0, -70.0);
                updateMilStdSymbolPosition(p3, 45.0, -80.0);

                updateDesignator(p1); updateDesignator(p1_1); updateDesignator(p2); updateDesignator(p3);
                m1.addOverlay(o1, true);

                o1.addFeature(p1, true);
                o1.addFeature(p2, true);
                o1.addFeature(p3, true);
                o1.addFeature(p1_1, true);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(small_waitInterval);

            ICamera camera = m1.getCamera();
            String status  = String.format(Locale.US, "Camera.apply start pos Camera (L:N:A %1$6.3f %2$6.3f %3$6.0f  H:T:R %4$6.3f %5$6.3f %6$6.3f) no feature seen",
                    camera.getLatitude(), camera.getLongitude(), camera.getAltitude(),
                    camera.getHeading(), camera.getTilt(), camera.getRoll());
            updateStatus(status);
            Thread.sleep(waitInterval);

            setupApplyCamera(camera, 40.0, -70.0, 2000.0, 0.0, 0.0, 0.0, "TRUCK0");
            setupApplyCamera(camera, 40.0, -80.0, 2000.0, 0.0, 0.0, 0.0, "TRUCK1");
            setupApplyCamera(camera, 45.0, -70.0, 2000.0, 0.0, 0.0, 0.0, "TRUCK2");
            setupApplyCamera(camera, 45.0, -80.0, 2000.0, 0.0, 0.0, 0.0, "TRUCK3");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    private void setupApplyCamera(ICamera camera, double lat, double lon, double alt, double tilt, double roll, double heading, String featureSeen) {
        try {

            camera.setLatitude(lat);
            camera.setLongitude(lon);
            camera.setAltitude(alt);
            camera.setTilt(tilt);
            camera.setRoll(roll);
            camera.setHeading(heading);

            updateStatus("Camera.apply for " + featureSeen);
            camera.apply(false);
            Thread.sleep(small_waitInterval);

            String status  = String.format(Locale.US, "Camera.apply Target " + featureSeen +
                            " Camera " + " (L:N:A %1$6.3f %2$6.3f %3$6.0f H:T:R %4$6.3f %5$6.3f %6$6.3f) ",
                    camera.getLatitude(), camera.getLongitude(), camera.getAltitude(),
                    camera.getHeading(), camera.getTilt(), camera.getRoll());
            updateStatus(status);
            Thread.sleep(waitInterval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void applyLookAtTest() {
        try {

            startTest("applyLookAtTest");

            try {

                updateMilStdSymbolPosition(p1, 40.0, -70.0);
                updateMilStdSymbolPosition(p1_1, 40.0, -80.0);
                updateMilStdSymbolPosition(p2, 45.0, -70.0);
                updateMilStdSymbolPosition(p3, 45.0, -80.0);
                updateDesignator(p1); updateDesignator(p1_1); updateDesignator(p2); updateDesignator(p3);

                m1.addOverlay(o1, true);

                o1.addFeature(p1, true);
                o1.addFeature(p2, true);
                o1.addFeature(p3, true);
                o1.addFeature(p1_1, true);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            m1.setMidDistanceThreshold(5000000.0);
            m1.setFarDistanceThreshold(10000000.0);

            ILookAt lookAt = null;
//            lookAt = setupApplyLookAt(lookAt, 39.9, -70.1, 2000.0, 40.0, -70.0, 0.0, "TRUCK0");
//            lookAt = setupApplyLookAt(lookAt, 39.9, -80.1, 2000.0, 40.0, -80.0, 0.0, "TRUCK1");
//            lookAt = setupApplyLookAt(lookAt, 45.2, -69.5, 2000, 45.0, -70.0, 0.0, "TRUCK2");
//            setupApplyLookAt(lookAt, 45.3, -80.3, 2000, 45.0, -80.0, 0.0, "TRUCK3");

            lookAt = setupApplyLookAt(lookAt, 42.5, -75.0, 2000.0, 40.0, -70.0, 0.0, "TRUCK0");
            lookAt = setupApplyLookAt(lookAt, 42.5, -75.0, 2000.0, 40.0, -80.0, 0.0, "TRUCK1");
            lookAt = setupApplyLookAt(lookAt, 42.5, -75.0, 2000, 45.0, -70.0, 0.0, "TRUCK2");
            setupApplyLookAt(lookAt, 42.5, -75.0, 2000, 45.0, -80.0, 0.0, "TRUCK3");

        } finally {
            endTest();
        }
    }

    private ILookAt setupApplyLookAt(ILookAt lookAt, double sourceLat, double sourceLon, double sourceAlt, double targetLat, double targetLon, double targetAlt,
                             String featureSeen) {
        try {
            // Compute heading and distance from aircraft to airport
            double heading = CameraUtility.greatCircleAzimuth(Math.toRadians(sourceLat), Math.toRadians(sourceLon),
                    Math.toRadians(targetLat), Math.toRadians(targetLon));
            double distanceRadians = CameraUtility.greatCircleDistance(Math.toRadians(sourceLat), Math.toRadians(sourceLon),
                    Math.toRadians(targetLat), Math.toRadians(targetLon));
            double distance = distanceRadians * CameraUtility.getRadiusAt(sourceLat, sourceLon);
            System.err.println(" CHECK heading " + heading + " distanceRadians " + distanceRadians + " distance" + distance);
            // Compute camera settings
            double altitude = sourceAlt - targetAlt;
            double range = Math.sqrt(altitude * altitude + distance * distance);
            double tilt = Math.toDegrees(Math.atan(distance / sourceAlt));
            System.err.println("CHECK altitude " + altitude + " range " + range + " tilt " + tilt);

            // Apply the new view

            if(null == lookAt) {
                lookAt = new LookAt();
                lookAt.setName("Main LookAt");
                lookAt.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
                lookAt.setAltitude(targetAlt);
                lookAt.setHeading(heading);
                lookAt.setLatitude(targetLat);
                lookAt.setLongitude(targetLon);
                lookAt.setRange(range);
                lookAt.setTilt(tilt);

                try {
                    updateStatus("set LookAt for " + featureSeen);
                    m1.setLookAt(lookAt, false);
                } catch (EMP_Exception e) {
                    e.printStackTrace();
                }
            } else {
                lookAt.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
                lookAt.setAltitude(targetAlt);
                lookAt.setHeading(heading);
                lookAt.setLatitude(targetLat);
                lookAt.setLongitude(targetLon);
                lookAt.setRange(range);
                lookAt.setTilt(tilt);

                updateStatus("apply LookAt for " + featureSeen);
                lookAt.apply(false);
            }
            Thread.sleep(small_waitInterval);
            ILookAt lookAt2 = m1.getLookAt();
            ICamera camera2 = m1.getCamera();

            String lookat  = String.format(Locale.US, "LookAt.apply " + featureSeen + " (L:N:A %1$6.3f %2$6.3f %3$6.0f) " +
                    "Camera " + " (L:N:A %4$6.3f %5$6.3f %6$6.0f H:T:R %7$6.3f %8$6.3f %9$6.3f) ",
                    lookAt2.getLatitude(), lookAt2.getLongitude(), lookAt2.getAltitude(),
                    camera2.getLatitude(), camera2.getLongitude(), camera2.getAltitude(),
                    camera2.getHeading(), camera2.getTilt(), camera2.getRoll());

            updateStatus(lookat);
            Thread.sleep(waitInterval);
            return lookAt;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
