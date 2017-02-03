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

public class ZoomToTest extends TestBase implements Runnable{
    public ZoomToTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public void run() {
//        if((null == m1) || (null == m2)) {
//            testComplete("THIS IS A MULTIMAP TEST, CANNOT BE RUN IN THIS APPLICATION");
//            return;
//        }
        try {
            test0();
            altitudeTest();
            setBounds();
            poleTest();
            globeSurround();
            performance();
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
    private void initializeTest() {
        try {

            m1.setMidDistanceThreshold(500000);
            m1.setFarDistanceThreshold(1000000);

            updateDesignator(p1);
            updateDesignator(p2);
            updateDesignator(p3);
            updateDesignator(p1_1);

            m1.addOverlay(o1, true);
            if(null != m2) {
                m2.addOverlay(o2, true);
            }
            o1.addOverlay(o3, true);

            o1.addOverlay(o2, true);
            o2.addOverlay(o3, true);
            o3.addFeature(p1, true);
            o2.addFeature(p2, true);
            o1.addFeature(p3, true);
            p1.addFeature(p1_1, true);
            o3.addFeature(p1_1, true);
            updateTestStatus(4,3);
        } catch (EMP_Exception e) {
            e.printStackTrace();
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
            Thread.sleep(small_waitInterval);

            initializeTest();

            Thread.sleep(small_waitInterval);
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
            Thread.sleep(small_waitInterval);
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

    private void altitudeTest() {
        try {
            setUp();
            startTest("altitudeTest");
            Thread.sleep(small_waitInterval);

            initializeTest();

            displayStatus("updating altitude for TRUCK3 to 300000");
            updateMilStdSymbolAltitude(p3, 300000);
            p3.apply();
            Thread.sleep(small_waitInterval);
            displayStatus("zoomTo o1");
            m1.zoomTo(o1, false);
            Thread.sleep(medium_waitInterval);

            displayStatus("updating altitude for TRUCK3 to 100");
            updateMilStdSymbolAltitude(p3, 100);
            p3.apply();
            Thread.sleep(small_waitInterval);
            displayStatus("zoomTo o1");
            m1.zoomTo(o1, false);
            Thread.sleep(medium_waitInterval);

            displayStatus("zoomTo TRUCK3");
            m1.zoomTo(p3, false);
            Thread.sleep(medium_waitInterval);

            displayStatus("updating altitude for TRUCK2 to 1000");
            updateMilStdSymbolAltitude(p2, 1000);
            p2.apply();
            Thread.sleep(small_waitInterval);
            displayStatus("zoomTo o1");
            m1.zoomTo(o1, false);
            Thread.sleep(medium_waitInterval);

            displayStatus("zoomTo TRUCK2");
            m1.zoomTo(p2, false);
            Thread.sleep(medium_waitInterval);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    private void setBounds() {
        try {
            setUp();
            startTest("setBounds");
            Thread.sleep(waitInterval);

            initializeTest();

            updateMilStdSymbolPosition(p1, -.9, -.9 );
            updateMilStdSymbolPosition(p1_1, -.9, .9);
            updateMilStdSymbolPosition(p2, .9, .9);
            updateMilStdSymbolPosition(p3, .9, -.9);
            p1.apply();
            p1_1.apply();
            p2.apply();
            p3.apply();
            Thread.sleep(medium_waitInterval);
            displayStatus("setBounds -1.0, 1.0, -1.0, 1.0 TRCUK0-3 at four corners");
            IGeoBounds bounds = new GeoBounds();
            bounds.setSouth(-1.0);
            bounds.setNorth(1.0);
            bounds.setWest(-1.0);
            bounds.setEast(1.0);
            m1.setBounds(bounds, false);
            Thread.sleep(medium_waitInterval);
            updateMilStdSymbolPosition(p1, 40.1, -72.9 );
            updateMilStdSymbolPosition(p1_1, 40.1, -71.1);
            updateMilStdSymbolPosition(p2, 41.9, -72.9);
            updateMilStdSymbolPosition(p3, 41.9, -71.1);
            p1.apply();
            p1_1.apply();
            p2.apply();
            p3.apply();
            displayStatus("setBounds 40.0, 42.0, -73.0, -71.0 TRCUK0-3 at four corners");
            bounds.setSouth(40.0);
            bounds.setNorth(42.0);
            bounds.setWest(-73.0);
            bounds.setEast(-71.0);
            m1.setBounds(bounds, false);
            Thread.sleep(medium_waitInterval);

            displayStatus("setBounds 40.0, 40.1, -73.0, -72.9 TRCUK0-3 at four corners");
            updateMilStdSymbolPosition(p1, 40.01, -72.99 );
            updateMilStdSymbolPosition(p1_1, 40.01, -72.91);
            updateMilStdSymbolPosition(p2, 40.09, -72.99);
            updateMilStdSymbolPosition(p3, 40.09, -72.91);
            p1.apply();
            p1_1.apply();
            p2.apply();
            p3.apply();
            bounds.setSouth(40.0);
            bounds.setNorth(40.1);
            bounds.setWest(-73.0);
            bounds.setEast(-72.9);
            m1.setBounds(bounds, false);
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

            initializeTest();

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
            displayStatus("setBounds 89.0, 1.0, 89.0, -1.0, TRUCK3 89.0, 0.0");
            bounds.setSouth(89.0);
            bounds.setNorth(89.0);
            bounds.setWest(-1.0);
            bounds.setEast(1.0);
            m1.setBounds(bounds, false);
            Thread.sleep(medium_waitInterval);

            updateMilStdSymbolPosition(p3, 40.0, -73.0);
            p3.apply();
            Thread.sleep(waitInterval);

            displayStatus("setBounds 40.0, 40.0, -73.0, -73.0, TRUCK3 40.0, -73.0");
            bounds.setSouth(40.0);
            bounds.setNorth(40.0);
            bounds.setWest(-73.0);
            bounds.setEast(-73.0);
            m1.setBounds(bounds, false);
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
            displayStatus("setBounds 40.0, 40.0, 179.99, 179.99, TRUCK3 40.0, 180.0");
            bounds.setSouth(40.0);
            bounds.setNorth(40.0);
            bounds.setWest(179.99);
            bounds.setEast(179.99);
            m1.setBounds(bounds, false);
            Thread.sleep(medium_waitInterval);

            updateMilStdSymbolPosition(p3, 40.0, -180.0);
            p3.apply();
            Thread.sleep(waitInterval);
            displayStatus("setBounds 40.0, 40.0, -179.99, -179.99, TRUCK3 40.0, -180.0");
            bounds.setSouth(40.0);
            bounds.setNorth(40.0);
            bounds.setWest(-179.99);
            bounds.setEast(-179.99);
            m1.setBounds(bounds, false);
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

            initializeTest();

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
}
