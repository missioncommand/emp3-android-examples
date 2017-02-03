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

public class SwapMapEngineTest extends TestBase implements Runnable {
    private static String TAG = SwapMapEngineTest.class.getSimpleName();

    public SwapMapEngineTest(Activity activity, IMap map1, IMap map2) {
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

            try {
                final IEmpPropertyList properties = new EmpPropertyList();
                properties.put(Property.ENGINE_CLASSNAME.getValue(), "mil.emp3.openstreet.MapInstance");
                properties.put(Property.ENGINE_APKNAME.getValue(), "mil.emp3.openstreetapk");

                // m2.swapMapEngine("mil.emp3.arcgis.MapInstance", "mil.emp3.arcgis");
                m2.swapMapEngine(properties);
                // m2.swapMapEngine("mil.emp3.worldwind.MapInstance", "mil.emp3.worldwind");

                updateTestStatus(2, 2);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }

            Thread.sleep(large_waitInterval);

            try {
                final IEmpPropertyList properties = new EmpPropertyList();
                properties.put(Property.ENGINE_CLASSNAME.getValue(), "mil.emp3.worldwind.MapInstance");
                properties.put(Property.ENGINE_APKNAME.getValue(), "mil.emp3.worldwind");

                m2.swapMapEngine(properties);
                updateTestStatus(2, 2);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }

    }
}
