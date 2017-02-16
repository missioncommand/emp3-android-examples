package mil.emp3.examples.capabilities.common;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.examples.capabilities.dialogs.utils.ErrorDialog;
import mil.emp3.examples.capabilities.navItems.AddUpdateRemove;
import mil.emp3.examples.capabilities.navItems.DrawFeature;
import mil.emp3.examples.capabilities.navItems.FreehandDraw;
import mil.emp3.examples.capabilities.navItems.LaunchMap;
import mil.emp3.examples.capabilities.navItems.Styles;

import mil.emp3.examples.capabilities.navItems.EditFeature;
import mil.emp3.examples.capabilities.navItems.CameraAndLookAt;
import mil.emp3.examples.capabilities.navItems.HighlightFeatures;
import mil.emp3.examples.capabilities.navItems.ZoomAndBounds;
import mil.emp3.examples.capabilities.optItems.Camera;
import mil.emp3.examples.capabilities.optItems.Map;

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
        capabilityTests.put("Zoom and Bounds", ZoomAndBounds.class);
        capabilityTests.put("Camera and Look At", CameraAndLookAt.class);
        capabilityTests.put("Edit Feature", EditFeature.class);
        capabilityTests.put("Freehand Draw", FreehandDraw.class);
        capabilityTests.put("Draw Feature", DrawFeature.class);
        capabilityTests.put("Styles", Styles.class);

        optionSettings = new HashMap<>();
        optionSettings.put("Camera", Camera.class);
        optionSettings.put("Map", Map.class);
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
