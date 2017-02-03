package mil.emp3.examples;

import android.app.Activity;
import android.util.Log;


import java.util.List;

import mil.emp3.api.enums.IconSizeEnum;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;

import mil.emp3.examples.common.TestBase;

public class ApplyTest extends TestBase implements Runnable {

    private static String TAG = ApplyTest.class.getSimpleName();


    public ApplyTest(Activity activity, IMap map1, IMap map2) {
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
        } finally {
           testComplete();
        }
    }

    /*
      m1 -> o1, o1 -> o2, o3
      m2 -> o2
      o2 -> o3
      o3 -> p1
      o2 -> p2
      o1 -> p3
      p1 -> p1_1

    */
    private void test0() {
        try {
            startTest("test0");
            Thread.sleep(waitInterval);

            try {
                m1.addOverlay(o1, true);
                o1.addFeature(p1,true);
                updateTestStatus(1, 0);
                Log.d(TAG, "Feature p1 added to o1 " + p1.getPositions().get(0).getLatitude() + "-" + p1.getPositions().get(0).getLongitude());
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }

            Thread.sleep(waitInterval);

            try {
                updateMilStdSymbolPosition(p1, latitude + .02, longitude + .02);
                p1.apply();
                m1.setIconSize(IconSizeEnum.SMALL);
                Log.d(TAG, "Update p1 " + p1.getPositions().get(0).getLatitude() + "-" + p1.getPositions().get(0).getLongitude());
                displayStatus("Move feature and change icon size to SMALL");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                updateMilStdSymbolPosition(p1, latitude + (.01), longitude + (.01));
                p1.apply();
                m1.setIconSize(IconSizeEnum.MEDIUM);
                Log.d(TAG, "Update p1 "  + p1.getPositions().get(0).getLatitude() + "-" + p1.getPositions().get(0).getLongitude());
                displayStatus("Move feature and change icon size MEDIUM");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                updateMilStdSymbolPosition(p1, latitude + (.03), longitude + (.03));
                p1.apply();
                m1.setIconSize(IconSizeEnum.LARGE);
                Log.d(TAG, "Update p1 "  + p1.getPositions().get(0).getLatitude() + "-" + p1.getPositions().get(0).getLongitude());
                displayStatus("Move feature and change icon size LARGE");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);
            final List<IFeature> list = generateMilStdSymbolList(10, latitude, longitude);

            try {
                o1.addFeatures(list, true);
                Log.d(TAG, "add list of features "  + list.size());
                updateTestStatus(11, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                m1.setIconSize(IconSizeEnum.MEDIUM);
                Log.d(TAG, "Update p1 "  + p1.getPositions().get(0).getLatitude() + "-" + p1.getPositions().get(0).getLongitude());
                displayStatus("change icon size to MEDIUM");
            } catch (Exception e) {
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
