package mil.emp3.examples;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.examples.common.TestBase;

public class RemoveTest extends TestBase implements Runnable {

    private static String TAG = RemoveTest.class.getSimpleName();

    public RemoveTest(Activity activity, IMap map1, IMap map2) {
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
            test2();
            test3();
            test4();
            test5();
            test6();
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
                updateTestStatus(2, 2);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }

            Thread.sleep(waitInterval);

            try {
                o1.removeFeature(p1);
                updateTestStatus(0, 2);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Thread.sleep(waitInterval);

            try {
                o2.removeFeature(p1);
                updateTestStatus(0, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Thread.sleep(waitInterval);

            try {
                o2.removeFeature(p1_1);
                updateTestStatus(0, 0);
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
    // At the end of the test no features should be on the screen
    private void test1() {
        try {

            startTest("test1");

            try {
                m1.addOverlay(o1, true);
                o1.addFeature(p1, true);
                Log.d(TAG, "Feature p1 added to overlay1 on m1");
                p1.addFeature(p2, true);
                Log.d(TAG, "Feature p2 added to feature p1 on m1");
                updateTestStatus(2, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                p1.removeFeature(p2);
                Log.d(TAG, "Feature p2 removed from p1 on m1" + "TRUCK2");
                updateTestStatus(1, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                o1.removeFeature(p1);
                Log.d(TAG, "Feature p1 removed from o1 on m1 map should be empty" + "TRUCK2");
                updateTestStatus(0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                o1.addFeature(p1, true);
                p1.addFeature(p2, true);
                Log.d(TAG, "Added o1->p1 and p1->p2");
                updateTestStatus(2, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                o1.removeFeature(p1);
                Log.d(TAG, "Removed o1->p1 map should be empty");
                updateTestStatus(0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);
        }  catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    // A feature is added to another feature and a an overlay and then simply removed from the parent feature.
    // It should still remain visible. Issue in StorageObjectWrapper addParent was fixed.

    private void test2() {
        try {

            startTest("test2");

            try {
                m1.addOverlay(o1, true);
                m2.addOverlay(o2, true);
                o1.addFeature(p1, true);
                Log.d(TAG, "Feature p1 added ");
                p1.addFeature(p2, true);
                o2.addFeature(p2, true);
                Log.d(TAG, "Feature p2 added to p1 and overlay2 ");
                updateTestStatus(2, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                p1.removeFeature(p2);
                Log.d(TAG, "Feature p2 removed from p1" + "TRUCK2");
                updateTestStatus(1, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                o2.removeFeature(p2);
                Log.d(TAG, "Feature p2 removed from overlay 2" + "TRUCK2");
                updateTestStatus(1, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                o1.removeFeature(p1);
                Log.d(TAG, "Feature p1 removed from overlay1 " + "TRUCK1");
                updateTestStatus(0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    // Remove a list of features where some in the list may have parent children relationship.

    private void test3() {
        try {

            startTest("test3");

            try {
                m1.addOverlay(o1, true);
                o1.addFeature(p1, true);
                Log.d(TAG, "Feature p1 added ");
                updateTestStatus(1, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                m2.addOverlay(o2, true);
                o1.addFeature(p2, true);
                o2.addFeature(p2, true);
                Log.d(TAG, "Feature p2 added to p1 and overlay2 ");
                updateTestStatus(2, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                List<IFeature> list = new ArrayList<>();
                list.add(p1); list.add(p2);
                o1.removeFeatures(list);

                Log.d(TAG, "Feature p2 & P1 removed from overlay");
                Log.d(TAG, "Feature p2 should stay around a it is on overlay2 also");
                updateTestStatus(0, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                o2.removeFeature(p2);
                Log.d(TAG, "Feature p2 removed from overlay 2");
                updateTestStatus(0, 0);
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

    /*
        m1 -> o1, o1 -> o2, o3
        m2 -> o2
        o2 -> o3
        o3 -> p1

     */
    private void test4() {
        try {
            startTest("test4");
            Thread.sleep(waitInterval);

            try {
                m1.addOverlay(o1, true);
                m2.addOverlay(o2, true);
                o1.addOverlay(o3, true);

                o1.addOverlay(o2, true);
                o2.addOverlay(o3, true);
                o3.addFeature(p1, true);

                Log.d(TAG, "Feature p1 added to o3 ");
                updateTestStatus(1, 1);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                m1.removeOverlay(o1);
                Log.d(TAG, "Removed o1 from m1 feature should stiil be visible on m2");
                updateTestStatus(0, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                o2.removeOverlay(o3);
                Log.d(TAG, "Removed o3 from o2 feature should now disapear from m2");
                updateTestStatus(0, 0);
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

    /*
       m1 -> o1, o1 -> o2, o3
       m2 -> o2
       o2 -> o3
       o3 -> p1

    */
    private void test5() {
        try {
            startTest("test5");
            Thread.sleep(waitInterval);

            try {
                m1.addOverlay(o1, true);
                m2.addOverlay(o2, true);
                o1.addOverlay(o3, true);

                o1.addOverlay(o2, true);
                o2.addOverlay(o3, true);
                o3.addFeature(p1, true);

                Log.d(TAG, "Feature p1 added to o3 ");
                updateTestStatus(1, 1);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                o1.removeOverlay(o2);
                Log.d(TAG, "Removed o2 from o1 feature should stiil be visible on both maps");
                updateTestStatus(1, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                o2.removeOverlay(o3);
                Log.d(TAG, "Removed o3 from o2 feature should now be visible on map1 only");
                updateTestStatus(1, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                o1.removeOverlay(o3);
                Log.d(TAG, "Removed o3 from o1 feature should now be gone from both maps");
                updateTestStatus(0, 0);
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

    /*
      m1 -> o1, o1 -> o2, o3
      m2 -> o2
      o2 -> o3
      o3 -> p1
      o2 -> p2
      o1 -> p3
      p1 -> p1_1

   */
    private void test6() {
        try {
            startTest("test6");
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
                Log.d(TAG, "Feature p1 added to o3 ");
                updateTestStatus(4,3);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                o1.clearContainer();
                Log.d(TAG, "Clear Container o1: all features from map1 should disappear");
                updateTestStatus(0,3);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            try {
                o3.clearContainer();
                Log.d(TAG, "Clear Container o3 one of three features should remain on map2");
                updateTestStatus(0,1);
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


