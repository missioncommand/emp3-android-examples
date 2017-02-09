package mil.emp3.examples.capabilities.navItems;

import android.app.Activity;
import android.util.Log;
import java.util.List;

import mil.emp3.api.Circle;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Polygon;
import mil.emp3.api.Rectangle;
import mil.emp3.api.Square;
import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.events.FeatureDrawEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IFeatureDrawEventListener;
import mil.emp3.examples.capabilities.common.Emp3TesterDialogBase;
import mil.emp3.examples.capabilities.common.ExecuteTest;
import mil.emp3.examples.capabilities.common.NavItemBase;
import mil.emp3.examples.capabilities.containers.dialogs.milstdunits.SymbolPropertiesDialog;
import mil.emp3.examples.capabilities.dialogs.utils.ErrorDialog;

/**
 * EMP supports API to enable users to draw features on the map. This example shows how to use all the methods
 * associated with that capability.
 * The FreehandDraw.actOn method has the core of the example code, in the "Start" block.
 */
public class DrawFeature extends NavItemBase {
    private static String TAG = DrawFeature.class.getSimpleName();

    public DrawFeature(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Draw Circle", "Draw Polygon", "Draw Mil Symbol", "Done"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        String[] actions = {"Cancel" };
        return actions;
    }
    protected void test0() {

        try {
            SymbolPropertiesDialog.loadSymbolTables();
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
                            maps[ii].cancelDraw();
                        } catch(EMP_Exception e) {

                        }
                    }
                }
                clearMaps();
                testThread.interrupt();
            } else if(userAction.equals("ClearMap")) {
                for(int ii = 0; ii < ExecuteTest.MAX_MAPS; ii++) {
                    if(null != maps[ii]) {
                        try {
                            maps[ii].cancelDraw();
                        } catch(EMP_Exception e) {

                        }
                    }
                }
                clearMaps();
            } else if(userAction.contains("Draw Circle")) {
                if((maps[whichMap].getMotionLockMode() == MapMotionLockEnum.UNLOCKED) &&
                        (maps[whichMap].getEditorMode() == EditorMode.INACTIVE)) {
                    Circle circle = new Circle();

                    // It is also possible to add a listener at Map level instead of providing one as it is done
                    // below. Example of such a listener is provided at the end of this file.
                    // Please explore all IMap methods related to drawFeature.

                    // Note that once draw is complete, feature will be removed from the map, it is applications
                    // responsibility to add the feature to an appropriate overlay.

                    // Note, when in draw mode, zoom, pan, rotate gestures on the screen are disabled.
                    maps[whichMap].drawFeature(circle, new DrawEventListener());
                    showMessage("Use the control point to change the radius of the circle, then either select Done or Cancel");
                } else {
                    showMessage("Draw Circle not allowed in current state");
                }
            } else if(userAction.contains("Draw Polygon")) {
                if((maps[whichMap].getMotionLockMode() == MapMotionLockEnum.UNLOCKED) &&
                        (maps[whichMap].getEditorMode() == EditorMode.INACTIVE)) {
                    Polygon polygon = new Polygon();
                    maps[whichMap].drawFeature(polygon, new DrawEventListener());
                    showMessage("Tap the screen for polygon vertices, then either select Done or Cancel");
                } else {
                    showMessage("Draw Polygon not allowed in current state");
                }
            }  else if(userAction.contains("Draw Mil Symbol")) {
                if((maps[whichMap].getMotionLockMode() == MapMotionLockEnum.UNLOCKED) &&
                        (maps[whichMap].getEditorMode() == EditorMode.INACTIVE)) {
                    MilStdSymbol milStdSymbol = new MilStdSymbol();
                    milStdSymbol.setSymbolCode("SFG*EWTM----***");
                    maps[whichMap].drawFeature(milStdSymbol, new DrawEventListener());
                    showMessage("Select and drag the icon to desired position, you may need to zoom in, then either select Done or Cancel");
                } else {
                    showMessage("Draw Mil Symbol not allowed in current state");
                }
            } else if(userAction.equals("Done")) {
                if(maps[whichMap].getEditorMode() == EditorMode.DRAW_MODE) {
                    maps[whichMap].completeDraw();
                } else {
                    showMessage("Map is neither in EDIT nor in DRAW mode");
                }
            }  else if(userAction.equals("Cancel")) {
                if(maps[whichMap].getEditorMode() == EditorMode.DRAW_MODE) {
                    maps[whichMap].cancelDraw();
                } else {
                    showMessage("Map is neither in EDIT nor in DRAW mode");
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
        return(actOn(userAction));
    }

    private void showMessage(final String message) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ErrorDialog.showMessageWaitForConfirm(activity, message);
            }
        });
        t.start();
    }
    /**
     * Process the onDrawUpdate callback on the DrawEventListener.
     * @param listener
     * @param oFeature
     * @param updateData
     */
    private void processEditUpdate(String listener, IFeature oFeature, IEditUpdateData updateData) {
        int[] aIndexes;
        String temp;
        switch(updateData.getUpdateType()) {
            case COORDINATE_ADDED:
            case COORDINATE_MOVED:
            case COORDINATE_DELETED:
                aIndexes = updateData.getCoordinateIndexes();
                temp = "";
                for (int index = 0; index < aIndexes.length; index++) {
                    if (temp.length() > 0) {
                        temp += ",";
                    }
                    temp += aIndexes[index];
                }
                Log.i(listener, "   Draw Update " + updateData.getUpdateType().name() + " indexes:{" + temp + "}");
                updateStatus(listener, " Draw Update " + updateData.getUpdateType().name() + " indexes:{" + temp + "}");
                break;
            case MILSTD_MODIFIER_UPDATED:
                Log.i(listener, "   Draw Update " + updateData.getUpdateType().name() + " modifier: " + updateData.getChangedModifier().name() + " {" + ((MilStdSymbol) oFeature).getStringModifier(updateData.getChangedModifier()) + "}");
                updateStatus(listener, " Draw Update " + updateData.getUpdateType().name() + " modifier: " + updateData.getChangedModifier().name() + " {" + ((MilStdSymbol) oFeature).getStringModifier(updateData.getChangedModifier()) + "}" );
                break;
            case ACM_ATTRIBUTE_UPDATED:
                Log.i(listener, "   Draw Update " + updateData.getUpdateType().name() + " ACM attribute:{" + updateData.getChangedModifier().name() + "}");
                updateStatus(listener, " Draw Update " + updateData.getUpdateType().name() + " ACM attribute:{" + updateData.getChangedModifier().name() + "}");
                break;
            case FEATURE_PROPERTY_UPDATED:
                switch(updateData.getChangedProperty()) {
                    case HEIGHT_PROPERTY_CHANGED:
                        if (oFeature instanceof Rectangle) {
                            Log.i(listener, "   Draw Update " + updateData.getChangedProperty().toString() + " " + ((Rectangle) oFeature).getHeight());
                            updateStatus(listener, "   Draw Update " + updateData.getChangedProperty().toString() + " " + ((Rectangle) oFeature).getHeight());
                        }
                        break;

                    case WIDTH_PROPERTY_CHANGED:
                        if (oFeature instanceof Rectangle) {
                            Log.i(listener, "   Draw Update " + updateData.getChangedProperty().toString() + " " + ((Rectangle) oFeature).getWidth());
                            updateStatus(listener, "   Draw Update " + updateData.getChangedProperty().toString() + " " + ((Rectangle) oFeature).getWidth());
                        } else if(oFeature instanceof Square) {
                            Log.i(listener, "   Draw Update " + updateData.getChangedProperty().toString() + " " + ((Square) oFeature).getWidth());
                            updateStatus(listener, "   Draw Update " + updateData.getChangedProperty().toString() + " " + ((Square) oFeature).getWidth());
                        }
                        break;

                    case AZIMUTH_PROPERTY_CHANGED:
                        Log.i(listener, "   Draw Update " + updateData.getChangedProperty().toString() + " " + oFeature.getAzimuth());
                        updateStatus(listener, "   Draw Update " + updateData.getChangedProperty().toString() + " " + oFeature.getAzimuth());
                        break;
                }
                break;
            case POSITION_UPDATED:
                if(oFeature instanceof Rectangle) {
                    Log.i(listener, "   Draw Update " + updateData.getUpdateType().name() + " " + ((Rectangle) oFeature).getPosition().getLatitude() + "/"
                            + ((Rectangle) oFeature).getPosition().getLongitude());
                    updateStatus(listener, "   Draw Update " + updateData.getUpdateType().name() + " " + ((Rectangle) oFeature).getPosition().getLatitude() + "/"
                            + ((Rectangle) oFeature).getPosition().getLongitude());
                } else if(oFeature instanceof Square) {
                    Log.i(listener, "   Draw Update " + updateData.getUpdateType().name() + " " + ((Square) oFeature).getPosition().getLatitude() + "/"
                            + ((Square) oFeature).getPosition().getLongitude());
                    updateStatus(listener, "   Draw Update " + updateData.getUpdateType().name() + " " + ((Square) oFeature).getPosition().getLatitude() + "/"
                            + ((Square) oFeature).getPosition().getLongitude());
                }
                break;
        }
    }

    /**
     * Listener supplied with drawFeature method.
     */
    public class DrawEventListener implements IDrawEventListener {

        @Override
        public void onDrawStart(IMap map) {
            Log.d(TAG, "Draw Start.");
            updateStatus(TAG, "IDrawEventListener: Draw Start" );
        }

        @Override
        public void onDrawUpdate(IMap map, IFeature oFeature, List<IEditUpdateData> updateList) {
            Log.d(TAG, "Draw Update.");

            for (IEditUpdateData updateData: updateList) {
                processEditUpdate("IDrawEventListener", oFeature, updateData);
            }
        }

        // Note that once draw is complete, feature will be removed from the map, it is applications
        // responsibility to add the feature to an appropriate overlay.

        @Override
        public void onDrawComplete(IMap map, IFeature feature) {
            Log.d(TAG, "Draw Complete. " + feature.getClass().getSimpleName() + " pos count " + feature.getPositions().size());
            updateStatus(TAG, "IDrawEventListener: Draw Complete." + feature.getClass().getSimpleName());

            for(int ii = 0; ii < feature.getPositions().size(); ii++ ) {
                Log.d(TAG, "pos " + ii + " lat/lon " + feature.getPositions().get(ii).getLatitude() + "/" + feature.getPositions().get(ii).getLongitude());
            }

            try {
                IOverlay overlay = createOverlay(map);
                overlay.addFeature(feature, true);
                Log.d(TAG, "add feature");
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Draw action was cancelled by the user, so no action required.
         * @param map
         * @param originalFeature
         */
        @Override
        public void onDrawCancel(IMap map, IFeature originalFeature) {
            Log.d(TAG, "Draw Cancelled.");
            updateStatus(TAG, "IDrawEventListener: Draw Cancelled");
        }

        @Override
        public void onDrawError(IMap map, String errorMessage) {
            Log.d(TAG, "Draw Error. ");
            updateStatus(TAG, "IDrawEventListener: Draw Error");
        }
    }

    /**
     * Instead of adding a drawEventListener to the drawFeature method you can also add the following
     * event listener at map level. This class is not used in this example.
     */
    public class FeatureDrawEventListener implements IFeatureDrawEventListener {
        @Override
        public void onEvent(FeatureDrawEvent event) {
            if(null != event.getUpdateData()) {
                for (IEditUpdateData updateData : event.getUpdateData()) {
                    processEditUpdate("IFeatureDrawEventListener", event.getTarget(), updateData);
                }
            }
        }
    }
}

