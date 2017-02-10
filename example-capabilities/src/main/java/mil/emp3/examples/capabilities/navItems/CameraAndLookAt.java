package mil.emp3.examples.capabilities.navItems;

import android.app.Activity;

import org.cmapi.primitives.IGeoAltitudeMode;

import mil.emp3.api.Camera;
import mil.emp3.api.Circle;
import mil.emp3.api.LookAt;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Polygon;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.examples.capabilities.common.Emp3TesterDialogBase;
import mil.emp3.examples.capabilities.common.ExecuteTest;
import mil.emp3.examples.capabilities.common.NavItemBase;
import mil.emp3.examples.capabilities.utils.CameraUtility;
import mil.emp3.examples.capabilities.utils.ExampleBuilder;

/**
 * EMP supports API to manage map Camera and lookAt. This example shows how to use all the methods
 * associated with that capability.
 * Example.run has the core of the example code.
 */
public class CameraAndLookAt extends NavItemBase {
    private static String TAG = CameraAndLookAt.class.getSimpleName();

    // User can launch up to two maps, so all the members are setup to allow for two maps.
    // It is possible to share overlays and features across maps but this example doesn't do that.

    private IOverlay overlay_a[] = new IOverlay[ExecuteTest.MAX_MAPS];
    private IOverlay overlay_b[]= new IOverlay[ExecuteTest.MAX_MAPS];
    private IOverlay overlay_a_child[]= new IOverlay[ExecuteTest.MAX_MAPS];

    private Circle circle[]= new Circle[ExecuteTest.MAX_MAPS];
    private Polygon polygon[] = new Polygon[ExecuteTest.MAX_MAPS];
    private MilStdSymbol milStdSymbol[] = new MilStdSymbol[ExecuteTest.MAX_MAPS];

    private Thread examples[] = new Thread[ExecuteTest.MAX_MAPS];

    public CameraAndLookAt(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Start", "Stop"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        return null;
    }
    protected void test0() {

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
    public boolean actOn(String userAction) {
        final int whichMap = ExecuteTest.getCurrentMap();

        try {

            if(Emp3TesterDialogBase.isEmp3TesterDialogBaseActive()) {
                updateStatus("Dismiss the dialog first");
                return false;
            }

            if (userAction.equals("Exit")) {
                ExampleBuilder.stopAllExamples(examples);
                clearMaps();
                testThread.interrupt();
            } else if(userAction.equals("ClearMap")) {
                ExampleBuilder.stopAllExamples(examples);
                clearMaps();
            }  else if(userAction.equals("Start")) {
                if(null == examples[whichMap]) {
                    examples[whichMap] = new Thread(new Example(whichMap));
                    examples[whichMap].start();
                }
            } else if(userAction.equals("Stop")) {
                if(null != examples[whichMap]) {
                    examples[whichMap].interrupt();
                    examples[whichMap] = null;
                }
            }
        } catch (Exception e) {
            updateStatus(TAG, e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    @Override
    protected void clearMapForTest() {
        String userAction = "ClearMap";
        actOn(userAction);
    }

    @Override
    protected boolean exitTest() {
        String userAction = "Exit";
        return(actOn(userAction));
    }

    /**
     * @param whichMap
     */
    private void stopExample(int whichMap) {
        clearMap(maps[whichMap]);
    }

    class Example implements Runnable {
        int whichMap;
        Example(int whichMap) {
            this.whichMap = whichMap;
        }

        @Override
        public void run() {
            try {
                boolean animate = false;
                ExampleBuilder.buildOverlayHierarchy(maps, whichMap, overlay_a, overlay_b, overlay_a_child);
                ExampleBuilder.setupCamera(maps[whichMap]);
                ExampleBuilder.createAndAddFeatures(whichMap, overlay_a, overlay_b, overlay_a_child, circle, polygon, milStdSymbol);
                while (!Thread.interrupted()) {
                    try {
                        Thread.sleep(3 * 1000);

                        // Create a camera, set it up to point at the Circle and then set the Camera on the Map
                        // For Circle position see ExampleBuilder.createAndAddFeatures
                        ICamera camera = new Camera();
                        camera.setLatitude(33.947);
                        camera.setLongitude(-118.402);
                        camera.setAltitude(3342);
                        camera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                        camera.setHeading(0);
                        camera.setTilt(0);
                        camera.setRoll(0);
                        maps[whichMap].setCamera(camera, animate);   // set Camera

                        Thread.sleep(3 * 1000);

                        // You can retrieve the current Camera and update it to point it at the Missile Support
                        ICamera currentCamera = maps[whichMap].getCamera();
                        currentCamera.setLatitude(33.940);
                        currentCamera.setLongitude( -118.394);
                        currentCamera.setAltitude(5000);
                        currentCamera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                        currentCamera.setHeading(0);
                        currentCamera.setTilt(0);
                        currentCamera.setRoll(0);
                        currentCamera.apply(animate);   // apply Camera

                        // You can use the LookAt to point Camera at an object
                        // Following code takes currentCamera and sets up to Look At the center of the circle
                        // 33.947, -118.402, 0

                        Thread.sleep(3 * 1000);
                        ILookAt lookAt = new LookAt();

                        // Calculate the LookAt values based on Source position and target position we want to LookAt
                        double heading = CameraUtility.greatCircleAzimuth(Math.toRadians(currentCamera.getLatitude()), Math.toRadians(currentCamera.getLongitude()),
                                Math.toRadians(33.947), Math.toRadians(-118.402));
                        double distanceRadians = CameraUtility.greatCircleDistance(Math.toRadians(currentCamera.getLatitude()), Math.toRadians(currentCamera.getLongitude()),
                                Math.toRadians(33.947), Math.toRadians(-118.402));
                        double distance = distanceRadians * CameraUtility.getRadiusAt(currentCamera.getLatitude(), currentCamera.getLongitude());
                        double altitude = currentCamera.getAltitude() - 0;
                        double range = Math.sqrt(altitude * altitude + distance * distance);
                        double tilt = Math.toDegrees(Math.atan(distance / currentCamera.getAltitude()));

                        // Apply the new view
                        lookAt.setName("LookAt Circle");
                        lookAt.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
                        lookAt.setAltitude(0);
                        lookAt.setHeading(heading);
                        lookAt.setLatitude(33.947);
                        lookAt.setLongitude(-118.402);
                        lookAt.setRange(range);
                        lookAt.setTilt(tilt);

                        maps[whichMap].setLookAt(lookAt, animate); // set LookAt

                        // Following takes the current LookAt and updates it to LookAt one of the vertices of the Polygon
                        // 33.933214, -118.402899, 0
                        Thread.sleep(3 * 1000);
                        ILookAt currentLookAt = maps[whichMap].getLookAt();

                        heading = CameraUtility.greatCircleAzimuth(Math.toRadians(currentCamera.getLatitude()), Math.toRadians(currentCamera.getLongitude()),
                                Math.toRadians(33.933214), Math.toRadians(-118.402899));
                        distanceRadians = CameraUtility.greatCircleDistance(Math.toRadians(currentCamera.getLatitude()), Math.toRadians(currentCamera.getLongitude()),
                                Math.toRadians(33.933214), Math.toRadians(-118.402899));
                        distance = distanceRadians * CameraUtility.getRadiusAt(currentCamera.getLatitude(), currentCamera.getLongitude());
                        altitude = currentCamera.getAltitude() - 0;
                        range = Math.sqrt(altitude * altitude + distance * distance);
                        tilt = Math.toDegrees(Math.atan(distance / currentCamera.getAltitude()));

                        currentLookAt.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
                        currentLookAt.setAltitude(0);
                        currentLookAt.setHeading(heading);
                        currentLookAt.setLatitude(33.947);
                        currentLookAt.setLongitude(-118.402);
                        currentLookAt.setRange(range);
                        currentLookAt.setTilt(tilt);
                        currentLookAt.apply(animate);  // apply LookAt

                        // You can add listeners on Camera and LookAt to monitor changes. We will add that code here in future.
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (EMP_Exception e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                stopExample(whichMap);
            }
        }
    }
}
