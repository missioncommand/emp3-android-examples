package mil.emp3.examples.capabilities.navItems;

import android.app.Activity;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoMilSymbol;

import mil.emp3.api.Circle;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Overlay;
import mil.emp3.api.Polygon;
import mil.emp3.api.enums.VisibilityActionEnum;
import mil.emp3.api.enums.VisibilityStateEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.examples.capabilities.common.Emp3TesterDialogBase;
import mil.emp3.examples.capabilities.common.ExecuteTest;
import mil.emp3.examples.capabilities.common.NavItemBase;

/**
 * This example shows how to create an hierarchy of overlays and features. Once added these various properties of the
 * features are updated. Users controls starting and stopping of the example via appropriate controls on the menu. When user stops
 * the example all overlays and features are removed.
 *
 * Methods that show API usage: buildOverlayHierarchy, createAndAddFeatures, and updateFeatures
 * Note that there are many flavors of convenience methods for add, update (apply), remove etc and you should explore IMap, IOverlay
 * and IFeature classes. There are many Feature Types available than those demonstrated here.
 *
 * Everything else is just the framework to get the test running.
 */
public class AddUpdateRemove extends NavItemBase {

    private static String TAG = AddUpdateRemove.class.getSimpleName();

    // User can launch upto two maps, so all the members are setup to allow for two maps.
    // It is possible to share overlays and features across maps but this example doesn't do that.

    private IOverlay overlay_a[] = new IOverlay[ExecuteTest.MAX_MAPS];
    private IOverlay overlay_b[]= new IOverlay[ExecuteTest.MAX_MAPS];
    private IOverlay overlay_a_child[]= new IOverlay[ExecuteTest.MAX_MAPS];

    private Circle circle[]= new Circle[ExecuteTest.MAX_MAPS];
    private Polygon polygon[] = new Polygon[ExecuteTest.MAX_MAPS];
    private MilStdSymbol milStdSymbol[] = new MilStdSymbol[ExecuteTest.MAX_MAPS];

    private Thread examples[] = new Thread[ExecuteTest.MAX_MAPS];

    public AddUpdateRemove(Activity activity, IMap map1, IMap map2) {
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

        // User can launch one or two maps and then can select whihc map to run the example on.
        final int whichMap = ExecuteTest.getCurrentMap();

        try {
            if(Emp3TesterDialogBase.isEmp3TesterDialogBaseActive()) {
                updateStatus("Dismiss the dialog first");
                return false;
            }

            if (userAction.equals("Exit")) {
                stopAllExamples();
                testThread.interrupt();
            } else if(userAction.equals("ClearMap")) {
                stopAllExamples();
                clearMaps();
            } else if(userAction.equals("Start")) {
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
        return (actOn(userAction));
    }

    private void stopAllExamples() {
        for(int ii = 0; ii < examples.length; ii++) {
            if(null != examples[ii]) {
                examples[ii].interrupt();
                examples[ii] = null;
            }
        }
    }

    /**
     * We can easily remove the root overlay and all overlays and features will be removed, but following code
     * is showing how features and overlays can be removed from their parent containers. Overlays and Features are
     * treated as containers. Overlays can contain other overlays and features. Features can contain other features.
     * @param whichMap
     */
    private void stopExample(int whichMap) {
        try {
            overlay_a[whichMap].removeFeature(circle[whichMap]);
            overlay_b[whichMap].removeFeature(polygon[whichMap]);
            overlay_a_child[whichMap].removeFeature(milStdSymbol[whichMap]);
            maps[whichMap].removeOverlay(overlay_a[whichMap]);
            maps[whichMap].removeOverlay(overlay_b[whichMap]);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * You can build any hierarchy as long as there are no cycles. EMP shall check for cycles and will throw an exception
     * if one is detected.
     * @param whichMap
     */
    private void buildOverlayHierarchy(int whichMap) {
        IMap map = maps[whichMap];
        boolean visible = true;
        try {
            overlay_a[whichMap] = new Overlay();
            overlay_b[whichMap] = new Overlay();
            overlay_a_child[whichMap] = new Overlay();

            // You must add the parent overlay to the map before you can add a child to that overlay
            map.addOverlay(overlay_a[whichMap], visible);
            overlay_a[whichMap].addOverlay(overlay_a_child[whichMap], visible);
            map.addOverlay(overlay_b[whichMap], visible);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up camera to a fixed position around which example features will be added.
     * @param map
     */
    private void setupCamera(IMap map) {
        boolean animate = false;
        ICamera camera = maps[ExecuteTest.getCurrentMap()].getCamera();
        camera.setLatitude(33.940);
        camera.setLongitude(-118.394);
        camera.setAltitude(3342);
        camera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
        camera.setHeading(0);
        camera.setTilt(0);
        camera.setRoll(0);
        camera.apply(animate);
    }

    /**
     * Assumes that buildOverlayHierarchy was already executed.
     */
    private void createAndAddFeatures(int whichMap) {
        boolean visible = true;

        circle[whichMap] = new Circle();
        circle[whichMap].setName("myCircle");  // This is NOT required
        circle[whichMap].getPositions().add(new MyGeoPosition(33.947, -118.402, 0));
        circle[whichMap].setRadius(200);
        try {
            overlay_a[whichMap].addFeature(circle[whichMap], visible);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }

        polygon[whichMap] = new Polygon();
        polygon[whichMap].setName("myPolygon");
        polygon[whichMap].getPositions().add(new MyGeoPosition(33.939375, -118.405725, 0));
        polygon[whichMap].getPositions().add(new MyGeoPosition(33.938669, -118.400342, 0));
        polygon[whichMap].getPositions().add(new MyGeoPosition(33.934375, -118.397326, 0));
        polygon[whichMap].getPositions().add(new MyGeoPosition(33.933214, -118.402899, 0));
        try {
            overlay_b[whichMap].addFeature(polygon[whichMap], visible);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }

        milStdSymbol[whichMap] = new MilStdSymbol();
        milStdSymbol[whichMap].setSymbolCode("S*P*S-----*****");
        milStdSymbol[whichMap].getPositions().add(new MyGeoPosition(33.940, -118.394, 0));
        milStdSymbol[whichMap].setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, "Space Track");
        milStdSymbol[whichMap].setName("Satellite");
        milStdSymbol[whichMap].setAffiliation(MilStdSymbol.Affiliation.FRIEND);
        try {
            overlay_a_child[whichMap].addFeature(milStdSymbol[whichMap], visible);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update Features
     */

    private void updateFeatures(int whichMap) {
        // Update Radius of the Circle, you can change other properties/attributes similarly like position.
        int radius = (((int)circle[whichMap].getRadius() + 100) % 400) + 100;
        circle[whichMap].setRadius(radius);
        circle[whichMap].apply();

        // Change visibility of the Polygon on its parent overlay
        // Take a look at many flavors of setVisibility method in IMap
        VisibilityStateEnum isPolygonVisible = maps[whichMap].getVisibility(polygon[whichMap], overlay_b[whichMap]);
        try {
            if (isPolygonVisible != VisibilityStateEnum.VISIBLE) {
                maps[whichMap].setVisibility(polygon[whichMap], VisibilityActionEnum.TOGGLE_ON);
                // Following method is not working, issue #10
                // maps[whichMap].setVisibility(polygon[whichMap], overlay_b[whichMap], VisibilityActionEnum.TOGGLE_ON);
            } else {
                maps[whichMap].setVisibility(polygon[whichMap], VisibilityActionEnum.TOGGLE_OFF);
                // Following method is not working, issue #10
                // maps[whichMap].setVisibility(polygon[whichMap], overlay_b[whichMap], VisibilityActionEnum.TOGGLE_OFF);
            }
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }

        // Update the affiliation of MilStdSymbol
        if(milStdSymbol[whichMap].getAffiliation().equals(MilStdSymbol.Affiliation.FRIEND)) {
            milStdSymbol[whichMap].setAffiliation(MilStdSymbol.Affiliation.HOSTILE);
        } else {
            milStdSymbol[whichMap].setAffiliation(MilStdSymbol.Affiliation.FRIEND);
        }
        milStdSymbol[whichMap].apply();

    }

    class Example implements Runnable {
        int whichMap;
        Example(int whichMap) {
            this.whichMap = whichMap;
        }

        @Override
        public void run() {
            try {
                buildOverlayHierarchy(whichMap);
                setupCamera(maps[whichMap]);
                createAndAddFeatures(whichMap);
                while (!Thread.interrupted()) {
                    try {
                        Thread.sleep(10 * 1000);
                        updateFeatures(whichMap);
                        setupCamera(maps[whichMap]); // In case user has moved it.
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } finally {
                stopExample(whichMap);
            }
        }
    }

    class MyGeoPosition extends GeoPosition {
        MyGeoPosition(double latitude, double longitude, double altitude) {
            this.setLatitude(latitude);
            this.setLongitude(longitude);
            this.setAltitude(altitude);
        }
    }
}

