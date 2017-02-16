package mil.emp3.examples.userinteractionevents;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.interfaces.IMap;

import org.cmapi.primitives.GeoColor;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;
import mil.emp3.api.Overlay;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoMilSymbol;

import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import armyc2.c2sd.renderer.utilities.UnitDefTable;
import armyc2.c2sd.renderer.utilities.UnitDef;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.events.FeatureUserInteractionEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.listeners.IFeatureInteractionEventListener;
import mil.emp3.api.listeners.IMapInteractionEventListener;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    public enum App_Mode {
        NORMAL_MODE,
        EDIT_MODE
    }

    class SelectedFeature {
        public IFeature oFeature;
        public org.cmapi.primitives.IGeoStrokeStyle oPreviousStrokeStyle;

        public SelectedFeature(IFeature feature) {
            this.oFeature = feature;
            this.oPreviousStrokeStyle = feature.getStrokeStyle();
            this.oFeature.setStrokeStyle(MainActivity.this.oSelectedStrokeStyle);
            MainActivity.this.oSelectedFeatureHash.put(this.oFeature.getGeoId(), this);
            this.oFeature.apply();
        }

        public void unSelect() {
            this.oFeature.setStrokeStyle(this.oPreviousStrokeStyle);
            this.oFeature.apply();
            MainActivity.this.oSelectedFeatureHash.remove(this.oFeature.getGeoId());
        }
    }

    private IMap map;
    private Overlay oRootOverlay;
    private ICamera oCamera;
    private App_Mode eCurrentMode = App_Mode.NORMAL_MODE;
    private boolean bFeatureUIEventProcessed = false;
    private mil.emp3.api.WMS wmsService;
    private java.util.HashMap<java.util.UUID, IFeature> oFeatureHash = new java.util.HashMap<>();
    private final org.cmapi.primitives.IGeoStrokeStyle oSelectedStrokeStyle = new org.cmapi.primitives.GeoStrokeStyle();
    private java.util.HashMap<java.util.UUID, SelectedFeature> oSelectedFeatureHash = new java.util.HashMap<>();

    public class MapInteractionEventListener implements IMapInteractionEventListener {

        @Override
        public void onEvent(MapUserInteractionEvent event) {
            if (event.getCoordinate() == null) {
                Log.d(TAG, "Map User Interactive Event: " + event.getEvent().name() + " X/Y: " + event.getPoint().x + " / " +
                        event.getPoint().y + "  Lat/Lon: null");
            } else {
                Log.d(TAG, "Map User Interactive Event: " + event.getEvent().name() + " X/Y: " + event.getPoint().x + " / " +
                        event.getPoint().y + "  Lat: " + event.getCoordinate().getLatitude() + " Lon: " + event.getCoordinate().getLongitude());
            }
            
            switch (event.getEvent()) {
                case CLICKED:
                    break;
                case DOUBLE_CLICKED:
                    if (!bFeatureUIEventProcessed) {
                        if (event.getCoordinate() != null) {
                            oCamera.setLatitude(event.getCoordinate().getLatitude());
                            oCamera.setLongitude(event.getCoordinate().getLongitude());
                            oCamera.apply(false);
                        }
                    }
                    break;
                case LONG_PRESS:
                    break;
            }
            bFeatureUIEventProcessed = false;
        }
    }
    
    public class FeatureInteractionEventListener implements IFeatureInteractionEventListener {

        @Override
        public void onEvent(FeatureUserInteractionEvent event) {
            IFeature oFeature;
            SelectedFeature oSelectedItem;

            bFeatureUIEventProcessed = true;
            if (event.getCoordinate() == null) {
                Log.d(TAG, "Feature User Interactive Event: " + event.getEvent().name() + " Feature Count: " + event.getTarget().size() + " X/Y: " + event.getPoint().x + " / " +
                        event.getPoint().y + "  Lat/Lon: null");
            } else {
                Log.d(TAG, "Feature User Interactive Event: " + event.getEvent().name() + " Feature Count: " + event.getTarget().size() + " X/Y: " + event.getPoint().x + " / " +
                        event.getPoint().y + "  Lat: " + event.getCoordinate().getLatitude() + " Lon: " + event.getCoordinate().getLongitude());
            }
            switch (event.getEvent()) {
                case CLICKED:
                    oFeature = event.getTarget().get(0);
                    if (MainActivity.this.oSelectedFeatureHash.containsKey(oFeature.getGeoId())) {
                        // It is being unselected.
                        oSelectedItem = MainActivity.this.oSelectedFeatureHash.get(oFeature.getGeoId());
                        oSelectedItem.unSelect();

                        if (MainActivity.this.oSelectedFeatureHash.size() == 0) {
                            // Its the last one. Lock the map.
                            try {
                                MainActivity.this.map.setMotionLockMode(MapMotionLockEnum.UNLOCKED);
                            } catch (EMP_Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        // Its being selected.
                        oSelectedItem = new SelectedFeature(oFeature);

                        if (MainActivity.this.oSelectedFeatureHash.size() == 1) {
                            // Its the first one. Lock the map.
                            try {
                                MainActivity.this.map.setMotionLockMode(MapMotionLockEnum.SMART_LOCK);
                            } catch (EMP_Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case DOUBLE_CLICKED:
                    break;
                case LONG_PRESS:
                    break;
                case DRAG:
                    oFeature = event.getTarget().get(0);
                    if (MainActivity.this.oSelectedFeatureHash.containsKey(oFeature.getGeoId())) {
                        java.util.List<IGeoPosition> oPositionList = oFeature.getPositions();
                        oPositionList.get(0).setLatitude(event.getCoordinate().getLatitude());
                        oPositionList.get(0).setLongitude(event.getCoordinate().getLongitude());
                        oFeature.getPositions().clear();
                        oFeature.getPositions().addAll(oPositionList);
                        oFeature.apply();
                    }
                    break;
            }
        }
        
    }
    
    private double randomLatitude() {
        return (Math.random() * 30.0) + 10.0;
    }

    private double randomLongitude() {
        return (Math.random() * 30.0) - 110.0;
    }

    private java.util.List<IGeoPosition> getPositions(int iCount, double dAltitude) {
        IGeoPosition oPos;
        java.util.List<IGeoPosition> oPosList = new java.util.ArrayList<>();
        double dLat = this.randomLatitude();
        double dLong = this.randomLongitude();
        
        switch (iCount) {
            case 0:
                return oPosList;
            case 1:
                oPos = new GeoPosition();
                oPos.setAltitude(dAltitude);
                oPos.setLatitude(dLat);
                oPos.setLongitude(dLong);
                oPosList.add(oPos);
                break;
            default:
                for (int iIndex = 0; iIndex < iCount; iIndex++) {
                    oPos = new GeoPosition();
                    oPos.setAltitude(dAltitude);
                    oPos.setLatitude(dLat);
                    oPos.setLongitude(dLong);
                    oPosList.add(oPos);
                    
                    if (dLat > 0.0) {
                        dLat -= 0.5;
                    } else {
                        dLat += 0.5;
                    }
                    if (dLong > 0.0) {
                        dLong -= 0.5;
                    } else {
                        dLong += 0.5;
                    }
                }   break;
        }
        return oPosList;
    }
    
    private void processSymbolTable(java.util.Map<java.lang.String,UnitDef> oDefMap,
            org.cmapi.primitives.GeoMilSymbol.SymbolStandard eStandard,
            int iMilStdVersion,
            int iMaxCount) {
        java.util.Set<String> oSymbols = oDefMap.keySet();
        UnitDef oSymDef;
        String sSymbolCode;
        int iCount = 0;
        java.util.List<IFeature> oFeatureList = new java.util.ArrayList<>();

        for (String sBasicSymbolCode: oSymbols) {
            //Log.d(TAG, "Symbol " + sBasicSymbolCode);
            if (SymbolUtilities.isWarfighting(sBasicSymbolCode)) {
                oSymDef = oDefMap.get(sBasicSymbolCode);
                
                java.util.List<IGeoPosition> oPosList = this.getPositions(1, 20000.0);

                try {
                    // Allocate the new MilStd Symbol with a MilStd version and the symbol code.
                    mil.emp3.api.MilStdSymbol oSPSymbol = new mil.emp3.api.MilStdSymbol(eStandard, sBasicSymbolCode);

                    oSPSymbol.setAffiliation(MilStdSymbol.Affiliation.FRIEND);
                    //oSPSymbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.HQ_BRIGADE);
                    oSPSymbol.getPositions().clear();
                    oSPSymbol.getPositions().addAll(oPosList);
                    oSPSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSymDef.getDescription());
                    iCount++;
                    oSPSymbol.setName("Unit " + iCount);
                    oFeatureList.add(oSPSymbol);
                    this.oFeatureHash.put(oSPSymbol.getGeoId(), oSPSymbol);
                } catch (EMP_Exception Ex) {

                }
            }
            
            if ((iMaxCount > 0) && (iCount >= iMaxCount)) {
                break;
            }
        }
        if (!oFeatureList.isEmpty()) {
            try {
                oRootOverlay.addFeatures(oFeatureList, true);
                Log.d(TAG, "Added " + oFeatureList.size() + " features.");
            } catch (EMP_Exception Ex) {

            }
        }
    }
    
    private void plotMilStd(org.cmapi.primitives.GeoMilSymbol.SymbolStandard eStandard, int iCount) {
        int iMilStdVersion = (eStandard == org.cmapi.primitives.GeoMilSymbol.SymbolStandard.MIL_STD_2525B)? armyc2.c2sd.renderer.utilities.MilStdSymbol.Symbology_2525Bch2_USAS_13_14:
                armyc2.c2sd.renderer.utilities.MilStdSymbol.Symbology_2525C;
        UnitDefTable oDefTable = UnitDefTable.getInstance();
        java.util.Map<java.lang.String,UnitDef> oDefMap = oDefTable.getAllUnitDefs(iMilStdVersion);

        processSymbolTable(oDefMap, eStandard, iMilStdVersion, iCount);
    }
    
    private void removeAllFeatures() {
        if (!this.oFeatureHash.isEmpty()) {
            java.util.List<IFeature> oFeatureList = new java.util.ArrayList<>();
            
            for (java.util.UUID uuid: this.oFeatureHash.keySet()) {
                oFeatureList.add(this.oFeatureHash.get(uuid));
            }
            
            try {
                this.oRootOverlay.removeFeatures(oFeatureList);
                Log.d(TAG, "Removed " + oFeatureList.size() + " features.");
                this.oFeatureHash.clear();
            } catch (EMP_Exception Ex) {
                
            }
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        org.cmapi.primitives.IGeoColor oColor = new GeoColor();

        oColor.setRed(255);
        oColor.setGreen(255);
        oColor.setBlue(0);
        oColor.setAlpha(1.0);
        this.oSelectedStrokeStyle.setStrokeColor(oColor);
        this.oSelectedStrokeStyle.setStrokeWidth(5);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        map = (IMap) findViewById(R.id.map);

        oCamera = new mil.emp3.api.Camera();
        oCamera.setName("Main Cam");
        oCamera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
        oCamera.setAltitude(1000000.0);
        oCamera.setHeading(0.0);
        oCamera.setLatitude(40.0);
        oCamera.setLongitude(-105.0);
        oCamera.setRoll(0.0);
        oCamera.setTilt(0.0);

        java.util.List<String> oLayers = new java.util.ArrayList<>();

        oLayers.add("BlueMarble-200412");
        try {
            this.wmsService = new mil.emp3.api.WMS(
                    "http://worldwind25.arc.nasa.gov/wms",
                    WMSVersionEnum.VERSION_1_1,
                    "image/png",
                    true,
                    oLayers
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                    Log.d(TAG, "mapStateChangeEvent " + mapStateChangeEvent.getNewState());
                    oRootOverlay = new Overlay();
                    try {
                        map.addOverlay(oRootOverlay, true);
                        map.setCamera(oCamera, false);

                        map.addMapInteractionEventListener(new MapInteractionEventListener());
                        map.addFeatureInteractionEventListener(new FeatureInteractionEventListener());
                    } catch (EMP_Exception e) {
                        Log.e(TAG, "", e);
                    }
                }
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapStateChangeEventListener", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_plot2units2525B) {
            removeAllFeatures();
            plotMilStd(org.cmapi.primitives.GeoMilSymbol.SymbolStandard.MIL_STD_2525B, 200);
            return true;
        }
        if (id == R.id.action_removeAll) {
            removeAllFeatures();
            return true;
        }
        if (id == R.id.action_exit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.finishAndRemoveTask();
            } else {
                this.finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
