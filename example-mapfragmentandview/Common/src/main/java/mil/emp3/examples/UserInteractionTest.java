package mil.emp3.examples;

import android.app.Activity;
import android.util.Log;

import java.util.List;

import mil.emp3.api.abstracts.Map;
import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.UserInteractionEventEnum;
import mil.emp3.api.events.FeatureUserInteractionEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.IFeatureInteractionEventListener;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.examples.common.TestBase;
import mil.emp3.examples.utils.EditFeature;
import mil.emp3.examples.utils.FreehandDraw;

public class UserInteractionTest  extends TestBase implements Runnable {

    public UserInteractionTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    private IOverlay[] overlays = new IOverlay[2];
    Thread testThread;
    DrawPolygonTest[] drawPolygonTest = new DrawPolygonTest[2];
    FreehandDraw[] freehandDraw = new FreehandDraw[2];

    private int featureCount = 0;
    FeatureInteractionEventListener[] fiel = new FeatureInteractionEventListener[2];
    EventListenerHandle[] fielHandle = new EventListenerHandle[2] ;
    MapInteractionEventListener[] miel = new MapInteractionEventListener[2];
    EventListenerHandle[] mielHandle = new EventListenerHandle[2];


    @Override
    public void run() {

        overlays[0] = o1;
        overlays[1] = o2;

        fiel[0] = new FeatureInteractionEventListener(maps[0]);
        fiel[1] = new FeatureInteractionEventListener(maps[1]);

        miel[0] = new MapInteractionEventListener(maps[0]);
        miel[1] = new MapInteractionEventListener(maps[1]);


        try {
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
        if(null == m2) {
            String[] actions = { "Lock-1", "SmartLock-1", "Unlock-1", "LockStatus-1",
                "Exit"};
            return actions;
        } else {
            String[] actions = { "Lock-1", "SmartLock-1", "Unlock-1",
                "Lock-2", "SmartLock-2", "Unlock-2" };
            return actions;
        }
    }

    @Override
    public String[] getMoreActions() {
        if(null == m2) {
            String[] actions = {"+MapUserInt-1", "-MapUserInt-1", "+FeatUserInt-1", "-FeatUserInt-1",
                    "StartDraw-1", "CompleteDraw-1", "CancelDraw-1",
                    "CompleteEdit-1", "CancelEdit-1",
                    "StartFreehand-1", "ExitFreehand-1",
                    "AddFeature-1" };
            return actions;
        } else {
            String[] actions = { "LockStatus-1",
                    "+MapUserInt-1", "-MapUserInt-1", "+FeatUserInt-1", "-FeatUserInt-1",
                    "StartDraw-1", "CompleteDraw-1", "CancelDraw-1",
                    "CompleteEdit-1", "CancelEdit-1",
                    "StartFreehand-1", "ExitFreehand-1",
                    "AddFeature-1",
                    "LockStatus-2",
                    "+MapUserInt-2", "-MapUserInt-2", "+FeatUserInt-2", "-FeatUserInt-2",
                    "StartDraw-2", "CompleteDraw-2", "CancelDraw-2",
                    "CompleteEdit-2", "CancelEdit-2",
                    "StartFreehand-2", "ExitFreehand-2",
                    "AddFeature-2",
                    "Exit"
            };
            return actions;
        }
    }

    @Override
    public void actOn(String userAction) {

        int whichMap = userAction.contains("-1") ? 0 : 1;
        try {

            if (userAction.equals("Exit")) {
                prepareForExit(m1);
                prepareForExit(m2);
                testThread.interrupt();
            }  else if (userAction.contains("SmartLock-")) {
                maps[whichMap].setMotionLockMode(MapMotionLockEnum.SMART_LOCK);
            } else if (userAction.contains("Lock-")) {
                maps[whichMap].setMotionLockMode(MapMotionLockEnum.LOCKED);
            } else if (userAction.contains("Unlock-")) {
                maps[whichMap].setMotionLockMode(MapMotionLockEnum.UNLOCKED);
            } else if (userAction.contains("StartDraw-")) {
                if(null == drawPolygonTest[whichMap]) {
                    drawPolygonTest[whichMap] = new DrawPolygonTest(activity, m1, m2, false);
                    drawPolygonTest[whichMap].setAddPolygonOnComplete(whichMap, false);
                    try {
                        drawPolygonTest[whichMap].drawPolygon(whichMap, false);
                    } catch (EMP_Exception e) {
                        updateStatus(whichMap + " " + userAction + " Failed " + e.getMessage());
                        drawPolygonTest[whichMap] = null;
                    }
                } else {
                    updateStatus("map" + String.valueOf(whichMap) + " " + userAction + " is already in progress");
                }
            } else if (userAction.contains("CancelDraw-")) {
                if(null != drawPolygonTest[whichMap]) {
                    try {
                        maps[whichMap].cancelDraw();
                    } catch(EMP_Exception e) {
                        updateStatus("map" + String.valueOf(whichMap) + " " + userAction + " FAILED because " + e.getMessage());
                    }
                    drawPolygonTest[whichMap] = null;
                } else {
                    updateStatus("map" + String.valueOf(whichMap) + " " + userAction + " Failed because StartDraw is NOT in progress");
                }
            } else if (userAction.contains("CompleteDraw-")) {
                if(null != drawPolygonTest[whichMap]) {
                    try {
                        maps[whichMap].completeDraw();
                    } catch(EMP_Exception e) {
                        updateStatus("map" + String.valueOf(whichMap) + " " + userAction + " FAILED because " + e.getMessage());
                    }
                    drawPolygonTest[whichMap] = null;
                } else {
                    updateStatus("map" + String.valueOf(whichMap) + " " + userAction + " Failed because StartDraw is NOT in progress");
                }
            } else if (userAction.contains("AddFeature-")) {
                overlays[whichMap].addFeature(generateMilStdSymbol("TRUCK" + featureCount++, maps[whichMap].getCamera()), true);
            } else if (userAction.contains("+MapUserInt-")) {
                if(null == mielHandle[whichMap]) {
                    mielHandle[whichMap] = maps[whichMap].addMapInteractionEventListener(miel[whichMap]);
                } else {
                    updateStatus("map" + String.valueOf(whichMap) + " " + "MapUserInt was already added");
                }
            } else if (userAction.contains("-MapUserInt-")) {
                if(null != mielHandle[whichMap]) {
                    maps[whichMap].removeEventListener(mielHandle[whichMap]);
                    mielHandle[whichMap] = null;
                } else {
                    updateStatus("map" + String.valueOf(whichMap) + " " + "MapUserInt was already removed");
                }
            } else if (userAction.contains("+FeatUserInt-")) {
                if(null == fielHandle[whichMap]) {
                    fielHandle[whichMap] = maps[whichMap].addFeatureInteractionEventListener(fiel[whichMap]);
                } else {
                    updateStatus("map" + String.valueOf(whichMap) + " " + "FeatUserInt was already added");
                }
            } else if (userAction.contains("-FeatUserInt-")) {
                if(null != fielHandle[whichMap]) {
                    maps[whichMap].removeEventListener(fielHandle[whichMap]);
                    fielHandle[whichMap] = null;
                } else {
                    updateStatus("map" + String.valueOf(whichMap) + " " + "FeatUserInt was already removed");
                }
            } else if (userAction.contains("StartFreehand-")) {
                if(null == freehandDraw[whichMap]) {
                    freehandDraw[whichMap] = new FreehandDraw(activity, m1, m2, false);
                    try {
                        freehandDraw[whichMap].startFreehandDraw(maps[whichMap]);
                    } catch (EMP_Exception e) {
                        updateStatus("map" + String.valueOf(whichMap) + " " + "StartFreehand Failed " + e.getMessage());
                        freehandDraw[whichMap] = null;
                    }
                } else {
                    updateStatus("map" + String.valueOf(whichMap) + " " + userAction + " is already in progress");
                }
            } else if (userAction.contains("ExitFreehand-")) {
                if(null != freehandDraw[whichMap]) {
                    try {
                        maps[whichMap].drawFreehandExit();
                    } catch(EMP_Exception e) {
                        updateStatus("map" + String.valueOf(whichMap) + " " + userAction + " FAILED because " + e.getMessage());
                    }
                    freehandDraw[whichMap] = null;
                } else {
                    updateStatus("map" + String.valueOf(whichMap) + " " + userAction + " Failed because StartFreehand is NOT in progress");
                }
            } else if(userAction.contains("LockStatus-")) {
              updateStatus("map" + String.valueOf(whichMap) + " " + "MapMotionLock " + maps[whichMap].getMotionLockMode() + " EditorMode " + maps[whichMap].getEditorMode());
            } else if(userAction.contains("CompleteEdit-")) {
                maps[whichMap].completeEdit();
            } else if(userAction.contains("CancelEdit-")) {
                maps[whichMap].cancelEdit();
            } else {
                updateStatus("map" + String.valueOf(whichMap) + " " + userAction + " NOT supported");
            }
        } catch (EMP_Exception e) {
            updateStatus("map" + String.valueOf(whichMap) + " " + userAction + " FAILED because " + e.getMessage());
        }
    }

    private void prepareForExit(IMap map) {
        if(null == map) {
            return;
        }
        try {
            switch(map.getEditorMode()) {
                case EDIT_MODE:
                    map.cancelEdit();
                    break;
                case DRAW_MODE:
                    map.cancelDraw();
                    break;
                case FREEHAND_MODE:
                    map.drawFreehandExit();
            }

            if(map.getMotionLockMode() != MapMotionLockEnum.UNLOCKED) {
                map.setMotionLockMode(MapMotionLockEnum.UNLOCKED);
            }
        } catch(EMP_Exception e) {
            e.printStackTrace();
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
        private String TAG = FeatureInteractionEventListener.class.getSimpleName();
        private IMap map;

        FeatureInteractionEventListener(IMap map) {
            this.map = map;
        }
        @Override
        public void onEvent(FeatureUserInteractionEvent event) {

            try {
                Log.d(TAG, "onEvent " + event.getEvent().name());
                for (IFeature f : event.getTarget()) {
                    Log.d(TAG, "onEvent " + event.getEvent().name() + " on " + f.getDescription());
                    updateStatus(TAG, "onEvent " + event.getEvent().name() + " on " + f.getDescription());
                }

                if (event.getEvent().compareTo(UserInteractionEventEnum.DOUBLE_CLICKED) == 0) {
                    Log.d(TAG, "Attempt to switch to editFeature");
                    if ((event.getTarget().size() == 1) && (event.getTarget().get(0).getFeatureType().compareTo(FeatureTypeEnum.GEO_MIL_SYMBOL) == 0)) {
                        if (map.getEditorMode() == EditorMode.INACTIVE) {
                            EditFeature editFeature = new EditFeature(activity, m1, m2, false);
                            editFeature.startEditFeature(event.getTarget().get(0), map);
                        }
                    }
                } else if(event.getEvent().compareTo(UserInteractionEventEnum.DRAG) == 0) {
                    Log.e(TAG, "Application Received DRAG event");
                } else if(event.getEvent().compareTo(UserInteractionEventEnum.DRAG_COMPLETE) == 0) {
                    Log.e(TAG, "Application Received DRAG_COMPLETE event");
                }
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        }
    }

    class MapInteractionEventListener implements IMapInteractionEventListener {
        private String TAG = MapInteractionEventListener.class.getSimpleName();
        private IMap map;
        MapInteractionEventListener(IMap map) {
            this.map = map;
        }
        @Override
        public void onEvent(MapUserInteractionEvent event) {
            Log.d(TAG, "onEvent " + event.getEvent().name() + " on " + event.getTarget().getName() + " at " + event.getCoordinate().getLatitude() + "/" +
                event.getCoordinate().getLongitude());
            updateStatus(TAG, "onEvent " + event.getEvent().name() + " on " + event.getTarget().getName() + " at " + event.getCoordinate().getLatitude() + "/" +
                    event.getCoordinate().getLongitude());
        }
    }
}
