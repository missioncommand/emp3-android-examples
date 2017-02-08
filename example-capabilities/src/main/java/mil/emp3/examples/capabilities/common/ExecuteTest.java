package mil.emp3.examples.capabilities.common;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.examples.capabilities.dialogs.utils.ErrorDialog;
import mil.emp3.examples.capabilities.navItems.AddUpdateRemove;
import mil.emp3.examples.capabilities.navItems.FreehandDrawTest;
import mil.emp3.examples.capabilities.navItems.LaunchMap;
import mil.emp3.examples.capabilities.navItems.performance_test.PerformanceTest;
import mil.emp3.examples.capabilities.navItems.basic_capability_test.BasicShapeTest;

import mil.emp3.examples.capabilities.navItems.basic_shapes_editors.BasicShapesEditorsTest;
import mil.emp3.examples.capabilities.navItems.CameraAndLookAt;
import mil.emp3.examples.capabilities.navItems.HighlightFeatures;
import mil.emp3.examples.capabilities.navItems.zoom_and_bounds.ZoomAndBoundsTest;
import mil.emp3.examples.capabilities.optItems.Bounds;
import mil.emp3.examples.capabilities.optItems.Camera;
import mil.emp3.examples.capabilities.optItems.DistanceThresholds;
import mil.emp3.examples.capabilities.optItems.EditorMode;
import mil.emp3.examples.capabilities.optItems.GeoPackageSettings;
import mil.emp3.examples.capabilities.optItems.Graphics;
import mil.emp3.examples.capabilities.optItems.Locate;
import mil.emp3.examples.capabilities.optItems.LookAt;
import mil.emp3.examples.capabilities.optItems.Map;
import mil.emp3.examples.capabilities.optItems.MapBrightness;
import mil.emp3.examples.capabilities.optItems.RemoveGeoPackageSettings;
import mil.emp3.examples.capabilities.optItems.RemoveWmsSettings;
import mil.emp3.examples.capabilities.optItems.ScreenCaptureAction;
import mil.emp3.examples.capabilities.optItems.WmsSettings;

public class ExecuteTest {

    public static final int MAP1 = 0;
    public static final int MAP2 = 1;
    public static final int MAX_MAPS = 2;

    private static UserAction userActionImpl;
    private static NavItemBase navItemBase;
    private static boolean mapLaunched = false;
    private static int currentMap = -1;
    private static boolean[] mapReady = {false, false};

    private static java.util.Map<String, Class<? extends NavItemBase>> capabilityTests;
    private static java.util.Map<String, Class<?extends OptItemBase>> optionSettings;
    static {
        capabilityTests = new HashMap<>();
        capabilityTests.put("Launch Map", LaunchMap.class);
        capabilityTests.put("Highlight Feature(s)", HighlightFeatures.class);
        capabilityTests.put("Add Update Remove", AddUpdateRemove.class);
        capabilityTests.put("Zoom and Bounds", ZoomAndBoundsTest.class);
        capabilityTests.put("Camera and Look At", CameraAndLookAt.class);
        capabilityTests.put("Basic Shapes Editors", BasicShapesEditorsTest.class);
        capabilityTests.put("Basic Shape", BasicShapeTest.class);
        capabilityTests.put("Freehand Draw", FreehandDrawTest.class);
        capabilityTests.put("Performance", PerformanceTest.class);

        optionSettings = new HashMap<>();
        optionSettings.put("Camera", Camera.class);
        optionSettings.put("Bounds", Bounds.class);
        optionSettings.put("Look At", LookAt.class);
        optionSettings.put("Map", Map.class);
        optionSettings.put("Add WMS", WmsSettings.class);
        optionSettings.put("Remove WMS", RemoveWmsSettings.class);
        optionSettings.put("Add GeoPackage", GeoPackageSettings.class);
        optionSettings.put("Remove GeoPackage", RemoveGeoPackageSettings.class);
        optionSettings.put("Editor Mode", EditorMode.class);
        optionSettings.put("Distance Thresholds", DistanceThresholds.class);
        optionSettings.put("Locate", Locate.class);
        optionSettings.put("Graphics", Graphics.class);
        optionSettings.put("Map Brightness", MapBrightness.class);
        optionSettings.put("Screen Capture", ScreenCaptureAction.class);
    }

    public static UserAction getUserActionImpl() {
        return userActionImpl;
    }
    public static int getCurrentMap() {
        return currentMap;
    }
    public static void setCurrentMap(int currentMap) {
        ExecuteTest.currentMap = currentMap;
    }
    public static void setMapReady(int whichMap, boolean ready) {
        mapReady[whichMap] = ready;
        if(-1 == currentMap) {
            currentMap = whichMap;
        }
    }

    public static boolean getMapReady(int whichMap) {
        return mapReady[whichMap];
    }

    public static String onTestSelected(String TAG, Activity activity, String selectedTest, IMap map, IMap map2) {

        Log.d(TAG, "Selected Test " + selectedTest);
        Runnable test = null;
        String startingTest = null;

        Class<? extends NavItemBase> testClass = capabilityTests.get(selectedTest);
        if(null != testClass) {
            try {
                Constructor<? extends NavItemBase> c = testClass.getConstructor(Activity.class, IMap.class, IMap.class);
                test = c.newInstance(activity, map, map2);
                startingTest = "Starting " + selectedTest;
                if(selectedTest.equals("Launch Map")) {
                    mapLaunched = true;
                }
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                Log.e(TAG, "selectedTest " + selectedTest, e);
                test = null;
                startingTest = null;
            }
        }

        if(null != test) {
            Thread testThread = new Thread(test);
            testThread.start();
        }

        userActionImpl = (UserAction) test;
        navItemBase = (NavItemBase) test;
        return startingTest;
    }

    public static String onOptionItemSelected(String TAG, Activity activity, String selectedOptItem, IMap map, IMap map2) {

        Log.d(TAG, "Selected Option Item " + selectedOptItem);
        Runnable test = null;
        String startingOptionItem = null;
        boolean foundReadyMap = false;

        for(int ii = 0; ii < MAX_MAPS; ii++) {
            if(getMapReady(ii)) {
                foundReadyMap = true;
                break;
            }
        }
        if(!foundReadyMap) { // Need to find a better way to detect this.
            ErrorDialog.showError(activity, "You must launch a Map first");
            return "you must launch the map";
        }

        if(selectedOptItem.equals("Exit")) {
            if(navItemBase != null) {
                if(navItemBase.exitTest()) {
                    navItemBase = null;
                }
                return "existing test";
            } else {
                return "no test running";
            }
        } else  if(selectedOptItem.equals("Clear Map")) {
            if(navItemBase != null) {
                navItemBase.clearMapForTest();
                return "clearing map for test";
            } else {
                return "no test running";
            }
        } else {
            Class<? extends OptItemBase> optionClass = optionSettings.get(selectedOptItem);
            if(null != optionClass) {
                try {
                    Constructor<? extends OptItemBase> c = optionClass.getConstructor(Activity.class, IMap.class, IMap.class);
                    test = c.newInstance(activity, map, map2);
                    startingOptionItem = "Starting optItem." + selectedOptItem;
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    Log.e(TAG, "selectedOptItem " + selectedOptItem, e);
                    test = null;
                    startingOptionItem = null;
                }
            }
        }

        if(null != test) {
            Thread testThread = new Thread(test);
            testThread.start();
        }

        return startingOptionItem;
    }
}
