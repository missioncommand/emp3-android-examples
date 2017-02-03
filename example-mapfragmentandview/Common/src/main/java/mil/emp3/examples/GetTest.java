package mil.emp3.examples;

import android.app.Activity;
import android.util.Log;

import java.util.List;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.examples.common.TestBase;

public class GetTest extends TestBase implements Runnable {

    private static String TAG = GetTest.class.getSimpleName();

    public GetTest(Activity activity, IMap map1, IMap map2) {
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
            test1();
        } finally {
            testComplete();
        }
    }

    /*
        m1 -> o1 -> p1 -> p1_1
        m2 -> o2 -> p1 -> p1_1
              o2 -> p1_1
     */
    private void test0() {
        try {
            startTest("test0");
            Thread.sleep(waitInterval);

            try {
                m1.addOverlay(o1, true);
                m2.addOverlay(o2, true);

                o1.addFeature(p1, true);
                o2.addFeature(p1, true);
                p1.addFeature(p1_1, true);
                o2.addFeature(p1_1, true);
                Log.d(TAG, "Feature added to o1 and o2 visible on m1 and m2");
                updateTestStatus(2,2);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            List<IFeature> features = o1.getFeatures();
            for(IFeature feature: features) {
                displayStatus("o1 feature " + feature.getName());
            }

            features = o2.getFeatures();
            for(IFeature feature: features) {
                displayStatus("o2 feature " + feature.getName());
            }

            features = p1.getChildFeatures();
            for(IFeature feature: features) {
                displayStatus("p1 feature " + feature.getName());
            }

            features = m1.getAllFeatures();
            for(IFeature feature: features) {
                displayStatus("m1 feature " + feature.getName());
            }

            List<IOverlay> overlays = m1.getAllOverlays();
            for(IOverlay overlay: overlays) {
                displayStatus("m1 overlays " + overlay.getName());
            }

            overlays = o1.getOverlays();
            for(IOverlay overlay: overlays) {
                displayStatus("o1  overlays " + overlay.getName());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
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
    private void test1() {
        try {
            startTest("test1");
            Thread.sleep(waitInterval);

            try {
                m1.addOverlay(o1, true);
                m2.addOverlay(o2, true);
                o1.addOverlay(o3, true);

                o1.addOverlay(o2, true);
                o2.addOverlay(o3, true);
                o3.addFeature(p1, true);
                o2.addFeature(p2, true);
                o1.addFeature(p3, true);
                p1.addFeature(p1_1, true);
                o3.addFeature(p1_1, true);
                updateTestStatus(4, 3);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            // Let the adds run on ui thread
            Thread.sleep(waitInterval);

            List<IFeature> features = m1.getAllFeatures();
            Log.d(TAG, "m1 should have four features");
            for(IFeature feature: features) {
                displayStatus("m1 feature " + feature.getName());
            }

            features = m2.getAllFeatures();
            Log.d(TAG, "m2 should have three features");
            for(IFeature feature: features) {
                displayStatus("m2 feature " + feature.getName());
            }

            features = o1.getFeatures();
            Log.d(TAG, "o1 should have four features");
            for(IFeature feature: features) {
                displayStatus("o1 feature " + feature.getName());
            }

            features = o2.getFeatures();
            Log.d(TAG, "o2 should have three features");
            for(IFeature feature: features) {
                displayStatus("o2 feature " + feature.getName());
            }

            features = o3.getFeatures();
            Log.d(TAG, "o3 should have two features");
            for(IFeature feature: features) {
                displayStatus("o3 feature " + feature.getName());
            }

            java.util.List<IOverlay> overlays = m1.getAllOverlays();
            Log.d(TAG, "m1 should have three overlays");
            for(IOverlay overlay: overlays) {
                displayStatus("m1 overlays " + overlay.getName());
            }

            overlays = m2.getAllOverlays();
            Log.d(TAG, "m2 should have two overlays");
            for(IOverlay overlay: overlays) {
                displayStatus("m2 overlays " + overlay.getName());
            }

            overlays = o1.getOverlays();
            Log.d(TAG, "o1 should have two overlays");
            for(IOverlay overlay: overlays) {
                displayStatus("o1 overlays " + overlay.getName());
            }

            overlays = o3.getOverlays();
            Log.d(TAG, "o3 should have zero overlays");
            for(IOverlay overlay: overlays) {
                displayStatus("o3 overlays " + overlay.getName());
            }

            java.util.List<IContainer> containers = p1_1.getParents();
            Log.d(TAG, "p1_1 should have two parents one overlay and one feature");
            for(IContainer container: containers) {
                displayStatus("p1_1 parent " + container.getClass().getSimpleName() + " " + container.getName());
            }

            overlays = p1_1.getParentOverlays();
            Log.d(TAG, "p1_1 should have one parent overlay ");
            for(IContainer container: overlays) {
                displayStatus("p1_1 parent " + container.getClass().getSimpleName() + " " + container.getName());
            }

            features = p1_1.getParentFeatures();
            Log.d(TAG, "p1_1 should have one parent feature ");
            for(IContainer container: features) {
                displayStatus("p1_1 parent " + container.getClass().getSimpleName() + " " + container.getName());
            }

            Thread.sleep(waitInterval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }
}
