package mil.emp3.examples.capabilities.navItems;

import android.app.Activity;
import android.util.Log;

import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoPositionGroup;
import org.cmapi.primitives.IGeoStrokeStyle;

import mil.emp3.api.Path;
import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.events.MapFreehandEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.IFreehandEventListener;
import mil.emp3.api.listeners.IMapFreehandEventListener;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.examples.capabilities.common.Emp3TesterDialogBase;
import mil.emp3.examples.capabilities.common.ExecuteTest;
import mil.emp3.examples.capabilities.common.NavItemBase;
import mil.emp3.examples.capabilities.dialogs.utils.ErrorDialog;

/**
 * EMP supports API to enable users to draw on the map. This example shows how to use all the methods
 * associated with that capability.
 * The FreehandDraw.actOn method has the core of the example code, in the "Start" block.
 */
public class FreehandDraw extends NavItemBase {
    private static String TAG = FreehandDraw.class.getSimpleName();

    public FreehandDraw(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Start", "Stop"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        return null;
    }

    protected void test0() {

        try {
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

    @Override
    public boolean actOn(String userAction) {
        final int whichMap = ExecuteTest.getCurrentMap();

        try {
            if(Emp3TesterDialogBase.isEmp3TesterDialogBaseActive()) {
                updateStatus("Dismiss the dialog first");
                return false;
            }

            if (userAction.equals("Exit")) {
                for(int ii = 0; ii < ExecuteTest.MAX_MAPS; ii++) {
                    if(null != maps[ii]) {
                        try {
                            maps[ii].drawFreehandExit();
                        } catch(EMP_Exception e) {

                        }
                    }
                }
                testThread.interrupt();
            } else if(userAction.equals("ClearMap")) {
                for(int ii = 0; ii < ExecuteTest.MAX_MAPS; ii++) {
                    if(null != maps[ii]) {
                        try {
                            maps[ii].drawFreehandExit();
                        } catch(EMP_Exception e) {

                        }
                    }
                }
                clearMaps();
            } else if(userAction.equals("Start")) {
                try {
                    // Verify that map can be put into freehand draw mode
                    if((maps[whichMap].getMotionLockMode() == MapMotionLockEnum.UNLOCKED) &&
                            (maps[whichMap].getEditorMode() == EditorMode.INACTIVE)) {

                        // Create a style to be used for the drawn lines
                        IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
                        IGeoColor geoColor = new EmpGeoColor(1.0, 255, 255, 0);
                        strokeStyle.setStrokeColor(geoColor);
                        strokeStyle.setStrokeWidth(5);

                        // There are other flavors of this method in IMap. You can install a Map level
                        // listener and avoid providing a listener here.

                        // After this call is executed user should be able to draw on the map. Drawing should
                        // follow the stroke style set in the call. User can draw zero or more disconnected
                        // segments. It is the responsibility of the listener to preserve the drawing on the map
                        // as you will see in the implementation of the listener.

                        // Note, when in freehand draw mode, zoom, pan, rotate gestures on the screen are disabled.

                        maps[whichMap].drawFreehand(strokeStyle, new FreehandDrawEventListener(maps[whichMap]));
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ErrorDialog.showMessageWaitForConfirm(activity, "Draw on the screen, when done select Stop");
                            }
                        });
                        t.start();
                    }
                } catch(EMP_Exception e) {
                    ErrorDialog.showError(activity, e.getMessage());
                }
            } else if(userAction.equals("Stop")) {
                if(maps[whichMap].getEditorMode() == EditorMode.FREEHAND_MODE) {
                    maps[whichMap].drawFreehandExit();
                } else {
                    ErrorDialog.showError(activity, "Map is neither in EDIT nor in FREEHAND_MODE");
                }
            }
        } catch (Exception e) {
            updateStatus(TAG, e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void clearMapForTest() {
        String userAction = "ClearMap";
        actOn(userAction);
    }

    @Override
    protected boolean exitTest() {
        String userAction = "Exit";
        return (actOn(userAction));
    }

    /**
     * This is a sample Map Level Freehand Draw listener that is NOT used in this example.
     */
    class MapFreehandEventListener implements IMapFreehandEventListener {
        IMap theMap;

        MapFreehandEventListener(IMap map) {
            this.theMap = map;
        }

        @Override
        public void onEvent(MapFreehandEvent event) {
            switch(event.getEvent()) {

                case MAP_ENTERED_FREEHAND_DRAW_MODE:
                    updateStatus(TAG, "IMapFreehandEventListener: onEnterFreeHandDrawMode");
                    break;

                case MAP_FREEHAND_LINE_DRAW_START:
                    updateStatus(TAG, "IMapFreehandEventListener: onFreeHandLineDrawStart");
                    break;

                case MAP_FREEHAND_LINE_DRAW_UPDATE:
                    updateStatus(TAG, "IMapFreehandEventListener: onFreeHandLineDrawUpdate");
                    break;

                case MAP_FREEHAND_LINE_DRAW_END:
                    updateStatus(TAG, "IMapFreehandEventListener: onFreeHandLineDrawEnd");
                    Path path = new Path();
                    path.getPositions().clear();
                    path.getPositions().addAll(event.getPositionGroup().getPositions());
                    path.setStrokeStyle(event.getStyle());
                    IOverlay overlay = createOverlay(theMap);
                    try {
                        overlay.addFeature(path, true);
                    } catch (EMP_Exception e) {
                        ErrorDialog.showError(activity, e.getMessage());
                    }
                    break;

                case MAP_EXIT_FREEHAND_DRAW_MODE:
                    updateStatus(TAG, "IMapFreehandEventListener: onExitFreeHandDrawMode");
                    break;
                default:
                    updateStatus(TAG, "IMapFreehandEventListener: onDrawError");
                    Log.e(TAG, "Unsupported event received " + event.toString());
            }
        }
    }

    /**
     * Listener supplied to drawFreehand method.
     */
    class FreehandDrawEventListener implements IFreehandEventListener {
        IMap theMap;

        FreehandDrawEventListener(IMap map) {
            this.theMap = map;
        }
        @Override
        public void onEnterFreeHandDrawMode(IMap map) {
            updateStatus(TAG, "IFreehandEventListener: onEnterFreeHandDrawMode");
        }

        @Override
        public void onFreeHandLineDrawStart(IMap map, IGeoPositionGroup positionList) {
            updateStatus(TAG, "IFreehandEventListener: onFreeHandLineDrawStart");
        }

        @Override
        public void onFreeHandLineDrawUpdate(IMap map, IGeoPositionGroup positionList) {
            updateStatus(TAG, "IFreehandEventListener: onFreeHandLineDrawUpdate");
        }

        /**
         * When user lifts his/her finger from the screen this callback is invoked. EMP will remove the segment from the map,
         * so it is upto the application to add the segment to an appropriate layer.
         * @param map
         * @param style
         * @param positionList
         */
        @Override
        public void onFreeHandLineDrawEnd(IMap map, IGeoStrokeStyle style, IGeoPositionGroup positionList) {
            Path path = new Path();
            path.getPositions().clear();
            path.getPositions().addAll(positionList.getPositions());
            path.setStrokeStyle(style);
            IOverlay overlay = createOverlay(theMap);
            try {
                overlay.addFeature(path, true);
            } catch (EMP_Exception e) {
                ErrorDialog.showError(activity, e.getMessage());
            }
        }

        @Override
        public void onExitFreeHandDrawMode(IMap map) {
            updateStatus(TAG, "IFreehandEventListener: onExitFreeHandDrawMode");
        }

        @Override
        public void onDrawError(IMap map, String errorMessage) {
            updateStatus(TAG, "IFreehandEventListener: onDrawError");
        }
    }
}
