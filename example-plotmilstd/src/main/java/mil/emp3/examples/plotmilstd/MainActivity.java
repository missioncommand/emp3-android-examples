package mil.emp3.examples.plotmilstd;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import mil.emp3.api.Point;
import mil.emp3.api.events.CameraEvent;
import mil.emp3.api.events.ContainerEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.events.MapViewChangeEvent;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IMap;

import org.cmapi.primitives.GeoColor;
import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoPosition;
import mil.emp3.api.Overlay;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.api.listeners.IContainerEventListener;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.api.listeners.IFeatureInteractionEventListener;

import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoMilSymbol;

import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import armyc2.c2sd.renderer.utilities.UnitDefTable;
import armyc2.c2sd.renderer.utilities.UnitDef;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.enums.IconSizeEnum;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.events.FeatureUserInteractionEvent;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.listeners.IMapViewChangeEventListener;
import mil.emp3.examples.plotmilstd.dialogs.milstdunits.SymbolPropertiesDialog;


public class MainActivity extends AppCompatActivity implements SymbolPropertiesDialog.SymbolPropertiesDialogListener {
    private final static String TAG = MainActivity.class.getSimpleName();
    private IMap map;
    private Overlay oRootOverlay;
    private ICamera oCamera;
    private mil.emp3.api.WMS wmsService;
    private java.util.HashMap<java.util.UUID, IFeature> oFeatureHash = new java.util.HashMap<>();
    private IFeature oCurrentSelectedFeature;
    private final org.cmapi.primitives.IGeoStrokeStyle oSelectedStrokeStyle = new org.cmapi.primitives.GeoStrokeStyle();
    private java.util.HashMap<java.util.UUID, SelectedFeature> oSelectedFeatureHash = new java.util.HashMap<>();
    private boolean bFeatureUIEventProcessed = false;
    private FloatingActionButton oEditorCompleteBtn;
    private FloatingActionButton oEditorCancelBtn;

    public enum PlotModeEnum {
        IDLE,
        NEW,
        EDIT_PROPERTIES,
        EDIT_FEATURE,
        DRAW_FEATURE
    }

    private MainActivity.PlotModeEnum ePlotMode = MainActivity.PlotModeEnum.IDLE;

    class SelectedFeature {
        public IFeature oFeature;
        public org.cmapi.primitives.IGeoStrokeStyle oPreviousStrokeStyle;
        private double dTempValue;

        public SelectedFeature(IFeature feature) {
            this.oFeature = feature;

            switch (this.oFeature.getFeatureType()) {
                case GEO_POINT:
                {
                    Point oPoint = (Point) this.oFeature;

                    if (oPoint.getIconStyle() == null) {
                        IGeoIconStyle oIconStyle = new GeoIconStyle();
                        oPoint.setIconStyle(oIconStyle);
                    }
                    this.dTempValue = oPoint.getIconScale();
                    oPoint.setIconScale(1.4);
                }
                break;
                case GEO_MIL_SYMBOL:
                {
                    MilStdSymbol oSymbol = (MilStdSymbol) feature;

                    this.dTempValue = oSymbol.getIconScale();
                    this.oPreviousStrokeStyle = feature.getStrokeStyle();
                    this.oFeature.setStrokeStyle(MainActivity.this.oSelectedStrokeStyle);
                    oSymbol.setIconScale(1.4);
                }
                break;
            }
            MainActivity.this.oSelectedFeatureHash.put(this.oFeature.getGeoId(), this);
            this.oFeature.apply();
        }

        public void unSelect() {
            switch (this.oFeature.getFeatureType()) {
                case GEO_POINT:
                    ((Point) this.oFeature).setIconScale(this.dTempValue);
                    break;
                case GEO_MIL_SYMBOL:
                    this.oFeature.setStrokeStyle(this.oPreviousStrokeStyle);
                    ((MilStdSymbol) this.oFeature).setIconScale(this.dTempValue);
                    break;
            }
            this.oFeature.apply();
            MainActivity.this.oSelectedFeatureHash.remove(this.oFeature.getGeoId());
        }
    }

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
                    break;
                case LONG_PRESS:
                    break;
                case DRAG:
                    break;
            }
            bFeatureUIEventProcessed = false;
        }
    }

    public class FeatureInteractionEventListener implements IFeatureInteractionEventListener {

        @Override
        public void onEvent(FeatureUserInteractionEvent event) {
            IFeature oFeature = event.getTarget().get(0);
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
                    if (MainActivity.this.ePlotMode == PlotModeEnum.IDLE) {
                        if (MainActivity.this.oSelectedFeatureHash.containsKey(oFeature.getGeoId())) {
                            // It is being unselected.
                            oSelectedItem = MainActivity.this.oSelectedFeatureHash.get(oFeature.getGeoId());
                            oSelectedItem.unSelect();

                            if (MainActivity.this.oSelectedFeatureHash.size() == 0) {
                                // Its the last one.
                                MainActivity.this.oCurrentSelectedFeature = null;
                            } else if ((MainActivity.this.oCurrentSelectedFeature != null) && (MainActivity.this.oCurrentSelectedFeature.getGeoId().equals(oFeature.getGeoId()))) {
                                MainActivity.this.oCurrentSelectedFeature = ((SelectedFeature) MainActivity.this.oSelectedFeatureHash.values().toArray()[0]).oFeature;
                            }
                        } else {
                            // Its being selected.
                            oSelectedItem = new SelectedFeature(oFeature);

                            if (MainActivity.this.oSelectedFeatureHash.size() == 1) {
                                // Its the first one.
                            }
                            MainActivity.this.oCurrentSelectedFeature = event.getTarget().get(0);
                        }
                    }
                    break;
                case DOUBLE_CLICKED:
                    // On a tablet this is the double tap.
                    if (oFeature != null) {
                        // If there is a feature and its selected, we want to place it in edit mode.
                        try {
                            if ((MainActivity.this.ePlotMode == PlotModeEnum.IDLE) && (MainActivity.this.oSelectedFeatureHash.containsKey(oFeature.getGeoId()))) {
                                Log.d(TAG, "FeatureUserInteractionEvent  Entering edit mode.");
                                MainActivity.this.oCurrentSelectedFeature = oFeature;
                                MainActivity.this.map.editFeature(oFeature, new FeatureEditorListener());
                            }
                        } catch (EMP_Exception Ex) {
                        }
                    }
                    break;
                case LONG_PRESS:
                    if (event.getTarget().get(0) != null) {
                        // If there is a feature on the list, we want to place it in edit mode.
                        if (MainActivity.this.ePlotMode == PlotModeEnum.IDLE) {
                            Log.d(TAG, "FeatureUserInteractionEvent  Entering edit properties mode.");
                            MainActivity.this.oCurrentSelectedFeature = event.getTarget().get(0);
                            MainActivity.this.ePlotMode = PlotModeEnum.EDIT_PROPERTIES;
                            MainActivity.this.openFeatureProperties();
                        }
                    }
                    break;
                case DRAG:
                    break;
            }
        }
    }

    public class FeatureEditorListener implements IEditEventListener {

        @Override
        public void onEditStart(IMap map) {
            Log.d(TAG, "Edit Start.");
            MainActivity.this.ePlotMode = PlotModeEnum.EDIT_FEATURE;
            MainActivity.this.oEditorCompleteBtn.show();
            MainActivity.this.oEditorCancelBtn.show();
        }

        @Override
        public void onEditUpdate(IMap map, IFeature oFeature, List<IEditUpdateData> updateList) {
            Log.d(TAG, "Edit Update.");
        }

        @Override
        public void onEditComplete(IMap map, IFeature feature) {
            Log.d(TAG, "Edit Complete.");
            MainActivity.this.ePlotMode = PlotModeEnum.IDLE;
            MainActivity.this.oEditorCompleteBtn.hide();
            MainActivity.this.oEditorCancelBtn.hide();
        }

        @Override
        public void onEditCancel(IMap map, IFeature originalFeature) {
            Log.d(TAG, "Edit Canceled.");
            MainActivity.this.ePlotMode = PlotModeEnum.IDLE;
            MainActivity.this.oEditorCompleteBtn.hide();
            MainActivity.this.oEditorCancelBtn.hide();
        }

        @Override
        public void onEditError(IMap map, String errorMessage) {
            Log.d(TAG, "Edit Error.");
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

                    // Set the symbols affiliation.
                    oSPSymbol.setAffiliation(MilStdSymbol.Affiliation.FRIEND);
                    // Set the symbols altitude mode.
                    oSPSymbol.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
                    //oSPSymbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.HQ_BRIGADE);

                    // Set the position list with 1 position.
                    oSPSymbol.getPositions().clear();
                    oSPSymbol.getPositions().addAll(oPosList);

                    //Set a single modifier.
                    oSPSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSymDef.getDescription());
                    iCount++;

                    // Give the feature a name.
                    oSPSymbol.setName("Unit " + iCount);

                    //Add it to the list we will be adding to the overlay.
                    oFeatureList.add(oSPSymbol);

                    // Keep a reference to the feature in a hash for later use.
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
                // Add all the features to the overlay.
                // It is more efficient to add them in bulk.
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
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        org.cmapi.primitives.IGeoColor oColor = new GeoColor();

        oColor.setRed(255);
        oColor.setGreen(255);
        oColor.setBlue(0);
        oColor.setAlpha(1.0);
        this.oSelectedStrokeStyle.setStrokeColor(oColor);
        this.oSelectedStrokeStyle.setStrokeWidth(5);

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
            map.addContainerEventListener(new IContainerEventListener() {
                @Override
                public void onEvent(ContainerEvent event) {
                    if (event.targetIsOverlay()) {
                        Toast.makeText(MainActivity.this, "Got container event on overlay", Toast.LENGTH_LONG).show();
                    } else if (event.targetIsMap()) {
                        Toast.makeText(MainActivity.this, "Got container event on map", Toast.LENGTH_LONG).show();
                    } else if (event.targetIsFeature()) {
                        Toast.makeText(MainActivity.this, "Got container event on feature", Toast.LENGTH_LONG).show();
                    }
                }
            });
            map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                    Log.d(TAG, "mapStateChangeEvent " + mapStateChangeEvent.getNewState());
                    MainActivity.this.oRootOverlay = new Overlay();
                    try {
                        MainActivity.this.map.addOverlay(MainActivity.this.oRootOverlay, true);
                        MainActivity.this.map.setCamera(MainActivity.this.oCamera, false);
                        MainActivity.this.setEventListeners();

                    } catch (EMP_Exception e) {
                        Log.e(TAG, "", e);
                    }
                    }
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapStateChangeEventListener failed.", e);
        }

        this.oEditorCompleteBtn = (FloatingActionButton) this.findViewById(R.id.editorCompleteBtn);
        this.oEditorCancelBtn = (FloatingActionButton) this.findViewById(R.id.editorCancelBtn);
        this.oEditorCompleteBtn.hide();
        this.oEditorCancelBtn.hide();

        this.oEditorCompleteBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    switch (MainActivity.this.ePlotMode) {
                        case EDIT_FEATURE:
                            MainActivity.this.map.completeEdit();
                            break;
                        case DRAW_FEATURE:
                            MainActivity.this.map.completeDraw();
                            break;
                    }
                } catch (EMP_Exception e) {
                    e.printStackTrace();
                }
            }
        });

        this.oEditorCancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    switch (MainActivity.this.ePlotMode) {
                        case EDIT_FEATURE:
                            MainActivity.this.map.cancelEdit();
                            break;
                        case DRAW_FEATURE:
                            MainActivity.this.map.cancelDraw();
                            break;
                    }
                } catch (EMP_Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setEventListeners() {
        try {
            map.addMapInteractionEventListener(new MapInteractionEventListener());
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapInteractionEventListener failed.", e);
        }
        try {
            map.addFeatureInteractionEventListener(new FeatureInteractionEventListener());
        } catch (EMP_Exception e) {
            Log.e(TAG, "addFeatureInteractionEventListener failed.", e);
        }

        try {
            map.addMapViewChangeEventListener(new IMapViewChangeEventListener(){
                @Override
                public void onEvent(MapViewChangeEvent mapViewChangeEvent) {
                    Log.d(TAG, "mapViewChangeEvent");
                    if (mapViewChangeEvent.getCamera().getGeoId().compareTo(MainActivity.this.oCamera.getGeoId()) == 0) {
                        //oCamera.copySettingsFrom(mapViewChangeEvent.getCamera());
                    }
                    }
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapViewChangeEventListener failed.", e);
        }

        try {
            oCamera.addCameraEventListener(new ICameraEventListener(){
                @Override
                public void onEvent(CameraEvent cameraEvent) {
                    Log.d(TAG, "cameraEvent on " + cameraEvent.getCamera().getName());
                    }
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addCameraEventListener failed.", e);
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

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_plot2units2525B:
                removeAllFeatures();
                plotMilStd(org.cmapi.primitives.GeoMilSymbol.SymbolStandard.MIL_STD_2525B, 200);
                return true;
            case R.id.action_plotunits2525B:
                removeAllFeatures();
                plotMilStd(org.cmapi.primitives.GeoMilSymbol.SymbolStandard.MIL_STD_2525B, 0);
                return true;
            case R.id.action_plotunits2525C:
                removeAllFeatures();
                plotMilStd(org.cmapi.primitives.GeoMilSymbol.SymbolStandard.MIL_STD_2525C, 0);
                return true;
            case R.id.action_removeAll:
                removeAllFeatures();
                return true;
            case R.id.action_exit:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    this.finishAndRemoveTask();
                } else {
                    this.finish();
                }
                return true;
            case R.id.action_addWMS:
                try {
                    map.addMapService(this.wmsService);
                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_removeWMS:
                try {
                    map.removeMapService(this.wmsService);
                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_plotsymbol:
                if (this.ePlotMode == PlotModeEnum.IDLE) {
                    this.oCurrentSelectedFeature = null;
                    this.ePlotMode = PlotModeEnum.NEW;
                    openFeatureProperties();
                }
                return true;
            case R.id.action_plotPoint:
                try {
                    List<IGeoPosition> oPosList = this.getPositions(1, 20000);
                    Point oPoint = new Point();

                    oPoint.getPositions().clear();
                    oPoint.getPositions().addAll(oPosList);
                    this.oRootOverlay.addFeature(oPoint, true);

                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_iconsizetiny:
                try {
                    this.map.setIconSize(IconSizeEnum.TINY);
                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_iconsizesmall:
                try {
                    this.map.setIconSize(IconSizeEnum.SMALL);
                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_iconsizemedium:
                try {
                    this.map.setIconSize(IconSizeEnum.MEDIUM);
                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_iconsizelarge:
                try {
                    this.map.setIconSize(IconSizeEnum.LARGE);
                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_help:
                {
                    AlertDialog.Builder oAlertBox = new AlertDialog.Builder(this);
                    oAlertBox.setMessage(R.string.help_message);
                    oAlertBox.setTitle(R.string.help_title);
                    oAlertBox.setPositiveButton(R.string.help_ok_btn, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                        }
                    });
                    AlertDialog dialog = oAlertBox.create();
                    dialog.show();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openFeatureProperties() {
        SymbolPropertiesDialog featurePropertiesDlg;

        switch (this.ePlotMode) {
            case NEW:
                featurePropertiesDlg = new SymbolPropertiesDialog();

                featurePropertiesDlg.show(this.getFragmentManager(), "Feature Properties");
                break;
            case EDIT_PROPERTIES:
                if (this.oCurrentSelectedFeature != null) {
                    if (this.oCurrentSelectedFeature instanceof MilStdSymbol) {
                        featurePropertiesDlg = new SymbolPropertiesDialog();

                        MilStdSymbol oSymbol = (MilStdSymbol) this.oCurrentSelectedFeature;

                        featurePropertiesDlg.setMilStdVersion(oSymbol.getSymbolStandard());
                        featurePropertiesDlg.setSymbolCode(oSymbol.getSymbolCode());
                        featurePropertiesDlg.setFeatureName(oSymbol.getName());
                        featurePropertiesDlg.show(this.getFragmentManager(), "Feature Properties");
                    }
                }
                break;
        }
    }

    @Override
    public void onSymbolPropertiesSaveClick(SymbolPropertiesDialog oDialog) {
        armyc2.c2sd.renderer.utilities.UnitDef oUnitDef = oDialog.getCurrentUnitDef();
        try {
            Log.d(TAG, "Symbol properties Save Btn");
            switch (this.ePlotMode) {
                case NEW:
                    java.util.List<IGeoPosition> oPositionList = new java.util.ArrayList<>();
                    IGeoPosition oPosition;
                    oPosition = new GeoPosition();
                    oPosition.setLatitude(randomLatitude());
                    oPosition.setLongitude(randomLongitude());
                    oPosition.setAltitude(1000.0);
                    oPositionList.add(oPosition);
                    final MilStdSymbol oNewSymbol = new MilStdSymbol(oDialog.getMilStdVersion(), oDialog.getSymbolCode());
                    oNewSymbol.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
                    oNewSymbol.setName(oDialog.getFeatureName());
                    oNewSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oUnitDef.getDescription());

                    // This is not doing anything
                    // It throws an exception.  Removed by Raju for ticket EMP-2537
                    // SelectedFeature oSelectedItem = new SelectedFeature(oNewSymbol);

                    MainActivity.this.oCurrentSelectedFeature = oNewSymbol;

                    this.map.drawFeature(oNewSymbol, new IDrawEventListener() {
                        @Override
                        public void onDrawStart(IMap map) {
                            Log.d(TAG, "Draw Start");
                            MainActivity.this.ePlotMode = PlotModeEnum.DRAW_FEATURE;
                            MainActivity.this.oEditorCompleteBtn.show();
                            MainActivity.this.oEditorCancelBtn.show();
                        }

                        @Override
                        public void onDrawUpdate(IMap map, IFeature feature, List<IEditUpdateData> updateList) {
                            Log.d(TAG, "Draw Update");
                        }

                        @Override
                        public void onDrawComplete(IMap map, IFeature feature) {
                            Log.d(TAG, "Draw Complete");
                            MainActivity.this.ePlotMode = PlotModeEnum.IDLE;
                            MainActivity.this.oEditorCompleteBtn.hide();
                            MainActivity.this.oEditorCancelBtn.hide();
                            try {
                                MainActivity.this.oRootOverlay.addFeature(oNewSymbol, true);
                                MainActivity.this.oFeatureHash.put(oNewSymbol.getGeoId(), oNewSymbol);
                            } catch (EMP_Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onDrawCancel(IMap map, IFeature originalFeature) {
                            Log.d(TAG, "Draw Cancel");
                            MainActivity.this.ePlotMode = PlotModeEnum.IDLE;
                            MainActivity.this.oEditorCompleteBtn.hide();
                            MainActivity.this.oEditorCancelBtn.hide();
                        }

                        @Override
                        public void onDrawError(IMap map, String errorMessage) {
                            Log.d(TAG, "Draw Error");
                        }
                    });
                    break;
                case IDLE:
                    break;
                case EDIT_PROPERTIES:
                    if (this.oCurrentSelectedFeature != null) {
                        if (this.oCurrentSelectedFeature instanceof MilStdSymbol) {
                            MilStdSymbol oSymbol = (MilStdSymbol) this.oCurrentSelectedFeature;

                            oSymbol.setSymbolStandard(oDialog.getMilStdVersion());
                            oSymbol.setSymbolCode(oDialog.getSymbolCode());
                            //oSymbol.setAffiliation(oDialog.getAffiliation());
                            //oSymbol.setEchelonSymbolModifier(oDialog.getEchelon());
                            oSymbol.setName(oDialog.getFeatureName());
                            oSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oUnitDef.getDescription());
                            oSymbol.apply();
                            this.ePlotMode = MainActivity.PlotModeEnum.IDLE;
                        }
                    }
                    break;
            }
        } catch (EMP_Exception ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onSymbolPropertiesCancelClick(SymbolPropertiesDialog dialog) {
        Log.d(TAG, "Feature properties Cancel Btn");
        this.ePlotMode = MainActivity.PlotModeEnum.IDLE;
    }
}
