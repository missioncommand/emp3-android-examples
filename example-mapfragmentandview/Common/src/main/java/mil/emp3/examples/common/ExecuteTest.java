package mil.emp3.examples.common;

import android.app.Activity;
import android.util.Log;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.examples.ApplyTest;
import mil.emp3.examples.GetTest;
import mil.emp3.examples.RemoveTest;

public class ExecuteTest {

    private static UserAction userActionImpl;

    public static UserAction getUserActionImpl() {
        return userActionImpl;
    }

    public static String onTestSelected(String TAG, Activity activity, String selectedTest, IMap map, IMap map2) {

        Log.d(TAG, "Selected Test " + selectedTest);
        Runnable test = null;
        String startingTest = null;
        if(selectedTest.equals("RemoveTest")) {
            test = new RemoveTest(activity, map, map2);
             startingTest = "Starting RemoveTest";
        } else if(selectedTest.equals("GetTest")) {
            test = new GetTest(activity, map, map2);
            startingTest = "Starting GetTest";
        } else if(selectedTest.equals("ApplyTest")) {
            test = new ApplyTest(activity, map, map2);
            startingTest = "Starting ApplyTest";
        } else if(selectedTest.equals("SwapMapEngineTest")) {
            test = new mil.emp3.examples.SwapMapEngineTest(activity, map, map2);
            startingTest = "Starting SwapMapEngineTest";
        } else if(selectedTest.equals("MapRestoreTest")) {
            test = new mil.emp3.examples.MapRestoreTest(activity, map, map2);
            startingTest = "Starting MapRestoreTest";
        } else if(selectedTest.equals("ClearMapTest")) {
            test = new mil.emp3.examples.ClearMapTest(activity, map, map2);
            startingTest = "Starting ClearMapTest";
        } else if(selectedTest.equals("MultiThreadTest")) {
            test = new mil.emp3.examples.MultiThreadTest(activity, map, map2);
            startingTest = "Starting MultiThreadTest";
        } else if(selectedTest.equals("ZoomToTest")) {
            test = new mil.emp3.examples.ZoomToTest(activity, map, map2);
            startingTest = "Starting ZoomToTest";
        } else if(selectedTest.equals("PerformanceTest")) {
            test = new mil.emp3.examples.PerformanceTest(activity, map, map2);
            startingTest = "Starting PerformanceTest";
        } else if(selectedTest.equals("CameraLookAtTest")) {
            test = new mil.emp3.examples.CameraLookAtTest(activity, map, map2);
            startingTest = "Starting CameraLookAtTest";
        }  else if(selectedTest.equals("DrawPolygonTest")) {
            test = new mil.emp3.examples.DrawPolygonTest(activity, map, map2);
            startingTest = "Starting DrawPolygonTest";
        }  else if(selectedTest.equals("UserInteractionTest")) {
            test = new mil.emp3.examples.UserInteractionTest(activity, map, map2);
            startingTest = "Starting UserInteractionTest";
        } else if(selectedTest.equals("EventListenerTest")) {
            test = new mil.emp3.examples.EventListenerTest(activity, map, map2);
            startingTest = "Starting EventListenerTest";
        }  else if(selectedTest.equals("SelectFeatureTest")) {
            test = new mil.emp3.examples.SelectFeatureTest(activity, map, map2);
            startingTest = "Starting SelectFeatureTest";
        }


        if(null != test) {
            Thread testThread = new Thread(test);
            testThread.start();
        }

        userActionImpl = (UserAction) test;
        return startingTest;
    }
}
