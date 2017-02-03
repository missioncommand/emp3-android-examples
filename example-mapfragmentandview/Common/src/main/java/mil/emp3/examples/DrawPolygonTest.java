package mil.emp3.examples;

import android.app.Activity;
import android.util.Log;

import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.LookAt;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Overlay;
import mil.emp3.api.Polygon;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.examples.common.TestBase;

public class DrawPolygonTest extends TestBase implements Runnable{
    private boolean[] inDrawMode = { false, false };
    private boolean keepDrawing = true;
    private Polygon[] polygon = new Polygon[2];
    private IOverlay[] overlays = new IOverlay[2];

    private boolean[] addPolygonOnComplete = { true, true };

    public DrawPolygonTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    public DrawPolygonTest(Activity activity, IMap map1, IMap map2, boolean doSetup) {
        super(activity, map1, map2, TAG, doSetup);
    }

    public void setAddPolygonOnComplete(int whichMap, boolean addPolygonOnComplete) {
        this.addPolygonOnComplete[whichMap] = addPolygonOnComplete;
    }

    @Override
    public void run() {

        try {
            try {
                overlays[0] = o1;
                overlays[1] = o2;

                maps[0].addOverlay(overlays[0], true);
                maps[0].setFarDistanceThreshold(620000);
                maps[0].setMidDistanceThreshold(610000);

                if(maps[1] != null) {
                    maps[1].addOverlay(overlays[1], true);
                    maps[1].setFarDistanceThreshold(620000);
                    maps[1].setMidDistanceThreshold(610000);
                }
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }

            try {
                drawPolygon(0, true);

                if(maps[1] != null) {
                    drawPolygon(1, true);
                }
            } catch (EMP_Exception e) {
                updateStatus("drawPolygon Test failed " + e.getErrorDeatil());
            }
            // justDrawPolygon(32.4520, 63.44553, 32.4520, 63.4460, 32.4530, 63.4459, 1e5, 45.0);
        } catch (Exception e) {
            Log.d(TAG, "run:" , e);
        } finally {
            testComplete();
        }
    }

    private void justDrawPolygon(int whichMap, double lat1, double lon1, double lat2, double lon2, double lat3, double lon3, double altitude, double tilt) {

        IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
        IGeoFillStyle fillStyle = new GeoFillStyle();
        IGeoColor geoColor = new EmpGeoColor(1.0, 0, 255, 255);
        IGeoColor geoFillColor = new EmpGeoColor(0.7, 0, 0, 255);
        Polygon polygon = new Polygon();

        strokeStyle.setStrokeColor(geoColor);
        strokeStyle.setStrokeWidth(5);
        strokeStyle.setStrokePattern(IGeoStrokeStyle.StrokePattern.dotted);
        polygon.setStrokeStyle(strokeStyle);

        fillStyle.setFillColor(geoFillColor);
        fillStyle.setFillPattern(IGeoFillStyle.FillPattern.hatched);
        polygon.setFillStyle(fillStyle);

        polygon.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);

        List<IGeoPosition> posList = new ArrayList<>();
        IGeoPosition pos = new GeoPosition();
        pos.setLatitude(lat1);
        pos.setLongitude(lon1);
        posList.add(pos);

        pos = new GeoPosition();
        pos.setLatitude(lat2);
        pos.setLongitude(lon2);
        posList.add(pos);

        pos = new GeoPosition();
        pos.setLatitude(lat3);
        pos.setLongitude(lon3);
        posList.add(pos);

        polygon.setPositions(posList);

        polygon.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
        LookAt lookAt = new LookAt();

        lookAt.setName("Main Cam");
        lookAt.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
        lookAt.setAltitude(0);
        lookAt.setHeading(0);
        lookAt.setLatitude(lat1);
        lookAt.setLongitude(lon1);
        lookAt.setRange(altitude);
        lookAt.setTilt(tilt);

        LookAt lookAt2 = new LookAt();

        lookAt2.setName("Main Cam2");
        lookAt2.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
        lookAt2.setAltitude(0);
        lookAt2.setHeading(0);
        lookAt2.setLatitude(lat1);
        lookAt2.setLongitude(lon1);
        lookAt2.setRange(altitude);
        lookAt2.setTilt(tilt);

        try {
            maps[whichMap].setLookAt(lookAt, false);
            overlays[whichMap].addFeature(polygon, true);
            updateMilStdSymbolPosition(p1, lat1-.5, lon1);
            overlays[whichMap].addFeature(p1, true);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    private Polygon setupDrawPolygon() {
        IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
        IGeoFillStyle fillStyle = new GeoFillStyle();
        IGeoColor geoColor = new EmpGeoColor(1.0, 0, 255, 255);
        IGeoColor geoFillColor = new EmpGeoColor(0.7, 0, 0, 255);
        Polygon polygon = new Polygon();

        strokeStyle.setStrokeColor(geoColor);
        strokeStyle.setStrokeWidth(5);
        strokeStyle.setStrokePattern(IGeoStrokeStyle.StrokePattern.dotted);
        polygon.setStrokeStyle(strokeStyle);

        fillStyle.setFillColor(geoFillColor);
        fillStyle.setFillPattern(IGeoFillStyle.FillPattern.hatched);
        polygon.setFillStyle(fillStyle);

        if(null == maps[1]) {
            polygon.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
        } else { // NASA issue.
            polygon.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
        }
        return polygon;
    }

    protected void drawPolygon(int whichMap, boolean wait) throws EMP_Exception {

        polygon[whichMap] = setupDrawPolygon();

        Log.d(TAG, "call this.map.drawFeature");
        maps[whichMap].drawFeature(polygon[whichMap], new FeatureDrawListener(polygon[whichMap], whichMap));
        Log.d(TAG, "done this.map.drawFeature");

        updateStatus("TAP on screen to draw polygon, at least three points required map " + whichMap);
        if(wait) {
            while (keepDrawing) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class FeatureDrawListener implements IDrawEventListener {

        final int whichMap;
        public FeatureDrawListener(IFeature feature, int whichMap) {
            this.whichMap = whichMap;
        }

        @Override
        public void onDrawStart(IMap map) {
            Log.d(TAG, "Draw Start on map-" + whichMap);
            inDrawMode[whichMap] = true;
        }

        @Override
        public void onDrawUpdate(IMap map, IFeature oFeature, List<IEditUpdateData> updateList) {
            Log.d(TAG, "Draw Update on map-" + whichMap);
            int[] aIndexes;
            String temp;

            for (IEditUpdateData updateData: updateList) {
                switch (updateData.getUpdateType()) {
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
                        Log.i(TAG, "   Draw Update " + updateData.getUpdateType().name() + " indexes:{" + temp + "}");
                        break;
                    case MILSTD_MODIFIER_UPDATED:
                        Log.i(TAG, "   Draw Update " + updateData.getUpdateType().name() + " modifier: " + updateData.getChangedModifier().name() + " {" + ((MilStdSymbol) oFeature).getStringModifier(updateData.getChangedModifier()) + "}");
                        break;
                    case ACM_ATTRIBUTE_UPDATED:
                        Log.i(TAG, "   Draw Update " + updateData.getUpdateType().name() + " ACM attribute:{" + updateData.getChangedModifier().name() + "}");
                        break;
                }
            }
        }

        @Override
        public void onDrawComplete(IMap map, IFeature feature) {
            Log.d(TAG, "Draw Complete. " + feature.getClass().getSimpleName() + " pos count " + feature.getPositions().size());
            updateStatus("Draw Complete on map" + whichMap);

            for(int ii = 0; ii < feature.getPositions().size(); ii++ ) {
                Log.d(TAG, "pos " + ii + " lat/lon " + feature.getPositions().get(ii).getLatitude() + "/" + feature.getPositions().get(ii).getLongitude());
            }

            try {
                if(addPolygonOnComplete[whichMap]) {
                    overlays[whichMap].addFeature(feature, true);
                    overlays[whichMap].addFeature(p1, true);
                }
                Log.d(TAG, "add feature");
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            inDrawMode[whichMap] = false;
        }

        @Override
        public void onDrawCancel(IMap map, IFeature originalFeature) {
            Log.d(TAG, "Draw Canceled.");
            updateStatus("Draw Cancelled on map" + whichMap);
            inDrawMode[whichMap] = false;
        }

        @Override
        public void onDrawError(IMap map, String errorMessage) {
            Log.d(TAG, "Draw Error. " + whichMap);
        }
    }

    @Override
    public String[] getSupportedUserActions() {
        String []actions =  new String[6];
        actions[0] = "Remove-1";
        actions[1] = "Save-1";
        actions[2] = "Exit";
        actions[3] = "Start Draw-1";
        actions[4] = "Draw Polygon-1";
        actions[5] = "Get Camera-1";

        return actions;
    }

    @Override
    public String[] getMoreActions() {
        if(null != maps[1]) {
            String[] actions = { "Remove-2", "Save-2", "Start Draw-2", "Draw Polygon-2", "Get Camera-2" };
            return actions;
        }
        return super.getMoreActions();
    }
    @Override
    public void actOn(String userAction) {

        int whichMap = userAction.contains("-1") ? 0 : 1;

        if (userAction.contentEquals("Exit")){
            for(int ii = 0; ii < 2; ii++) {
                if (inDrawMode[ii]) {
                    try {
                        maps[ii].cancelDraw();
                        Thread.sleep(1000);
                    } catch (EMP_Exception e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            endTest();
            keepDrawing = false;
            return;
        }

        if(inDrawMode[whichMap] && userAction.contains("Save")) {
            try {
                maps[whichMap].completeDraw();
                updateStatus("You will need to Exit and restart the test");
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        } else if(inDrawMode[whichMap] && userAction.contains("Remove")) {
            try {
                maps[whichMap].cancelDraw();
                updateStatus("You will need to Exit and restart the test");
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        } else if(userAction.contains("Start Draw")) {
            if(inDrawMode[whichMap]) {
                updateStatus("You must remove or save current drawn feature");
            }
            else {
                try {
                    Log.d(TAG, "call this.map.drawFeature");
                    Polygon polygon = setupDrawPolygon();
                    maps[whichMap].drawFeature(polygon, new FeatureDrawListener(polygon, whichMap));
                    Log.d(TAG, "done this.map.drawFeature");

                } catch(EMP_Exception Ex) {
                    Log.e(TAG, "Draw polygon failed.");
                }
            }
        } else if(userAction.contains("Draw Polygon")) {
            if(inDrawMode[whichMap]) {
                updateStatus("You must remove or save current drawn feature");
            }
            justDrawPolygon(whichMap, 32.4, 63.4, 32.4520, 63.8, 32.8, 63.6, 5000, 0);
        } else if(userAction.contains("Get Camera")) {
            showCamera(maps[whichMap]);
        }
        else {
            Log.e(TAG, "Unsupported action " + userAction + " in mode " + inDrawMode[0] + " " + inDrawMode[1]);
        }
    }
}
