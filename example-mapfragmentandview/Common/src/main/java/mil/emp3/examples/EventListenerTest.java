package mil.emp3.examples;

import android.app.Activity;
import android.util.Log;

import mil.emp3.api.enums.VisibilityActionEnum;
import mil.emp3.api.events.CameraEvent;
import mil.emp3.api.events.ContainerEvent;
import mil.emp3.api.events.FeatureEvent;
import mil.emp3.api.events.FeatureUserInteractionEvent;
import mil.emp3.api.events.LookAtEvent;
import mil.emp3.api.events.MapFeatureAddedEvent;
import mil.emp3.api.events.MapFeatureRemovedEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.events.MapViewChangeEvent;
import mil.emp3.api.events.VisibilityEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.api.listeners.IContainerEventListener;
import mil.emp3.api.listeners.IFeatureEventListener;
import mil.emp3.api.listeners.IFeatureInteractionEventListener;
import mil.emp3.api.listeners.ILookAtEventListener;
import mil.emp3.api.listeners.IMapFeatureAddedEventListener;
import mil.emp3.api.listeners.IMapFeatureRemovedEventListener;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.listeners.IMapViewChangeEventListener;
import mil.emp3.api.listeners.IVisibilityEventListener;
import mil.emp3.examples.common.TestBase;

/**
 * Purpose of this test is to verify that EMP/Map is not affected if application throws an exception in the listener.
 * This test is also used to verify that all defined event listener are invoked
 * Some of the events not covered here are covered in Uni Tests (Editor events)
 * NOTE: There is no guard if application simply blocks the map.
 */

public class EventListenerTest  extends TestBase implements Runnable {

    private static String TAG = EventListenerTest.class.getSimpleName();
    private IOverlay[] overlays = new IOverlay[2];
    int featureCount = 0;

    public EventListenerTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public void run() {

        overlays[0] = o1;
        overlays[1] = o2;

        try {
            maps[0].setFarDistanceThreshold(620000);
            maps[0].setMidDistanceThreshold(610000);

            if(null != maps[1]) {
                maps[1].setFarDistanceThreshold(620000);
                maps[1].setMidDistanceThreshold(610000);
            }

            CameraEventListenerTest();
            ContainerEventListenerTest();
            // IDrawEventListener in Unit Tests
            // IEditEventListener in Unit Tests
            // IFeatureDrawEventListener in Unit Tests
            // IFeatureEditEventListener in Unit Tests
//            FeatureEventListenerTest(); // TODO EMP-2800 FeatureEvent ias not fired.
            FeatureInteractionEventListenerTest();
            // IFreehandEventListener in Unit Tests
            LookAtEventListenerTest();
            FeatureAddedRemovedListenerTest();
            // IMapFreehandEventListener in Unit Tests
            MapInteractionEventListenerTest();
            // IMapStateChangeEventListener TODO
            MapViewChangeEventListenerTest();
            VisibilityEventListenerTest();
        } catch (Exception e) {
            Log.d(TAG, "run:" , e);
        } finally {
            testComplete();
        }
    }

    class FeatureInteractionEventListener implements IFeatureInteractionEventListener {
        Integer featureInteractionEventListener = 0;
        Object lock = new Object();
        @Override
        public void onEvent(FeatureUserInteractionEvent event) {
            EventListenerTest.this.updateStatus("throwing Exception FeatureInteractionEventListener FeatureUserInteractionEvent");
            synchronized(lock) {
                featureInteractionEventListener++;
                lock.notify();
            }
            throw new NullPointerException("FeatureInteractionEventListener onEvent");
        }
    }

    private void FeatureInteractionEventListenerTest() {

        try {
            startTest("FeatureInteractionTest");
            Thread.sleep(waitInterval);
            maps[0].addOverlay(overlays[0], true);

            FeatureInteractionEventListener listener = new FeatureInteractionEventListener();
            EventListenerHandle handle = maps[0].addFeatureInteractionEventListener(listener);
            overlays[0].addFeature(generateMilStdSymbol("TRUCK" + featureCount++, maps[0].getCamera()), true);

            updateStatus("FeatureInteractionTest", "Please single Tap on the TRUCK to begin test");
            synchronized (listener.lock) {
                while(0 == listener.featureInteractionEventListener) {
                    listener.lock.wait();
                }
                listener.featureInteractionEventListener = 0;
            }
            updateStatus("FeatureInteractionTest", "Test is SUCCESS if you still see the TRUCK, tap on the TRUCK to end test");
            synchronized (listener.lock) {
                while(0 == listener.featureInteractionEventListener) {
                    listener.lock.wait();
                }
                listener.featureInteractionEventListener = 0;
            }

            maps[0].removeEventListener(handle);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    class MapInteractionEventListener implements IMapInteractionEventListener {

        Integer mapInteractionEventListener = 0;
        Object lock = new Object();

        @Override
        public void onEvent(MapUserInteractionEvent event) {
            // EventListenerTest.this.updateStatus("throwing Exception MapInteractionEventListener MapUserInteractionEvent");
            synchronized(lock) {
                mapInteractionEventListener++;
                lock.notify();
            }
            throw new NullPointerException("MapInteractionEventListener onEvent");
        }
    }

    private void MapInteractionEventListenerTest() {
        try {
            startTest("MapInteractionTest");
            Thread.sleep(waitInterval);
            maps[0].addOverlay(overlays[0], true);

            MapInteractionEventListener listener = new MapInteractionEventListener();
            EventListenerHandle handle = maps[0].addMapInteractionEventListener(listener);
            overlays[0].addFeature(generateMilStdSymbol("TRUCK" + featureCount++, maps[0].getCamera()), true);

            updateStatus("MapInteractionTest", "to begin the test, please single Tap anywhere on the map except the TRUCK");
            synchronized (listener.lock) {
                while(0 == listener.mapInteractionEventListener) {
                    listener.lock.wait();
                }
                listener.mapInteractionEventListener = 0;
            }
            updateStatus("MapInteractionTest", "Test is SUCCESS if you still see the TRUCK, tap on the TRUCK to end test");
            synchronized (listener.lock) {
                while(0 == listener.mapInteractionEventListener) {
                    listener.lock.wait();
                }
                listener.mapInteractionEventListener = 0;
            }

            maps[0].removeEventListener(handle);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    class MapViewChangeEventListener implements IMapViewChangeEventListener {

        Integer mapViewChangeEventListener = 0;
        Object lock = new Object();
        @Override
        public void onEvent(MapViewChangeEvent event) {
            EventListenerTest.this.updateStatus("throwing Exception MapViewChangeEventListener MapViewChangeEvent");
            synchronized(lock) {
                mapViewChangeEventListener++;
                lock.notify();
            }
            throw new NullPointerException("MapViewChangeEventListener onEvent");
        }
    }

    private void MapViewChangeEventListenerTest() {
        try {
            startTest("MapViewChangeTest");
            Thread.sleep(waitInterval);
            maps[0].addOverlay(overlays[0], true);

            MapViewChangeEventListener listener = new MapViewChangeEventListener();
            EventListenerHandle handle = maps[0].addMapViewChangeEventListener(listener);
            overlays[0].addFeature(generateMilStdSymbol("TRUCK" + featureCount++, maps[0].getCamera()), true);

            updateStatus("MapViewChangeTest", "Please pan the map to begin the test");
            synchronized (listener.lock) {
                while(0 == listener.mapViewChangeEventListener) {
                    listener.lock.wait();
                }
                listener.mapViewChangeEventListener = 0;
            }
            maps[0].removeEventListener(handle);

            FeatureInteractionEventListener listener2 = new FeatureInteractionEventListener();
            EventListenerHandle handle2 = maps[0].addFeatureInteractionEventListener(listener2);
            updateStatus("MapViewChangeTest", "Test is SUCCESS if you still see the TRUCK, tap on the TRUCK to end test");

            synchronized (listener2.lock) {
                while(0 == listener2.featureInteractionEventListener) {
                    listener2.lock.wait();
                }
                listener2.featureInteractionEventListener = 0;
            }
            maps[0].removeEventListener(handle2);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    class FeatureAddedListener implements IMapFeatureAddedEventListener {

        Integer featureAddedListener = 0;
        Object lock = new Object();
        @Override
        public void onEvent(MapFeatureAddedEvent event) {
            EventListenerTest.this.updateStatus("throwing Exception FeatureAddedListener MapFeatureAddedEvent");
            synchronized(lock) {
                featureAddedListener++;
                lock.notify();
            }
            throw new NullPointerException("FeatureAddedListener onEvent");
        }
    }

    class FeatureRemovedListener implements IMapFeatureRemovedEventListener {

        Integer featureRemovedListener = 0;
        Object lock = new Object();
        @Override
        public void onEvent(MapFeatureRemovedEvent event) {
            EventListenerTest.this.updateStatus("throwing Exception FeatureAddedListener MapFeatureRemovedEvent");
            synchronized(lock) {
                featureRemovedListener++;
                lock.notify();
            }
            throw new NullPointerException("FeatureRemovedListener onEvent");
        }
    }

    private void FeatureAddedRemovedListenerTest() {
        try {
            startTest("FeatureAddedRemovedTest");
            Thread.sleep(waitInterval);
            maps[0].addOverlay(overlays[0], true);

            FeatureAddedListener listener = new FeatureAddedListener();
            EventListenerHandle handle = maps[0].addMapFeatureAddedEventListener(listener);

            FeatureRemovedListener listener2 = new FeatureRemovedListener();
            EventListenerHandle handle2 = maps[0].addMapFeatureRemovedEventListener(listener2);

            IFeature truck = generateMilStdSymbol("TRUCK" + featureCount++, maps[0].getCamera());
            overlays[0].addFeature(truck, true);

            synchronized (listener.lock) {
                while(listener.featureAddedListener == 0) {
                    listener.lock.wait();
                }
                listener.featureAddedListener = 0;
            }

            updateStatus("FeatureAddedRemovedTest", "Test is SUCCESS if you still see the TRUCK, tap on the TRUCK to replace the TRUCK");

            FeatureInteractionEventListener listener3 = new FeatureInteractionEventListener();
            EventListenerHandle handle3 = maps[0].addFeatureInteractionEventListener(listener3);
            synchronized (listener3.lock) {
                while(listener3.featureInteractionEventListener == 0) {
                    listener3.lock.wait();
                }
                listener3.featureInteractionEventListener = 0;
            }

            overlays[0].removeFeature(truck);
            synchronized (listener2.lock) {
                while(listener2.featureRemovedListener == 0) {
                    listener2.lock.wait();
                }
                listener2.featureRemovedListener = 0;
            }
            overlays[0].addFeature(generateMilStdSymbol("TRUCK" + featureCount++, maps[0].getCamera()), true);
            updateStatus("FeatureAddedRemovedTest", "Test is SUCCESS if you see a new TRUCK, tap on the TRUCK to end the test");
            synchronized (listener3.lock) {
                while(0 == listener3.featureInteractionEventListener) {
                    listener3.lock.wait();
                }
                listener3.featureInteractionEventListener = 0;
            }

            maps[0].removeEventListener(handle);
            maps[0].removeEventListener(handle2);
            maps[0].removeEventListener(handle3);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    class VisibilityEventListener implements IVisibilityEventListener {
        Integer visibilityEventListener = 0;
        Object lock = new Object();
        @Override
        public void onEvent(VisibilityEvent event) {
            EventListenerTest.this.updateStatus("throwing Exception VisibilityEventListener VisibilityEvent");
            synchronized(lock) {
                visibilityEventListener++;
                lock.notify();
            }
            throw new NullPointerException("VisibilityEventListener onEvent");
        }
    }

    private void VisibilityEventListenerTest() {

        try {
            startTest("VisibilityTest");
            Thread.sleep(waitInterval);
            maps[0].addOverlay(overlays[0], true);

            VisibilityEventListener listener = new VisibilityEventListener();
            EventListenerHandle handle = maps[0].addVisibilityEventListener(listener);

            IFeature truck = generateMilStdSymbol("TRUCK" + featureCount++, maps[0].getCamera());
            overlays[0].addFeature(truck, true);

            FeatureInteractionEventListener listener2 = new FeatureInteractionEventListener();
            EventListenerHandle handle2 = maps[0].addFeatureInteractionEventListener(listener2);

            updateStatus("VisibilityTest", "Please single Tap on the TRUCK, to make it invisible");
            synchronized (listener2.lock) {
                while(0 == listener2.featureInteractionEventListener) {
                    listener2.lock.wait();
                }
                listener2.featureInteractionEventListener = 0;
            }

            maps[0].setVisibility(truck, VisibilityActionEnum.TOGGLE_OFF);
            synchronized (listener.lock) {
                while(listener.visibilityEventListener == 0) {
                    listener.lock.wait();
                }
                listener.visibilityEventListener = 0;
            }

            updateStatus("VisibilityTest", "If TRUCK is no longer visible then test is SUCCESS, TAP on the map few times to make TRUCK visible again");

            MapInteractionEventListener listener3 = new MapInteractionEventListener();
            EventListenerHandle handle3 = maps[0].addMapInteractionEventListener(listener3);
            Thread.sleep(small_waitInterval);

            synchronized (listener3.lock) {
                while(5 > listener3.mapInteractionEventListener) {
                    listener3.lock.wait();
                }
                listener3.mapInteractionEventListener = 0;
            }

            maps[0].setVisibility(truck, VisibilityActionEnum.TOGGLE_ON);
            synchronized (listener.lock) {
                while(listener.visibilityEventListener == 0) {
                    listener.lock.wait();
                }
                listener.visibilityEventListener = 0;
            }
            updateStatus("VisibilityTest", "If TRUCK is visible then test is SUCCESS, TAP on the TRUCK to end the test");
            synchronized (listener2.lock) {
                while(0 == listener2.featureInteractionEventListener) {
                    listener2.lock.wait();
                }
                listener2.featureInteractionEventListener = 0;
            }
            maps[0].removeEventListener(handle2);
            maps[0].removeEventListener(handle);
            maps[0].removeEventListener(handle3);

        } catch (EMP_Exception e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    class ContainerEventListener implements IContainerEventListener {

        Object lock = new Object();
        Integer containerEventListener = 0;
        @Override
        public void onEvent(ContainerEvent event) {
            EventListenerTest.this.updateStatus("throwing Exception ContainerEventListener ContainerEvent");
            synchronized(lock) {
                containerEventListener++;
                lock.notify();
            }
            throw new NullPointerException("ContainerEventListener onEvent");
        }
    }

    private void ContainerEventListenerTest() {

        try {
            startTest("ContainerTest");

            ContainerEventListener listener = new ContainerEventListener();
            EventListenerHandle handle = maps[0].addContainerEventListener(listener);

            updateStatus("ContainerTest", "Waiting for ContainerEvent after adding an overlay and feature");
            Thread.sleep(waitInterval);
            maps[0].addOverlay(overlays[0], true);

            synchronized (listener.lock) {
                while(listener.containerEventListener == 0) {
                    listener.lock.wait();
                }
                listener.containerEventListener = 0;
            }

            IFeature truck = generateMilStdSymbol("TRUCK" + featureCount++, maps[0].getCamera());
            overlays[0].addFeature(truck, true);

            synchronized (listener.lock) {
                while(listener.containerEventListener == 0) {
                    listener.lock.wait();
                }
                listener.containerEventListener = 0;
            }

            FeatureInteractionEventListener listener2 = new FeatureInteractionEventListener();
            EventListenerHandle handle2 = maps[0].addFeatureInteractionEventListener(listener2);

            updateStatus("ContainerTest", "If you see a TRUCK on the map the test is SUCCESS, TAP on the TRUCK to end the test");

            synchronized (listener2.lock) {
                while(0 == listener2.featureInteractionEventListener) {
                    listener2.lock.wait();
                }
                listener2.featureInteractionEventListener = 0;
            }
            maps[0].removeEventListener(handle2);
            maps[0].removeEventListener(handle);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    class CameraEventListener implements ICameraEventListener {
        Object lock = new Object();
        Integer cameraEventListener = 0;

        @Override
        public void onEvent(CameraEvent event) {
            // EventListenerTest.this.updateStatus("throwing Exception CameraEventListener CameraEvent");
            synchronized(lock) {
                cameraEventListener++;
                lock.notify();
            }
            throw new NullPointerException("CameraEventListener onEvent");
        }
    }

    private void CameraEventListenerTest() {

        try {
            startTest("CameraTest");

            ICamera camera = maps[0].getCamera();
            CameraEventListener listener = new CameraEventListener();
            EventListenerHandle handle = camera.addCameraEventListener(listener);

            updateStatus("CameraTest", "Begin test by performing few zoom/pan/roll operations on the map");

            synchronized (listener.lock) {
                while(5 > listener.cameraEventListener) {
                    listener.lock.wait();
                }
                listener.cameraEventListener = 0;
            }

            FeatureInteractionEventListener listener2 = new FeatureInteractionEventListener();
            EventListenerHandle handle2 = maps[0].addFeatureInteractionEventListener(listener2);
            maps[0].addOverlay(overlays[0], true);
            IFeature truck = generateMilStdSymbol("TRUCK" + featureCount++, maps[0].getCamera());
            overlays[0].addFeature(truck, true);

            updateStatus("CameraTest", "Test is SUCCESS if you see the TRUCK, tap on the TRUCK to end test");
            synchronized (listener2.lock) {
                while(0 == listener2.featureInteractionEventListener) {
                    listener2.lock.wait();
                }
                listener2.featureInteractionEventListener = 0;
            }
            maps[0].removeEventListener(handle2);
            maps[0].removeEventListener(handle);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    class FeatureEventListener implements IFeatureEventListener {
        Object lock = new Object();
        Integer featureEventListener = 0;

        @Override
        public void onEvent(FeatureEvent event) {
            // EventListenerTest.this.updateStatus("throwing Exception CameraEventListener CameraEvent");
            synchronized(lock) {
                featureEventListener++;
                lock.notify();
            }
            throw new NullPointerException("FeatureEventListener onEvent");
        }
    }

    private void FeatureEventListenerTest() {

        try {
            startTest("FeatureTest");
            Thread.sleep(waitInterval);
            maps[0].addOverlay(overlays[0], true);

            FeatureEventListener listener = new FeatureEventListener();
            EventListenerHandle handle = maps[0].addFeatureEventListener(listener);
            IFeature truck = generateMilStdSymbol("TRUCK" + featureCount++, maps[0].getCamera());
            overlays[0].addFeature(truck, true);

            FeatureInteractionEventListener listener2 = new FeatureInteractionEventListener();
            EventListenerHandle handle2 = maps[0].addFeatureInteractionEventListener(listener2);

            updateStatus("FeatureTest", "Please single Tap on the TRUCK to begin test");
            synchronized (listener2.lock) {
                while(0 == listener2.featureInteractionEventListener) {
                    listener2.lock.wait();
                }
                listener2.featureInteractionEventListener = 0;
            }

            maps[0].selectFeature(truck);
            synchronized (listener.lock) {
                while(0 == listener.featureEventListener) {
                    listener.lock.wait();
                }
                listener.featureEventListener = 0;
            }

            maps[0].removeEventListener(handle);
            maps[0].removeEventListener(handle2);

        } catch (EMP_Exception e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    class LookAtEventListener implements ILookAtEventListener {
        Object lock = new Object();
        Integer lookAtEventListener = 0;

        @Override
        public void onEvent(LookAtEvent event) {
            // EventListenerTest.this.updateStatus("throwing Exception CameraEventListener CameraEvent");
            synchronized(lock) {
                lookAtEventListener++;
                lock.notify();
            }
            throw new NullPointerException("LookAtEventListener onEvent");
        }
    }

    private void LookAtEventListenerTest() {

        try {
            startTest("LookAtTest");

            ICamera camera = maps[0].getCamera();
            ILookAt lookAt = maps[0].getLookAt();

            LookAtEventListener listener = new LookAtEventListener();
            EventListenerHandle handle = lookAt.addLookAtEventListener(listener);

            updateStatus("LookAtTest", "Begin test by performing few zoom/pan/roll operations on the map");

            synchronized (listener.lock) {
                while(5 > listener.lookAtEventListener) {
                    listener.lock.wait();
                }
                listener.lookAtEventListener = 0;
            }

            FeatureInteractionEventListener listener2 = new FeatureInteractionEventListener();
            EventListenerHandle handle2 = maps[0].addFeatureInteractionEventListener(listener2);
            maps[0].addOverlay(overlays[0], true);
            IFeature truck = generateMilStdSymbol("TRUCK" + featureCount++, maps[0].getCamera());
            overlays[0].addFeature(truck, true);

            updateStatus("LookAtTest", "Test is SUCCESS if you see the TRUCK, tap on the TRUCK to end test");
            synchronized (listener2.lock) {
                while(0 == listener2.featureInteractionEventListener) {
                    listener2.lock.wait();
                }
                listener2.featureInteractionEventListener = 0;
            }
            maps[0].removeEventListener(handle2);
            maps[0].removeEventListener(handle);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }
}
