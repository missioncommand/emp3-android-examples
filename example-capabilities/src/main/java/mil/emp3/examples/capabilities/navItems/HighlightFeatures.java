package mil.emp3.examples.capabilities.navItems;

import android.app.Activity;
import android.util.Log;

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
 * EMP supports API to highlight feature that are currently displayed on the map. This example shows how to use all the methods
 * associated with that capability.
 * Example.run has the core of the example code.
 */
public class HighlightFeatures extends NavItemBase{
    private static String TAG = HighlightFeatures.class.getSimpleName();

    // User can launch up to two maps, so all the members are setup to allow for two maps.
    // It is possible to share overlays and features across maps but this example doesn't do that.

    private IOverlay overlay_a[] = new IOverlay[ExecuteTest.MAX_MAPS];
    private IOverlay overlay_b[]= new IOverlay[ExecuteTest.MAX_MAPS];
    private IOverlay overlay_a_child[]= new IOverlay[ExecuteTest.MAX_MAPS];

    private Circle circle[]= new Circle[ExecuteTest.MAX_MAPS];
    private Polygon polygon[] = new Polygon[ExecuteTest.MAX_MAPS];
    private MilStdSymbol milStdSymbol[] = new MilStdSymbol[ExecuteTest.MAX_MAPS];

    private Thread examples[] = new Thread[ExecuteTest.MAX_MAPS];

    public HighlightFeatures(Activity activity, IMap map1, IMap map2) {
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

        // User can launch one or two maps and then can select whihc map to run the example on.
        final int whichMap = ExecuteTest.getCurrentMap();

        try {
            if(Emp3TesterDialogBase.isEmp3TesterDialogBaseActive()) {
                updateStatus("Dismiss the dialog first");
                return false;
            }

            if (userAction.equals("Exit")) {
                ExampleBuilder.stopAllExamples(examples);
                testThread.interrupt();
            } else if(userAction.equals("ClearMap")) {
                ExampleBuilder.stopAllExamples(examples);
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
                ExampleBuilder.buildOverlayHierarchy(maps, whichMap, overlay_a, overlay_b, overlay_a_child);
                ExampleBuilder.setupCamera(maps[whichMap]);
                ExampleBuilder.createAndAddFeatures(whichMap, overlay_a, overlay_b, overlay_a_child, circle, polygon, milStdSymbol);
                while (!Thread.interrupted()) {
                    try {
                        Thread.sleep(3 * 1000);
                        ExampleBuilder.setupCamera(maps[whichMap]); // In case user has moved it.
                        maps[whichMap].selectFeature(polygon[whichMap]); // Select a single feature

                        Thread.sleep(3 * 1000);
                        ExampleBuilder.setupCamera(maps[whichMap]); // In case user has moved it.
                        maps[whichMap].deselectFeature(polygon[whichMap]); // Deselect a single feature
                        List<IFeature> featureList = new ArrayList<>();
                        featureList.add(circle[whichMap]);
                        featureList.add(milStdSymbol[whichMap]);
                        maps[whichMap].selectFeatures(featureList); // Select a list of features

                        Thread.sleep(3 * 1000);
                        ExampleBuilder.setupCamera(maps[whichMap]); // In case user has moved it.
                        maps[whichMap].deselectFeatures(featureList); // Deselect a list of features
                        maps[whichMap].selectFeature(polygon[whichMap]);

                        Thread.sleep(3 * 1000);
                        ExampleBuilder.setupCamera(maps[whichMap]); // In case user has moved it.
                        featureList = maps[whichMap].getSelected();
                        if((null != featureList) && (0 != featureList.size())) {
                            Log.d(TAG, "selected feature " + featureList.get(0).getClass().getCanonicalName());
                        }
                        maps[whichMap].clearSelected(); // Clear all selected features

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
