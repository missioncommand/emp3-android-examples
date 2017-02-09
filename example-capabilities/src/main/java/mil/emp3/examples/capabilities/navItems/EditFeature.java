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
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.examples.capabilities.common.Emp3TesterDialogBase;
import mil.emp3.examples.capabilities.common.ExecuteTest;
import mil.emp3.examples.capabilities.common.NavItemBase;

import mil.emp3.examples.capabilities.dialogs.utils.ErrorDialog;
import mil.emp3.examples.capabilities.utils.ExampleBuilder;

/**
 * EMP supports API to enable users to edit features on the map. This example shows how to use all the methods
 * associated with that capability.
 * The FreehandDraw.actOn method has the core of the example code.
 */

public class EditFeature extends NavItemBase {
    private static String TAG = EditFeature.class.getSimpleName();

    // User can launch up to two maps, so all the members are setup to allow for two maps.
    // It is possible to share overlays and features across maps but this example doesn't do that.

    private IOverlay overlay_a[] = new IOverlay[ExecuteTest.MAX_MAPS];
    private IOverlay overlay_b[]= new IOverlay[ExecuteTest.MAX_MAPS];
    private IOverlay overlay_a_child[]= new IOverlay[ExecuteTest.MAX_MAPS];

    private Circle circle[]= new Circle[ExecuteTest.MAX_MAPS];
    private Polygon polygon[] = new Polygon[ExecuteTest.MAX_MAPS];
    private MilStdSymbol milStdSymbol[] = new MilStdSymbol[ExecuteTest.MAX_MAPS];

    public EditFeature(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Edit Circle", "Edit Polygon", "Edit Mil Symbol", "Done"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        String[] actions = { "Cancel" };
        return actions;
    }

    protected void test0() {

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
                            maps[ii].cancelEdit();
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
                            maps[ii].cancelEdit();
                        } catch(EMP_Exception e) {

                        }
                    }
                }
                clearMaps();
            } else if(userAction.contains("Edit Circle")) {
                if((maps[whichMap].getMotionLockMode() == MapMotionLockEnum.UNLOCKED) &&
                        (maps[whichMap].getEditorMode() == EditorMode.INACTIVE)) {
                    createFeaturesForEdit(whichMap);
                    maps[whichMap].editFeature(circle[whichMap], new EditEventListener());
                    showMessage("Use the control points to change the radius of the circle, then either select Done or Cancel");
                } else {
                    showMessage("Edit Circle not allowed in current state");
                }
            } else if(userAction.contains("Edit Polygon")) {
                if((maps[whichMap].getMotionLockMode() == MapMotionLockEnum.UNLOCKED) &&
                        (maps[whichMap].getEditorMode() == EditorMode.INACTIVE)) {
                    createFeaturesForEdit(whichMap);
                    maps[whichMap].editFeature(polygon[whichMap], new EditEventListener());
                    showMessage("Add, remove or Drag control points, then either select Done or Cancel");
                } else {
                    showMessage("Edit Polygon not allowed in current state");
                }
            } else if(userAction.contains("Edit Mil Symbol")) {
                if((maps[whichMap].getMotionLockMode() == MapMotionLockEnum.UNLOCKED) &&
                        (maps[whichMap].getEditorMode() == EditorMode.INACTIVE)) {
                    createFeaturesForEdit(whichMap);
                    maps[whichMap].editFeature(milStdSymbol[whichMap], new EditEventListener());
                    showMessage("Select and drag the icon to desired position, you may need to zoom in, then either select Done or Cancel");
                } else {
                    showMessage("Edit Mil Symbol not allowed in current state");
                }
            } else if(userAction.equals("Done")) {
                if(maps[whichMap].getEditorMode() == EditorMode.EDIT_MODE) {
                    maps[whichMap].completeEdit();
                } else {
                    showMessage("Map is neither in EDIT nor in DRAW mode");
                }
            } else if(userAction.equals("Cancel")) {
                if(maps[whichMap].getEditorMode() == EditorMode.EDIT_MODE) {
                    maps[whichMap].cancelEdit();
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
     * Create features for editing if they are already not there.
     * @param whichMap
     */
    private void createFeaturesForEdit(int whichMap) {

        List<IFeature> features = maps[whichMap].getAllFeatures();
        if((null == features) || (3 != features.size())) {
            clearMap(maps[whichMap]);
            ExampleBuilder.buildOverlayHierarchy(maps, whichMap, overlay_a, overlay_b, overlay_a_child);
            ExampleBuilder.setupCamera(maps[whichMap]);
            ExampleBuilder.createAndAddFeatures(whichMap, overlay_a, overlay_b, overlay_a_child, circle, polygon, milStdSymbol);
        }
    }

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
     * Event listener for processing EMP editor callbacks. This listener simply puts out Log mesages. Applications
     * will probably choose to do something more.
     */
    public class EditEventListener implements IEditEventListener {

        @Override
        public void onEditStart(IMap map) {
            updateStatus(TAG, "IEditEventListener: Edit Start" );
        }

        @Override
        public void onEditUpdate(IMap map, IFeature oFeature, List<IEditUpdateData> updateList) {
            Log.d(TAG, "Edit Update.");

            for (IEditUpdateData updateData: updateList) {
                processEditUpdate("IEditEventListener", oFeature, updateData);
            }
        }

        @Override
        public void onEditComplete(IMap map, IFeature feature) {
            updateStatus(TAG, "IEditEventListener: Edit Complete.");
        }

        @Override
        public void onEditCancel(IMap map, IFeature originalFeature) {
            updateStatus(TAG, "IEditEventListener: Edit Canceled.");
        }

        @Override
        public void onEditError(IMap map, String errorMessage) {
            updateStatus(TAG, "IEditEventListener: Edit Error.");
        }
    }
}
