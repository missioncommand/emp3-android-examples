package mil.emp3.examples;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoPosition;

import java.util.Locale;

import mil.emp3.api.Camera;
import mil.emp3.api.LookAt;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.examples.common.CameraUtility;
import mil.emp3.examples.common.TestBase;
import mil.emp3.examples.dialogs.CameraDialog;
import mil.emp3.examples.dialogs.LookAtDialog;

public class CameraLookAtTest extends TestBase implements Runnable, LookAtDialog.ILookAtDialogListener, CameraDialog.ICameraDialogListener {

    private static String TAG = CameraLookAtTest.class.getSimpleName();
    private IOverlay[] overlays = new IOverlay[2];
    Thread testThread;
    boolean backgroundTestInProgress = false;
    int featureCount = 0;

    public CameraLookAtTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public void run() {
        try {

            overlays[0] = o1;
            overlays[1] = o2;

            try {
                maps[0].addOverlay(overlays[0], true);
                maps[0].setFarDistanceThreshold(1620000);
                maps[0].setMidDistanceThreshold(1610000);

                if(null != maps[1]) {
                    maps[1].addOverlay(overlays[1], true);
                    maps[1].setFarDistanceThreshold(1620000);
                    maps[1].setMidDistanceThreshold(1610000);
                }
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            test0();
        } catch (Exception e) {
            Log.d(TAG, "run:" , e);
        } finally {
            testComplete();
        }
    }

    private void test0() {

        try {
            testThread = Thread.currentThread();
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(large_waitInterval * 10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } finally {
            endTest();
        }
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"setCameraTest", "setLookAtTest", "ClearMap", "Exit" };
        return actions;
    }

    @Override
    public String[] getMoreActions() {

        String[] actions = {"applyCameraTest", "applyLookAtTest", "TrackIt", "CameraIt", "AddFeature"};
        return actions;
    }

    @Override
    public void actOn(String userAction) {

        int whichMap = 0;
        try {

            FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
            if((null != fm.findFragmentByTag("fragment_lookAt_dialog")) || (null != fm.findFragmentByTag("fragment_camera_dialog"))) {
                if(!userAction.contains("AddFeature")) {
                    updateStatus("Please dismiss the dialog by selecting DONE");
                    return;
                }
            }

            if (userAction.equals("Exit")) {
                testThread.interrupt();
            } else if (userAction.contains("setCameraTest")) {
                if(!backgroundTestInProgress) {
                    new Thread(new SetCameraTest()).start();
                }
            } else if (userAction.contains("setLookAtTest")) {
                if(!backgroundTestInProgress) {
                    new Thread(new SetLookAtTest()).start();
                }
            } else if (userAction.contains("applyCameraTest")) {
                if(!backgroundTestInProgress) {
                    new Thread(new ApplyCameraTest()).start();
                }
            } else if (userAction.contains("applyLookAtTest")) {
                if(!backgroundTestInProgress) {
                    new Thread(new ApplyLookAtTest()).start();
                }
            } else if (userAction.contains("ClearMap")) {
                if(null != maps[0]) {
                    overlays[0].removeFeatures(overlays[0].getFeatures());
                }
                if(null != maps[1]) {
                    overlays[1].removeFeatures(overlays[1].getFeatures());
                }
            } else if (userAction.contains("TrackIt")) {
                updateStatus("Use AddFeature(s) and then use this test to LookAt those features");
                showLookupDialog(whichMap);
            }  else if (userAction.contains("CameraIt")) {
                updateStatus("Use AddFeature(s) and then use this test to set Camera on those features");
                showCameraDialog(whichMap);
            } else if (userAction.contains("AddFeature")) {
                updateStatus("Feature is added at lat/long of the camera, so pan the camera and then select AddFeature");
                overlays[whichMap].addFeature(generateMilStdSymbol("TRUCK" + featureCount++, maps[whichMap].getCamera()), true);
            }  else {
                updateStatus("map" + String.valueOf(whichMap) + " " + userAction + " NOT supported");
            }
        } catch (EMP_Exception e) {
            updateStatus("map" + String.valueOf(whichMap) + " " + userAction + " FAILED because " + e.getMessage());
        }
    }

    private void setupFeatures() {
        try {
            overlays[0].removeFeatures(overlays[0].getFeatures());

            updateDesignator(p1); updateDesignator(p1_1); updateDesignator(p2); updateDesignator(p3);

            maps[0].setMidDistanceThreshold(5000000.0);
            maps[0].setFarDistanceThreshold(10000000.0);

            overlays[0].addFeature(p1, true);
            overlays[0].addFeature(p2, true);
            overlays[0].addFeature(p3, true);
            overlays[0].addFeature(p1_1, true);

            Thread.sleep(waitInterval);

        } catch (EMP_Exception e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void tearDownFeatures() {
        try {
            overlays[0].removeFeatures(overlays[0].getFeatures());
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    class SetCameraTest implements Runnable {
        @Override
        public void run() {
            backgroundTestInProgress = true;
            setCameraTest();
            backgroundTestInProgress = false;
        }
    }

    private void setCameraTest() {
        try {
            startTest("setCameraTest");
            updateMilStdSymbolPosition(p1, 40.0, -70.0);
            updateMilStdSymbolPosition(p1_1, 40.0, -80.0);
            updateMilStdSymbolPosition(p2, 45.0, -70.0);
            updateMilStdSymbolPosition(p3, 45.0, -80.0);

            setupFeatures();

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
            tearDownFeatures();
            updateStatus("setCameraTest complete");
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

    class SetLookAtTest implements Runnable {
        @Override
        public void run() {
            backgroundTestInProgress = true;
            setLookAtTest();
            backgroundTestInProgress = false;
        }
    }

    private void setLookAtTest() {
        try {

            startTest("setLookAtTest");
            updateMilStdSymbolPosition(p1, 33.9424368, -118.4081222);
            updateMilStdSymbolPosition(p1_1, 40.0, -80.0);
            updateMilStdSymbolPosition(p2, 45.0, -70.0);
            updateMilStdSymbolPosition(p3, 45.0, -80.0);

            setupFeatures();

            setupLookAt(34.0158333, -118.4513056, 2500.0, 33.9424368, -118.4081222, 0.0, "TRUCK0");
            setupLookAt(39.9, -80.1, 2000.0, 40.0, -80.0, 0.0, "TRUCK1");
            setupLookAt(45.2, -69.5, 4000.0, 45.0, -70.0, 0.0, "TRUCK2");
            setupLookAt(45.3, -80.3, 5000.0, 45.0, -80.0, 0.0, "TRUCK3");

        } finally {
            tearDownFeatures();
            updateStatus("setLookAtTest complete");
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

    class ApplyCameraTest implements Runnable {
        @Override
        public void run() {
            backgroundTestInProgress = true;
            applyCameraTest();
            backgroundTestInProgress = false;
        }
    }
    private void applyCameraTest() {
        try {

            startTest("applyCameraTest");
            updateMilStdSymbolPosition(p1, 40.0, -70.0);
            updateMilStdSymbolPosition(p1_1, 40.0, -80.0);
            updateMilStdSymbolPosition(p2, 45.0, -70.0);
            updateMilStdSymbolPosition(p3, 45.0, -80.0);
            setupFeatures();

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
            tearDownFeatures();
            updateStatus("applyCameraTest complete");
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

    class ApplyLookAtTest implements Runnable {
        @Override
        public void run() {
            backgroundTestInProgress = true;
            applyLookAtTest();
            backgroundTestInProgress = false;
        }
    }
    private void applyLookAtTest() {
        try {

            startTest("applyLookAtTest");
            updateMilStdSymbolPosition(p1, 40.0, -70.0);
            updateMilStdSymbolPosition(p1_1, 40.0, -80.0);
            updateMilStdSymbolPosition(p2, 45.0, -70.0);
            updateMilStdSymbolPosition(p3, 45.0, -80.0);

            setupFeatures();

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
            tearDownFeatures();
            updateStatus("applyLookAtTest complete");
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

    private void showLookupDialog(final int whichMap) {
        Handler mainHandler = new Handler(activity.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                IGeoPosition startPosition = new GeoPosition();
                startPosition.setLatitude(maps[whichMap].getCamera().getLatitude());
                startPosition.setLongitude(maps[whichMap].getCamera().getLongitude());
                startPosition.setAltitude(maps[whichMap].getCamera().getAltitude());
                LookAtDialog lookAtDialogFragment = LookAtDialog.newInstance("Look At", CameraLookAtTest.this, startPosition, whichMap);
                lookAtDialogFragment.show(fm, "fragment_lookAt_dialog");
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void lookAt(int whichMap, double latitude, double longitude, double altitude) {
        Log.d(TAG, "Lat:Lon:Alt:" + latitude + " " + longitude + " " + altitude);

        trackIt(maps[whichMap].getCamera().getLatitude(), maps[whichMap].getCamera().getLongitude(), maps[whichMap].getCamera().getAltitude(),
                latitude, longitude, altitude);
    }

    private void trackIt(double sourceLat, double sourceLon, double sourceAlt, double targetLat, double targetLon, double targetAlt) {
        try {
            LookAt lookAt = new LookAt();


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

            m1.setLookAt(lookAt, false);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    private void showCameraDialog(final int whichMap) {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                CameraDialog cameraDialogFragment = CameraDialog.newInstance("Camera", CameraLookAtTest.this, maps[whichMap].getCamera(), whichMap);
                cameraDialogFragment.show(fm, "fragment_camera_dialog");
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void cameraSet(int whichMap, double latitude, double longitude, double altitude, double heading, double tilt, double roll) {
        maps[0].getCamera().setLatitude(latitude);
        maps[0].getCamera().setLongitude(longitude);
        maps[0].getCamera().setAltitude(altitude);
        maps[0].getCamera().setHeading(heading);
        maps[0].getCamera().setTilt(tilt);
        maps[0].getCamera().setRoll(roll);
        maps[0].getCamera().apply(true);
    }
}
