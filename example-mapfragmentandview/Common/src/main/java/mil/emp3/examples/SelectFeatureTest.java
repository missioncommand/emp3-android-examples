package mil.emp3.examples;

import android.app.Activity;
import android.util.Log;

import java.util.List;
import java.util.Random;

import mil.emp3.api.enums.UserInteractionEventEnum;
import mil.emp3.api.events.FeatureEvent;
import mil.emp3.api.events.FeatureUserInteractionEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.IFeatureEventListener;
import mil.emp3.api.listeners.IFeatureInteractionEventListener;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.examples.common.TestBase;

/**
 * Test selectFeature(s), deselectFeature(s), clearSelected, isSelected and getSelected
 * Also test FeatureEventListener
 */
public class SelectFeatureTest extends TestBase implements Runnable {

    public SelectFeatureTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    private IOverlay[] overlays = new IOverlay[2];
    Thread testThread;

    private int featureCount = 0;
    FeatureInteractionEventListener[] fiel = new FeatureInteractionEventListener[2];
    EventListenerHandle[] fielHandle = new EventListenerHandle[2] ;
    MapInteractionEventListener[] miel = new MapInteractionEventListener[2];
    EventListenerHandle[] mielHandle = new EventListenerHandle[2];
    FeatureEventListener[] fel = new FeatureEventListener[2];
    EventListenerHandle[] felHandle = new EventListenerHandle[2];

    Thread selectorThread = null;
    Thread deselectorThread = null;
    Thread adderThread = null;
    Thread removerThread = null;

    boolean multiThreadTestInProgress = false;

    @Override
    public void run() {

        overlays[0] = o1;
        overlays[1] = o2;

        try {
            fiel[0] = new FeatureInteractionEventListener(0);
            miel[0] = new MapInteractionEventListener(0);
            fiel[1] = new FeatureInteractionEventListener(1);
            miel[1] = new MapInteractionEventListener(1);
            fel[0] = new FeatureEventListener(0);
            fel[1] = new FeatureEventListener(1);

            try {
                maps[0].addOverlay(overlays[0], true);
                maps[0].setFarDistanceThreshold(620000);
                maps[0].setMidDistanceThreshold(610000);

                if(null != maps[1]) {
                    maps[1].addOverlay(overlays[1], true);
                    maps[1].setFarDistanceThreshold(620000);
                    maps[1].setMidDistanceThreshold(610000);
                }
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            test0();
        } catch (Exception e) {
            Log.d(TAG, "run:" , e);
        } finally {
            testComplete();
        }
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"MultiThread-Start", "MultiThread-Stop", "ClearMap", "Exit" };
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        if(null == m2) {
            String[] actions = {"AddFeatures-1", "StartTest-1", "ClearSelected-1", "GetSelected-1", "IsSelected-1", "ListenerTest-1"};
            return actions;
        } else {
            String[] actions = { "AddFeatures-1", "StartTest-1", "ClearSelected-1", "GetSelected-1", "IsSelected-1", "ListenerTest-1",
                    "AddFeatures-2", "StartTest-2", "ClearSelected-2", "GetSelected-2", "IsSelected-2", "ListenerTest-2"
            };
            return actions;
        }
    }

    private void removeListeners() {
        if(fielHandle[0] != null) maps[0].removeEventListener(fielHandle[0]);
        if(fielHandle[1] != null) maps[1].removeEventListener(fielHandle[1]);
        if(mielHandle[0] != null) maps[0].removeEventListener(mielHandle[0]);
        if(mielHandle[1] != null) maps[1].removeEventListener(mielHandle[1]);
        if(felHandle[0] != null) maps[0].removeEventListener(felHandle[0]);
        if(felHandle[1] != null) maps[1].removeEventListener(felHandle[1]);

        fielHandle[0] = null;
        fielHandle[1] = null;
        mielHandle[0] = null;
        mielHandle[1] = null;
        felHandle[0] = null;
        felHandle[1] = null;
    }
    @Override
    public void actOn(String userAction) {

        int whichMap = userAction.contains("-1") ? 0 : 1;
        try {

            if(multiThreadTestInProgress) {
                if(!userAction.equals("MultiThread-Stop")) {
                    updateStatus("Please stop MultiThread Test first");
                    return;
                } else {
                    stopMultiThreadTest(0);
                    return;
                }
            }

            if (userAction.equals("Exit")) {
                removeListeners();
                testThread.interrupt();
            } else if (userAction.contains("AddFeatures-")) {
                if (null != fielHandle[whichMap]) {
                    maps[whichMap].removeEventListener(fielHandle[whichMap]);
                    fielHandle[whichMap] = null;
                }
                if (null != felHandle[whichMap]) {
                    maps[whichMap].removeEventListener(felHandle[whichMap]);
                    felHandle[whichMap] = null;
                }
                if (null == mielHandle[whichMap]) {
                    mielHandle[whichMap] = maps[whichMap].addMapInteractionEventListener(miel[whichMap]);
                }
                updateStatus("map" + String.valueOf(whichMap) + " " + "Tap to add features");
            } else if (userAction.contains("StartTest-")) {
                if (null != mielHandle[whichMap]) {
                    maps[whichMap].removeEventListener(mielHandle[whichMap]);
                    mielHandle[whichMap] = null;
                }
                if (null != felHandle[whichMap]) {
                    maps[whichMap].removeEventListener(felHandle[whichMap]);
                    felHandle[whichMap] = null;
                }
                if (null == fielHandle[whichMap]) {
                    fielHandle[whichMap] = maps[whichMap].addFeatureInteractionEventListener(fiel[whichMap]);
                }
                fiel[whichMap].setSelectedMode(false);
                updateStatus("map" + String.valueOf(whichMap) + " " + "Single tap to select and double tap to deselect");
            } else if (userAction.contains("ListenerTest-")) {
                if (null != mielHandle[whichMap]) {
                    maps[whichMap].removeEventListener(mielHandle[whichMap]);
                    mielHandle[whichMap] = null;
                }
                if (null == felHandle[whichMap]) {
                    felHandle[whichMap] = maps[whichMap].addFeatureEventListener(fel[whichMap]);
                }
                if (null == fielHandle[whichMap]) {
                    fielHandle[whichMap] = maps[whichMap].addFeatureInteractionEventListener(fiel[whichMap]);
                }
                fiel[whichMap].setSelectedMode(false);
                updateStatus("map" + String.valueOf(whichMap) + " " + "Single tap to select and double tap to deselect");
            }
            else if (userAction.contains("IsSelected-")) {
                if (null != mielHandle[whichMap]) {
                    maps[whichMap].removeEventListener(mielHandle[whichMap]);
                    mielHandle[whichMap] = null;
                }
                if (null != felHandle[whichMap]) {
                    maps[whichMap].removeEventListener(felHandle[whichMap]);
                    felHandle[whichMap] = null;
                }
                if (null == fielHandle[whichMap]) {
                    fielHandle[whichMap] = maps[whichMap].addFeatureInteractionEventListener(fiel[whichMap]);
                }
                fiel[whichMap].setSelectedMode(true);
                updateStatus("map" + String.valueOf(whichMap) + " " + "Single tap to select and double tap to deselect");
            }  else if (userAction.contains("ClearSelected-")) {
                maps[whichMap].clearSelected();
            } else if (userAction.contains("GetSelected-")) {
                List<IFeature> selected = maps[whichMap].getSelected();
                String selectedString = "Selected: ";
                for(IFeature feature: selected) {
                    selectedString += " " + feature.getDescription();
                }
                updateStatus(selectedString);
            }  else if (userAction.contains("ClearMap")) {
                if(null != maps[0]) {
                    overlays[0].removeFeatures(overlays[0].getFeatures());
                }
                if(null != maps[1]) {
                    overlays[1].removeFeatures(overlays[1].getFeatures());
                }
            } else if (userAction.contains("MultiThread-Start")) {
                startMultiThreadTest(0);
            }  else {
                updateStatus("map" + String.valueOf(whichMap) + " " + userAction + " NOT supported");
            }
        } catch (EMP_Exception e) {
            updateStatus("map" + String.valueOf(whichMap) + " " + userAction + " FAILED because " + e.getMessage());
        }
    }

    private void test0() {

        try {
            testThread = Thread.currentThread();
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(large_waitInterval * 10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } finally {
            endTest();
        }
    }

    class FeatureInteractionEventListener implements IFeatureInteractionEventListener {
        private String TAG = UserInteractionTest.FeatureInteractionEventListener.class.getSimpleName();
        private int whichMap;
        private boolean isSelectedMode = false;

        public void setSelectedMode(boolean selectedMode) {
            isSelectedMode = selectedMode;
        }

        FeatureInteractionEventListener(int whichMap) {
            this.whichMap = whichMap;
        }
        @Override
        public void onEvent(FeatureUserInteractionEvent event) {

            Log.d(TAG, "onEvent " + event.getEvent().name());
            for (IFeature f : event.getTarget()) {
                Log.d(TAG, "onEvent " + event.getEvent().name() + " on " + f.getDescription());
            }

            if (event.getEvent().compareTo(UserInteractionEventEnum.DOUBLE_CLICKED) == 0) {
                if(!isSelectedMode) {
                    Log.d(TAG, "deselect features");
                    if (event.getTarget().size() == 1) {
                        maps[whichMap].deselectFeature(event.getTarget().get(0));
                    } else if (event.getTarget().size() > 1) {
                        maps[whichMap].deselectFeatures(event.getTarget());
                    }
                }
            } else if(event.getEvent().compareTo(UserInteractionEventEnum.CLICKED) == 0) {
                if(!isSelectedMode) {
                    Log.d(TAG, "select features");
                    if (event.getTarget().size() == 1) {
                        maps[whichMap].selectFeature(event.getTarget().get(0));
                    } else if (event.getTarget().size() > 1) {
                        maps[whichMap].selectFeatures(event.getTarget());
                    }
                } else {
                    String selectStatus = "Select Status:";
                    for(IFeature feature: event.getTarget()) {
                        if(maps[whichMap].isSelected(feature)) {
                            selectStatus += " " + feature.getDescription() + ":Y";
                        } else {
                            selectStatus += " " + feature.getDescription() + ":N";
                        }
                    }
                    updateStatus(selectStatus);
                }
            } else if(event.getEvent().compareTo(UserInteractionEventEnum.DRAG) == 0) {
                Log.e(TAG, "Application Received DRAG event");
            } else if(event.getEvent().compareTo(UserInteractionEventEnum.DRAG_COMPLETE) == 0) {
                Log.e(TAG, "Application Received DRAG_COMPLETE event");
            }
        }
    }

    class MapInteractionEventListener implements IMapInteractionEventListener {
        private String TAG = UserInteractionTest.MapInteractionEventListener.class.getSimpleName();
        private int whichMap;
        MapInteractionEventListener(int whichMap) {
            this.whichMap = whichMap;
        }
        @Override
        public void onEvent(MapUserInteractionEvent event) {
            Log.d(TAG, "onEvent " + event.getEvent().name() + " on " + event.getTarget().getName() + " at " + event.getCoordinate().getLatitude() + "/" +
                    event.getCoordinate().getLongitude());
            try {
                overlays[whichMap].addFeature(generateMilStdSymbol("TRUCK" + featureCount, new java.util.UUID(featureCount, featureCount),
                        event.getCoordinate().getLatitude(),event.getCoordinate().getLongitude() ), true);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            featureCount++;
        }
    }

    class FeatureEventListener implements IFeatureEventListener {
        int whichMap;

        FeatureEventListener(int whichMap) {
            this.whichMap = whichMap;
        }
        @Override
        public void onEvent(FeatureEvent event) {
            Log.d(TAG, "onEvent " + event.getEvent().toString());
            updateStatus("map" + whichMap + " FeatureEvent Fired " + event.getTarget().getDescription());
        }
    }

    private void stopMultiThreadTest(int whichMap) {
        if(multiThreadTestInProgress) {
            multiThreadTestInProgress = false;
            selectorThread.interrupt();
            deselectorThread.interrupt();
            removerThread.interrupt();
            adderThread.interrupt();
        }
    }
    private void startMultiThreadTest(int whichMap) {

        ICamera camera = maps[whichMap].getCamera();
        List<IFeature>  list = generateMilStdSymbolList(10, camera.getLatitude(), camera.getLongitude());

        try {
            removeListeners();
            overlays[whichMap].addFeatures(list, true);
            adderThread = new Thread(new Adder(whichMap, list));
            selectorThread = new Thread(new Selector(whichMap, list));
            deselectorThread = new Thread(new Deselector(whichMap, list));
            removerThread = new Thread(new Adder(whichMap, list));

            selectorThread.start();
            deselectorThread.start();
            removerThread.start();
            adderThread.start();
            multiThreadTestInProgress = true;

        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    abstract class BaseMultiThreadTest implements Runnable {
        protected int whichMap;
        protected List<IFeature> list;
        Random random = new Random();
        BaseMultiThreadTest(int whichMap, List<IFeature> list) {
            this.whichMap = whichMap;
            this.list = list;
        }

        abstract protected void action(int index);

        public void run() {
            while(!Thread.interrupted()) {
                try {
                    Thread.sleep(random.nextInt(10));
                    action(random.nextInt(list.size()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    class Selector extends BaseMultiThreadTest {
        Selector(int whichMap, List<IFeature> list) {
            super(whichMap, list);
        }
        @Override
        protected void action(int index) {
            maps[whichMap].selectFeature(list.get(index));
        }
    }

    class Deselector extends BaseMultiThreadTest {

        Deselector(int whichMap, List<IFeature> list) {
            super(whichMap, list);
        }
        @Override
        protected void action(int index) {
            maps[whichMap].deselectFeature(list.get(index));
        }
    }

    class Adder extends BaseMultiThreadTest {

        Adder(int whichMap, List<IFeature> list) {
            super(whichMap, list);
        }
        @Override
        protected void action(int index) {
            try {
                overlays[whichMap].addFeature(list.get(index), true);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        }
    }

    class Remover extends BaseMultiThreadTest {

        Remover(int whichMap, List<IFeature> list) {
            super(whichMap, list);
        }
        @Override
        protected void action(int index) {
            try {
                overlays[whichMap].removeFeature(list.get(index));
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        }
    }
}
