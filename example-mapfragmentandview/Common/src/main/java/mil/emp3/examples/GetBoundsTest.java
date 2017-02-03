package mil.emp3.examples;

import android.app.Activity;
import android.util.Log;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.IGeoBounds;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.examples.common.TestBase;

/**
 * Created by raju on 7/13/2016.
 */
public class GetBoundsTest extends TestBase implements Runnable{

    public GetBoundsTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public void run() {
        try {
            setExtents();
        } catch (Exception e) {
            Log.d(TAG, "run:" , e);
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
            Thread.sleep(medium_waitInterval);
            displayStatus("zoomTo on TRUCK0");
            m1.zoomTo(p1, false);
            Thread.sleep(medium_waitInterval);
            displayStatus("zoomTo on TRUCK2, TRUCK3");
            List<IFeature> list = new ArrayList<>();
            list.add(p2); list.add(p3);
            m1.zoomTo(list, false);
            Thread.sleep(medium_waitInterval);
            displayStatus("zoomTo o1, TRUCK0-3");
            m1.zoomTo(o1, false);
            Thread.sleep(medium_waitInterval);
            displayStatus("zoomTo o2, TRUCK0-2");
            m1.zoomTo(o2, false);
            Thread.sleep(medium_waitInterval);

            displayStatus("Updating symbol position");
            updateMilStdSymbolPosition(p1, 40.1, 179.5);
            updateMilStdSymbolPosition(p2, 40.4, -179.5);
            p1.apply(); p2.apply();
            Thread.sleep(medium_waitInterval);
            list.clear();
            list.add(p1); list.add(p2);
            displayStatus("zoomTo TRUCK0 and TRUCK2");
            m1.zoomTo(list, false);
            Thread.sleep(medium_waitInterval);
            displayStatus("zoomTo o1, TRUCK0-3");
            m1.zoomTo(o1, false);
            Thread.sleep(medium_waitInterval);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    private void test1() {
        try {
            setUp();
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
                Log.d(TAG, "Feature p1 added to o3 ");
                updateTestStatus(4,3);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            displayStatus("updating altitude for TRUCK3 to 300000");
            updateMilStdSymbolAltitude(p3, 300000);
            p3.apply();
            Thread.sleep(medium_waitInterval);
            displayStatus("zoomTo o1");
            m1.zoomTo(o1, false);
            Thread.sleep(medium_waitInterval);


        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    private void setExtents() {
        try {
            setUp();
            startTest("getBounds");
            displayStatus("getBounds");
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
            Thread.sleep(medium_waitInterval);
            IGeoBounds bounds = new GeoBounds();
            bounds.setSouth(-1.0);
            bounds.setNorth(1.0);
            bounds.setWest(-1.0);
            bounds.setEast(1.0);
            displayBounds(m1.getName() + " setBounds", bounds);
            m1.setBounds(bounds, false);
            displayBounds(m1.getName() + " getBounds", m1.getBounds());
            Thread.sleep(medium_waitInterval);

            bounds.setSouth(40.0);
            bounds.setNorth(42.0);
            bounds.setWest(-73.0);
            bounds.setEast(-71.0);
            displayBounds(m1.getName() + " setBounds", bounds);
            m1.setBounds(bounds, false);
            displayBounds(m1.getName() + " getBounds", m1.getBounds());
            Thread.sleep(medium_waitInterval);

            bounds.setSouth(40.0);
            bounds.setNorth(40.1);
            bounds.setWest(-73.0);
            bounds.setEast(-72.9);
            displayBounds(m1.getName() + " setBounds", bounds);
            m1.setBounds(bounds, false);
            displayBounds(m1.getName() + " getBounds", m1.getBounds());
            Thread.sleep(medium_waitInterval);


        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    private void poleTest() {
        try {
            setUp();
            startTest("poleTest");
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
            displayStatus("updating TRUCK3 latitude/longitude to 90.0/0.0");
            updateMilStdSymbolPosition(p3, 90.0, 0.0);
            p3.apply();
            Thread.sleep(waitInterval);
            displayStatus("zoomTo TRUCK3 90.0/0.0");
            m1.zoomTo(p3, false);
            Thread.sleep(medium_waitInterval);

            updateMilStdSymbolPosition(p3, 89.0, 0.0);
            p3.apply();
            Thread.sleep(waitInterval);

            IGeoBounds bounds = new GeoBounds();
            bounds.setSouth(89.0);
            bounds.setNorth(89.0);
            bounds.setWest(-1.0);
            bounds.setEast(1.0);
            displayBounds(m1.getName() + " setBounds", bounds);
            m1.setBounds(bounds, false);
            displayBounds(m1.getName() + " getBounds", m1.getBounds());
            Thread.sleep(medium_waitInterval);

            updateMilStdSymbolPosition(p3, 40.0, -73.0);
            p3.apply();
            Thread.sleep(waitInterval);

            bounds.setSouth(40.0);
            bounds.setNorth(40.0);
            bounds.setWest(-73.0);
            bounds.setEast(-73.0);
            displayBounds(m1.getName() + " setBounds", bounds);
            m1.setBounds(bounds, false);
            displayBounds(m1.getName() + " getBounds", m1.getBounds());
            Thread.sleep(medium_waitInterval);

            updateMilStdSymbolPosition(p3, 40.0, 180.0);
            p3.apply();
            Thread.sleep(waitInterval);
            displayStatus("zoomTo TRUCK3 40.0, 180.0");
            m1.zoomTo(p3, false);
            Thread.sleep(medium_waitInterval);

            updateMilStdSymbolPosition(p3, -40.0, -180.0);
            p3.apply();
            Thread.sleep(waitInterval);
            displayStatus("zoomTo TRUCK3 -40.0, -180.0");
            m1.zoomTo(p3, false);
            Thread.sleep(medium_waitInterval);

            updateMilStdSymbolPosition(p3, 40.0, 180.0);
            p3.apply();
            Thread.sleep(waitInterval);
            bounds.setSouth(40.0);
            bounds.setNorth(40.0);
            bounds.setWest(179.99);
            bounds.setEast(179.99);
            displayBounds(m1.getName() + " setBounds", bounds);
            m1.setBounds(bounds, false);
            displayBounds(m1.getName() + " getBounds", m1.getBounds());
            Thread.sleep(medium_waitInterval);

            updateMilStdSymbolPosition(p3, 40.0, -180.0);
            p3.apply();
            Thread.sleep(waitInterval);
            bounds.setSouth(40.0);
            bounds.setNorth(40.0);
            bounds.setWest(-179.99);
            bounds.setEast(-179.99);
            displayBounds(m1.getName() + " setBounds", bounds);
            m1.setBounds(bounds, false);
            displayBounds(m1.getName() + " getBounds", m1.getBounds());
            Thread.sleep(medium_waitInterval);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    private void globeSurround() {
        try {
            setUp();
            startTest("globeSurround");
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
            displayStatus("updating TRUCK0 latitude/longitude to 0.0/0.0");
            updateMilStdSymbolPosition(p1, 0.0, 0.0);
            p1.apply();

            displayStatus("updating TRUCK1 latitude/longitude to 0.0/90.0");
            updateMilStdSymbolPosition(p1_1, 0.0, 90.0);
            p1_1.apply();

            displayStatus("updating TRUCK2 latitude/longitude to 0.0/180.0");
            updateMilStdSymbolPosition(p2, 0.0, 180.0);
            p2.apply();

            displayStatus("updating TRUCK3 latitude/longitude to 0.0/-90.0");
            updateMilStdSymbolPosition(p3, 0.0, -90.0);
            p3.apply();
            Thread.sleep(medium_waitInterval);
            displayStatus("zoomTo TRUCK0-3");
            m1.zoomTo(o1, false);
            Thread.sleep(medium_waitInterval);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    private void performance() {
        try {
            setUp();
            startTest("performance");
            Thread.sleep(waitInterval);

            List<IFeature> list = generateMilStdSymbolList(1000, 30.0, -70.0);
            try {
                m1.addOverlay(o1, true);
                o1.addFeatures(list, true);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }

            Thread.sleep(medium_waitInterval);
            displayStatus("zoomTo o1");
            m1.zoomTo(o1, false);
            Thread.sleep(medium_waitInterval);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    private void displayBounds(String action, IGeoBounds bounds) {
        if (bounds != null) {
            String string =
                    this.getClass().getSimpleName() + " " + action + " " +
                            bounds.getSouth() + ", " +
                            bounds.getNorth() + ", " +
                            bounds.getWest() + ", " +
                            bounds.getEast();
            Log.d(TAG, string);
            updateStatus(string);
        } else {
            Log.e(TAG, " got bounds null ");
        }
        try {
            Thread.sleep(small_waitInterval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
