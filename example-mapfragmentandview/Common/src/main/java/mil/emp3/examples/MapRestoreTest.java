package mil.emp3.examples;

import android.app.Activity;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import mil.emp3.api.enums.Property;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEmpPropertyList;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.utils.EmpPropertyList;
import mil.emp3.examples.common.TestBase;

public class MapRestoreTest extends TestBase implements Runnable {
    private static String TAG = MapRestoreTest.class.getSimpleName();

    public MapRestoreTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public void run() {
        if((null == m1) || (null == m2)) {
            testComplete("THIS IS A MULTIMAP TEST, CANNOT BE RUN IN THIS APPLICATION");
            return;
        }
        try {
            test0();
        } catch (Exception e) {
            Log.d(TAG, "run:" , e);
        } finally {
            testComplete();
        }
    }

    public void test0() {
        try {
            startTest("test0");
            try {
                m1.addOverlay(o1, true);
                m2.addOverlay(o2, true);
                o1.addFeature(p1, true);
                o2.addFeature(p1, true);
                p1.addFeature(p1_1, true);
                o2.addFeature(p1_1, true);
                updateTestStatus(2, 2);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            final IEmpPropertyList properties = new EmpPropertyList();
            properties.put(Property.ENGINE_CLASSNAME.getValue(), "mil.emp3.openstreet.MapInstance");
            properties.put(Property.ENGINE_APKNAME.getValue(), "mil.emp3.openstreetapk");

            try {
                m2.swapMapEngine(properties);
                updateTestStatus(2, 0);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);
            displayStatus("Now rotate the devices and make sure map restores");
            Thread.sleep(large_waitInterval);

            Thread.sleep(large_waitInterval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Log.d(TAG, "Ending MapRestoreTest");
            // endTest(); Activity was destroyed, so let the user clear the map.
        }

    }
}
