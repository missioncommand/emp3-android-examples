package mil.emp3.examples.capabilities.navItems;

import android.app.Activity;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.IGeoBounds;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.Circle;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Polygon;

import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.examples.capabilities.common.Emp3TesterDialogBase;
import mil.emp3.examples.capabilities.common.ExecuteTest;
import mil.emp3.examples.capabilities.common.NavItemBase;
import mil.emp3.examples.capabilities.containers.dialogs.milstdunits.SymbolPropertiesDialog;

import mil.emp3.examples.capabilities.utils.ExampleBuilder;

/**
 * EMP supports API to zoom to features and set bounds. This example shows how to use all the methods
 * associated with that capability.
 * Example.run has the core of the example code.
 */
public class ZoomAndBounds extends NavItemBase {
    private static String TAG = ZoomAndBounds.class.getSimpleName();

    // User can launch up to two maps, so all the members are setup to allow for two maps.
    // It is possible to share overlays and features across maps but this example doesn't do that.

    private IOverlay overlay_a[] = new IOverlay[ExecuteTest.MAX_MAPS];
    private IOverlay overlay_b[]= new IOverlay[ExecuteTest.MAX_MAPS];
    private IOverlay overlay_a_child[]= new IOverlay[ExecuteTest.MAX_MAPS];

    private Circle circle[]= new Circle[ExecuteTest.MAX_MAPS];
    private Polygon polygon[] = new Polygon[ExecuteTest.MAX_MAPS];
    private MilStdSymbol milStdSymbol[] = new MilStdSymbol[ExecuteTest.MAX_MAPS];

    private Thread examples[] = new Thread[ExecuteTest.MAX_MAPS];

    public ZoomAndBounds(Activity activity, IMap map1, IMap map2) {
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
            SymbolPropertiesDialog.loadSymbolTables();
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
                testThread.interrupt();
            } else if(userAction.equals("ClearMap")) {
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
                        maps[whichMap].zoomTo(polygon[whichMap], animate); // Zoom to a single feature

                        Thread.sleep(3 * 1000);
                        List<IFeature> list = new ArrayList<>();
                        list.add(polygon[whichMap]); list.add(circle[whichMap]);
                        maps[whichMap].zoomTo(list, animate); // Zoom to a list of features

                        Thread.sleep(3 * 1000);
                        maps[whichMap].zoomTo(overlay_a[whichMap], animate); // Zoom to overlay

                        Thread.sleep(3 * 1000);
                        // 33.933214, -118.402899, 0 This is one of the verecise of the Polygon
                        // 33.940, -118.394, 0 This is where the milStdSymbol is
                        IGeoBounds bounds = new GeoBounds();
                        bounds.setSouth(33.933214);
                        bounds.setNorth(33.940);
                        bounds.setWest(-118.402899);
                        bounds.setEast(-118.394);
                        maps[whichMap].setBounds(bounds, animate); // Set bounds

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } finally {
                stopExample(whichMap);
            }
        }
    }
}
