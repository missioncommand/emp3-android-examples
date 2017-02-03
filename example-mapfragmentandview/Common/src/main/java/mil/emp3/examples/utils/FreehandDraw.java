package mil.emp3.examples.utils;

import android.app.Activity;
import android.util.Log;

import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoPositionGroup;
import org.cmapi.primitives.IGeoStrokeStyle;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IFreehandEventListener;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.examples.common.TestBase;

/**
 * Much of this code is copied from test-basic, MainActivity.
 */
public class FreehandDraw extends TestBase {

    private static String TAG = FreehandDraw.class.getSimpleName();

    public FreehandDraw(Activity activity, IMap map1, IMap map2, boolean doSetup) {
        super(activity, map1, map2, TAG, doSetup);
    }

    public void startFreehandDraw(IMap map) throws EMP_Exception {

        IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
        IGeoColor geoColor = new EmpGeoColor(1.0, 255, 255, 0);

        strokeStyle.setStrokeColor(geoColor);
        strokeStyle.setStrokeWidth(5);

        map.drawFreehand(strokeStyle, new IFreehandEventListener() {

            @Override
            public void onEnterFreeHandDrawMode(IMap map) {
                updateStatus(TAG, "Enter Freehand Draw Mode.");
            }

            @Override
            public void onFreeHandLineDrawStart(IMap map, IGeoPositionGroup positionList) {
                Log.d(TAG, "Freehand Draw Start.");
                int iCurrentSize = positionList.getPositions().size();
                if (iCurrentSize > 0) {
                    String lastPoint = String.format("[%d] Lat: %2$8.5f Lon: %3$8.5f\n", iCurrentSize, positionList.getPositions().get(iCurrentSize - 1).getLatitude(),
                                positionList.getPositions().get(iCurrentSize - 1).getLongitude());
                    updateStatus(TAG, lastPoint);
                } else {
                    updateStatus(TAG, "Freehand Draw Start. No pos");
                }
            }

            @Override
            public void onFreeHandLineDrawUpdate(IMap map, IGeoPositionGroup positionList) {
                Log.d(TAG, "Freehand Draw Update.");
                int iCurrentSize = positionList.getPositions().size();
                if (iCurrentSize > 0) {
                    String lastPoint = String.format("[%d] Lat: %2$8.5f Lon: %3$8.5f\n", iCurrentSize, positionList.getPositions().get(iCurrentSize - 1).getLatitude(),
                                positionList.getPositions().get(iCurrentSize - 1).getLongitude());
                    updateStatus(TAG, lastPoint);
                } else {
                    updateStatus(TAG, "Freehand Draw Update. No pos");
                }
            }

            @Override
            public void onFreeHandLineDrawEnd(IMap map, IGeoStrokeStyle style, IGeoPositionGroup positionList) {
                Log.d(TAG, "Freehand Draw Complete.");
                int iCurrentSize = positionList.getPositions().size();
                if (iCurrentSize > 0) {
                    String lastPoint = String.format("[%d] Lat: %2$8.5f Lon: %3$8.5f\n", iCurrentSize, positionList.getPositions().get(iCurrentSize - 1).getLatitude(),
                                positionList.getPositions().get(iCurrentSize - 1).getLongitude());
                    updateStatus(TAG, lastPoint);
                } else {
                    updateStatus(TAG, "Freehand Draw Complete. No pos");
                }
            }

            @Override
            public void onExitFreeHandDrawMode(IMap map) {
                updateStatus(TAG, "Exit Freehand Draw mode.");
            }

            @Override
            public void onDrawError(IMap map, String errorMessage) {
                updateStatus(TAG, "onDrawError " + errorMessage);
            }
        });

    }
}
