package mil.emp3.examples.utils;


import android.app.Activity;

import org.cmapi.primitives.IGeoPosition;

import java.util.List;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.examples.common.TestBase;

/**
 * This code is mostly copied from test-basic. It has been simplified to avoid the dialog and simply update geo position by set amount
 * for 10 seconds.
 */
public class EditFeature extends TestBase {
    private static String TAG = EditFeature.class.getSimpleName();
    private boolean enableUpdaterThread = true;
    public EditFeature(Activity activity, IMap map1, IMap map2, boolean doSetup) {
        super(activity, map1, map2, TAG, doSetup);
    }

    public EditFeature(Activity activity, IMap map1, IMap map2, boolean doSetup, boolean enableUpdaterThread) {
        super(activity, map1, map2, TAG, doSetup);
        this.enableUpdaterThread = enableUpdaterThread;
    }

    public void startEditFeature(IFeature feature, IMap map) {
        try {
            map.editFeature(feature, new FeatureEditorListener(feature));
        } catch(EMP_Exception e) {
            updateStatus(e.getMessage());
            e.printStackTrace();
        }
    }

    class PositionUpdater implements Runnable {
        IFeature feature;
        PositionUpdater(IFeature feature) {
            this.feature = feature;
        }

        @Override
        public void run() {
            int updateCount = 0;
            while(!Thread.currentThread().interrupted()) {
                try {
                    Thread.sleep(1000);
                    List<IGeoPosition> posList = feature.getPositions();
                    if ((null != posList) && (posList.size() == 1)) {
                        IGeoPosition pos = posList.get(0);
                        if(updateCount % 2 == 0) {
                            pos.setLatitude(pos.getLatitude() + .1);
                            pos.setLongitude(pos.getLongitude() + .1);
                        } else {
                            pos.setLatitude(pos.getLatitude() - .1);
                            pos.setLongitude(pos.getLongitude() - .1);
                        }

                        updateCount++;
                        feature.apply();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }

        }
    }
    public class FeatureEditorListener implements IEditEventListener {

        IFeature feature;
        Thread updaterThread;

        public FeatureEditorListener(IFeature feature) {
            this.feature = feature;
        }

        @Override
        public void onEditStart(IMap map) {
            updateStatus(TAG, "Edit Start.");
            if(enableUpdaterThread) {
                updaterThread = new Thread(new PositionUpdater(feature));
                updaterThread.start();
            }
        }

        @Override
        public void onEditUpdate(IMap map, IFeature oFeature, List<IEditUpdateData> updateList) {
            updateStatus(TAG, "Edit Update.");
//            if (MainActivity.this.oSelectedDialogHash.containsKey(oFeature.getGeoId())) {
//                FeatureLocationDialog oDialog = MainActivity.this.oSelectedDialogHash.get(oFeature.getGeoId());
//                oDialog.updateDialog();
//            }
        }

        @Override
        public void onEditComplete(IMap map, IFeature feature) {
            updateStatus(TAG, "Edit Complete.");
            if(null != updaterThread) {
                updaterThread.interrupt();
            }
//            if (MainActivity.this.oSelectedDialogHash.containsKey(feature.getGeoId())) {
//                FeatureLocationDialog oDialog = MainActivity.this.oSelectedDialogHash.get(feature.getGeoId());
//                oDialog.updateDialog();
//            }
        }

        @Override
        public void onEditCancel(IMap map, IFeature originalFeature) {
            updateStatus(TAG, "Edit Canceled.");
            if(null != updaterThread) {
                updaterThread.interrupt();
            }
//            if (MainActivity.this.oSelectedDialogHash.containsKey(originalFeature.getGeoId())) {
//                FeatureLocationDialog oDialog = MainActivity.this.oSelectedDialogHash.get(originalFeature.getGeoId());
//                oDialog.updateDialog();
//            }
        }

        @Override
        public void onEditError(IMap map, String errorMessage) {
            updateStatus(TAG, "Edit Error.");
            if(null != updaterThread) {
                updaterThread.interrupt();
            }
        }
    }
}
