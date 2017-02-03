package mil.emp3.examples.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Handler;
import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Overlay;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.interfaces.IOverlay;

public class TestBase implements UserAction {

    protected static String TAG;

    protected double latitude = 40.2171;
    protected double longitude = -74.7429;
    protected MilStdSymbol p1, p1_1, p2, p3;
    protected IOverlay o1, o2, o3;
    protected final IMap m1, m2;
    protected final Activity activity;
    protected OnTestStatusUpdateListener statusUpdateListener;
    protected final int waitInterval = 3000;
    protected final int small_waitInterval = 1000;
    protected final int large_waitInterval = 30000;
    protected final int medium_waitInterval = 15000;
    protected final IMap maps[] = new IMap[2];
    public TestBase(Activity activity, IMap map1, IMap map2, String tag, boolean doSetup) {
        this.m1 = map1;
        this.m2 = map2;
        maps[0] = map1;
        maps[1] = map2;

        this.activity = activity;
        TAG = tag;

        if(activity instanceof OnTestStatusUpdateListener) statusUpdateListener = (OnTestStatusUpdateListener) activity;

        if(doSetup) {
            setUp();
        }
    }

    public TestBase(Activity activity, IMap map1, IMap map2, String tag) {
        this.m1 = map1;
        this.m2 = map2;

        maps[0] = map1;
        maps[1] = map2;

        this.activity = activity;
        TAG = tag;

        if(activity instanceof OnTestStatusUpdateListener) statusUpdateListener = (OnTestStatusUpdateListener) activity;
        setUp();
    }

    protected void setUp()
    {
        int count = 0;
        double mult = .01;
        p1 = generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * mult), longitude + (count * mult));
        o1 = new Overlay();
        o1.setName("o1");
        o2 = new Overlay();
        o2.setName("o2");
        o3 = new Overlay();
        o3.setName("o3");
        count++;
        p1_1 = generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * mult), longitude + (count * mult));
        count++;
        p2 = generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * mult), longitude + (count * mult));
        count++;
        p3 = generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * mult), longitude + (count * mult));

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(null != m1) {
                        m1.setCamera(CameraUtility.buildCamera(40.2171, -74.7429, 9000), false);
                    }
                    if(null != m2) {
                        m2.setCamera(CameraUtility.buildCamera(40.2171, -74.7429, 9000), false);
                    }

                    java.util.List<String> oLayers = new java.util.ArrayList<>();

                    oLayers.add("BlueMarble-200412");
                    mil.emp3.api.WMS wmsService = new mil.emp3.api.WMS(
                            "http://worldwind25.arc.nasa.gov/wms",
                            WMSVersionEnum.VERSION_1_1_1,
                            "image/png",
                            true,
                            oLayers
                    );

                    if(null != m1) {
                        List<IMapService> mapServices = m1.getMapServices();
                        if ((null == mapServices) || (0 == mapServices.size())) {
                            m1.addMapService(wmsService);
                        }
                    }

                    if(null != m2) {
                        List<IMapService> mapServices = m2.getMapServices();
                        if ((null == mapServices) || (0 == mapServices.size())) {
                            m2.addMapService(wmsService);
                        }
                    }
                } catch (EMP_Exception e) {
                    e.printStackTrace();
                }  catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    protected static MilStdSymbol generateMilStdSymbol(String description, UUID uuid, double latitude, double longitude) {
        java.util.List<IGeoPosition> oPositionList = new java.util.ArrayList<>();
        IGeoPosition oPosition = new GeoPosition();
        oPosition.setLatitude(latitude);
        oPosition.setLongitude(longitude);
        oPositionList.add(oPosition);
        MilStdSymbol oSPSymbol = null;
        try {
            oSPSymbol = new MilStdSymbol(
                    IGeoMilSymbol.SymbolStandard.MIL_STD_2525C,
                    "SFAPMFF--------");
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }

        oSPSymbol.setPositions(oPositionList);
        oSPSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, "My First Icon");
        oSPSymbol.setName(description);
        oSPSymbol.setDescription(description);
        oSPSymbol.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
        return oSPSymbol;
    }

    protected static MilStdSymbol generateMilStdSymbol(String description, ICamera camera) {
        java.util.List<IGeoPosition> oPositionList = new java.util.ArrayList<>();
        IGeoPosition oPosition = new GeoPosition();
        oPosition.setLatitude(camera.getLatitude());
        oPosition.setLongitude(camera.getLongitude());
        oPositionList.add(oPosition);
        MilStdSymbol oSPSymbol = null;
        try {
            oSPSymbol = new MilStdSymbol(
                    IGeoMilSymbol.SymbolStandard.MIL_STD_2525C,
                    "SFAPMFF--------");
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }

        oSPSymbol.setPositions(oPositionList);
        oSPSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, "My First Icon");
        oSPSymbol.setName(description);
        oSPSymbol.setDescription(description);
        oSPSymbol.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
        updateDesignator(oSPSymbol);
        return oSPSymbol;
    }

    protected static List<IFeature> generateMilStdSymbolList(int howMany, double latitude, double longitude) {
        long startCount = 1000;
        List<IFeature> list = new ArrayList<>();
        for(int ii = 0; ii < howMany; ii++) {
            list.add(generateMilStdSymbol(String.valueOf(ii), new java.util.UUID(startCount, startCount++), latitude + (ii * .005), longitude + (ii * .005)));
        }
        return list;
    }

    protected void updateMilStdSymbolPosition(MilStdSymbol symbol, double latitude, double longitude) {
        IGeoPosition oPosition = symbol.getPositions().get(0);
        oPosition.setLatitude(latitude);
        oPosition.setLongitude(longitude);
        updateDesignator(symbol);
    }

    protected void updateMilStdSymbolPosition(MilStdSymbol symbol, double latitude, double longitude, double altitude) {
        IGeoPosition oPosition = symbol.getPositions().get(0);
        oPosition.setLatitude(latitude);
        oPosition.setLongitude(longitude);
        oPosition.setAltitude(altitude);
        updateDesignator(symbol);
    }

    protected void updateMilStdSymbolAltitude(MilStdSymbol symbol, double altitude) {
        IGeoPosition oPosition = symbol.getPositions().get(0);
        oPosition.setAltitude(altitude);
        updateDesignator(symbol);
    }
    protected void deltaMilStdSymbolPosition(MilStdSymbol symbol, double delta_lat, double delta_lon) {
        IGeoPosition oPosition = symbol.getPositions().get(0);
        oPosition.setLatitude(oPosition.getLatitude() + delta_lat);
        oPosition.setLongitude(oPosition.getLongitude() + delta_lon);
        updateDesignator(symbol);
    }

    protected static void updateDesignator(MilStdSymbol symbol) {
        IGeoPosition oPosition = symbol.getPositions().get(0);

        String designator  = String.format(Locale.US, "%1$6.3f:%2$6.3f:%3$6.0f",
                oPosition.getLatitude(), oPosition.getLongitude(), oPosition.getAltitude());
        symbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, designator);
    }
    protected void updateStatus(String updatedStatus) {
        if(null != statusUpdateListener) {
            statusUpdateListener.onTestStatusUpdated(updatedStatus);
        }
    }

    protected void updateStatus(String TAG, String updatedStatus) {
        if(null != statusUpdateListener) {
            statusUpdateListener.onTestStatusUpdated(TAG + " " + updatedStatus);
        }
    }

    protected void testComplete() {
        if(null != statusUpdateListener) {
            statusUpdateListener.onTestCompleted(this.getClass().getSimpleName());
        }
    }

    protected void testComplete(String message) {
        if(null != statusUpdateListener) {
            statusUpdateListener.onTestCompleted(this.getClass().getSimpleName() + ":" + message);
        }
    }
    private String currentTest = null;
    protected void startTest(String test) {
        currentTest = test;
        updateStatus("Starting... " + this.getClass().getSimpleName() + ":[" + currentTest + "] ");
    }

    protected void endTest() {
        updateStatus("Ending... " + this.getClass().getSimpleName() + ":[" + currentTest + "] ");
        clearMaps();
    }

    protected void updateTestStatus(int map1FeatureCount, int map2FeatureCount) {
        updateStatus(this.getClass().getSimpleName() + ":[" + currentTest + "] " + " Map1 Feature Count [" + map1FeatureCount + "] Map2 Feature Count [" +
            map2FeatureCount + "]");
    }

    private void clearMaps() {
        if(null != m1) clearMap(m1);
        if(null != m2) clearMap(m2);
    }

    private void clearMap(IMap map) {
        if(null != map) {
            List<IOverlay> overlays = map.getAllOverlays();
            for(IOverlay overlay: overlays) {
                try {
                    map.removeOverlay(overlay);
                } catch (EMP_Exception e) {

                }
            }
        }
    }

    protected void displayStatus(String status) {
        updateStatus(this.getClass().getSimpleName() + ":[" + currentTest + "] " + status );
        try {
            Thread.sleep(small_waitInterval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected Handler getHandler() {
        return statusUpdateListener.getHandler();
    }

    protected void showCamera(IMap xMap) {
        ICamera camera = xMap.getCamera();
        String status  = String.format(Locale.US, "Starting Pos " +
                        "Camera " + " (L:N:A %1$6.3f %2$6.3f %3$6.0f H:T:R %4$6.3f %5$6.3f %6$6.3f) ",
                camera.getLatitude(), camera.getLongitude(), camera.getAltitude(),
                camera.getHeading(), camera.getTilt(), camera.getRoll());
        updateStatus(status);
    }
    @Override
    public void actOn(String userAction) {
        Log.d(TAG, "actOn " + userAction);
    }

    @Override
    public String[] getSupportedUserActions() {
        return null;
    }

    @Override
    public String[] getMoreActions() { return null; }
}

