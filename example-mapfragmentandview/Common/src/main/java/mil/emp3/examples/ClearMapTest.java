package mil.emp3.examples;

import android.app.Activity;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import mil.emp3.api.enums.Property;
import mil.emp3.api.interfaces.IEmpPropertyList;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.utils.EmpPropertyList;
import mil.emp3.examples.common.TestBase;

public class ClearMapTest extends TestBase implements Runnable{
    private static String TAG = MapRestoreTest.class.getSimpleName();

    public ClearMapTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public void run() {
        final IEmpPropertyList properties = new EmpPropertyList();
        properties.put(Property.ENGINE_CLASSNAME.getValue(), "mil.emp3.worldwind.MapInstance");
        properties.put(Property.ENGINE_APKNAME.getValue(), "mil.emp3.worldwind");

        try {
            startTest("test0");
            displayStatus("Map should cleared and engine should be set back to worldwind");
            if (null != m1) {
                m1.swapMapEngine(properties);
            }
            if (null != m2) {
                m2.swapMapEngine(properties);
            }
        } catch (Exception e) {
            Log.d(TAG, "run:" , e);
        } finally {
            endTest();
            testComplete();
        }
    }
}
