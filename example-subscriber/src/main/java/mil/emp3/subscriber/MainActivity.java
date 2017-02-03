package mil.emp3.subscriber;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.util.Log;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.cmapi.primitives.GeoMilSymbol;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import armyc2.c2sd.renderer.utilities.UnitDef;
import armyc2.c2sd.renderer.utilities.UnitDefTable;
import mil.army.us.coe.udm.lower.Vehicle;
import mil.army.us.coe.udm.upper.GeographicPoint;
import mil.army.us.coe.udm.upper.Noun;
import mil.army.us.coe.udm.upper.Root;
import mil.coe.v3.core.exception.InitializationException;
import mil.coe.v3.core.utils.Identifier;
import mil.coe.v3.core.uuid.Uuid;
import mil.coe.v3.dataservice.DataService;
import mil.coe.v3.dataservice.filters.ClassFilter;
import mil.coe.v3.dataservice.pubsub.Subscriber;
import mil.coe.v3.dataservice.pubsub.Subscription;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Overlay;
import mil.emp3.api.Point;
import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    IMap map;
    IOverlay overlay;
    IOverlay overlay2;
    private List<IFeature> featureList = new ArrayList<>();
    private int nextSymbol = 0;
    Map<UUID, IFeature> featuresOnOverlay = new HashMap<>();

    final String[] geometryTypes = new String[] { "Point", "Polyline", "Polygon" };
    int selectedGeometryIndex = -1;
    Button geometryButton;
    private Thread subscriberThread = new Thread(new SubscriberRunnable());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getFragmentManager();
        map = (IMap) fm.findFragmentById(R.id.map);

        map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
            @Override
            public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                Log.d(TAG, "mapStateChangeEvent " + mapStateChangeEvent.getNewState());
                geometryButton.setEnabled(true);
                if (null == overlay) {
                    overlay = new Overlay(UUID.randomUUID());
                    overlay2 = new Overlay(UUID.randomUUID());
                    try {
                        map.addOverlay(overlay, true);
                        map.addOverlay(overlay2, true);

                    } catch (EMP_Exception e) {
                        e.printStackTrace();
                    }
                }
                FeatureRemoveTest featureRemoveTest = new FeatureRemoveTest(overlay, overlay2);
                Thread frtTestThread = new Thread(featureRemoveTest);
                frtTestThread.start();
            }
        });

        map.addMapInteractionEventListener(new IMapInteractionEventListener() {
            @Override
            public void onEvent(MapUserInteractionEvent mapUserInteractionEvent) {
                Log.d(TAG, "onEvent " + mapUserInteractionEvent.getEvent());
                try {
                    Point geoPoint = new Point(mapUserInteractionEvent.getPoint().x, mapUserInteractionEvent.getPoint().y);
                    if (null == overlay) {
                        overlay = new Overlay(UUID.randomUUID());
                        map.addOverlay(overlay, true);
                    }
                    // overlay.addFeature(geoPoint, true);
                    Log.d(TAG, "onEvent added feature");
                } catch(EMP_Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /*
		 * Initialize Android Geometry Button
		 */
        geometryButton = (Button) findViewById(R.id.btnFreeDraw);
        geometryButton.setEnabled(false);
        geometryButton.setOnClickListener(new View.OnClickListener() {
            /*
             * This displays an AlertDilaog as defined in onCreateDialog()
             * method. Invocation of show() causes onCreateDialog() to be called
             * internally.
             */
            public void onClick(View v) {
                showDialog(0);
            }
        });

        try{
            DataService.initialize();

            subscriberThread.start();

        } catch (Exception e){
            e.printStackTrace();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
	 * Returns an AlertDialog that includes names of all layers in the map
	 * service
	 */
    protected Dialog onCreateDialog(int id) {
        return new AlertDialog.Builder(MainActivity.this)
                .setTitle("Select Geometry")
                .setItems(geometryTypes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // graphicsLayer.removeAll();

                        // ignore first element
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.BOTTOM, 0, 0);

                        // Get item selected by user.
                        String geomType = geometryTypes[which];
                        // label.setText(geomType + " selected.");
                        selectedGeometryIndex = which;

                        // process user selection
                        if (geomType.equalsIgnoreCase("Polygon")) {
                            // myListener.setType("POLYGON");
                            toast.setText("Drag finger across screen to draw a Polygon. \nRelease finger to stop drawing.");
                        } else if (geomType.equalsIgnoreCase("Polyline")) {
                            // myListener.setType("POLYLINE");
                            toast.setText("Drag finger across screen to draw a Polyline. \nRelease finger to stop drawing.");
                        } else if (geomType.equalsIgnoreCase("Point")) {
                            // myListener.setType("POINT");
                            toast.setText("Tap on screen once to draw a Point.");
                        }

                        toast.show();
                    }
                }).create();
    }

    private double randomLatitude() {
        return (Math.random() * 30.0) + 10.0;
    }

    private double randomLongitude() {
        return (Math.random() * 30.0) - 110.0;
    }

    private List<IGeoPosition> getPositions(int iCount, double dAltitude) {
        IGeoPosition oPos;
        List<IGeoPosition> oPosList = new ArrayList<>();
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

    private void processSymbolTable(Map<String,UnitDef> oDefMap,
                                    IGeoMilSymbol.SymbolStandard eStandard,
                                    int iMaxCount) {
        Set<String> oSymbols = oDefMap.keySet();
        UnitDef oSymDef;
        String sSymbolCode;
        int iCount = 0;

        for (String sBasicSymbolCode: oSymbols) {
            //Log.d(TAG, "Symbol " + sBasicSymbolCode);
            if (SymbolUtilities.isWarfighting(sBasicSymbolCode)) {
                oSymDef = oDefMap.get(sBasicSymbolCode);

                List<IGeoPosition> oPosList = this.getPositions(1, 20000.0);

                try {
                    // Allocate the new MilStd Symbol with a MilStd version and the symbol code.
                    MilStdSymbol symbol = new MilStdSymbol(eStandard, sBasicSymbolCode);

                    // Set the symbols affiliation.
                    symbol.setAffiliation(MilStdSymbol.Affiliation.FRIEND);
                    // Set the symbols altitude mode.
                    symbol.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
                    //oSPSymbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.HQ_BRIGADE);

                    // Set the position list with 1 position.
                    symbol.setPositions(oPosList);

                    //Set a single modifier.
                    symbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSymDef.getDescription());
                    iCount++;

                    // Give the feature a name.
                    symbol.setName("Unit " + iCount);

                    //Add it to the list we will be adding to the overlay.
                    featureList.add(symbol);

                } catch (EMP_Exception Ex) {

                }
            }

            if ((iMaxCount > 0) && (iCount >= iMaxCount)) {
                break;
            }
        }
    }

    private class SubscriberRunnable implements Runnable {

        @Override
        public void run() {
            try {
                // Create a Subscriber and connect it to the bus
                Subscriber subscriber = DataService.subscriberFactory().createSubscriber();
                subscriber.connect();

                // Create a class-based filter for all derivatives of Noun published
                ClassFilter classFilter = DataService.filterFactory().createClassFilter(Noun.getClassIdentifier(), true);

                // Create a new subscription with the ClassFilter and a listener for received objects
                Subscription subscription = subscriber.subscribe(classFilter, new MyClassListener());

                Log.d(TAG, "\nListening for instances of Noun published to the data service...\n");

                // Sleep for 10 seconds to show output from ClassFilter
                for(;;)
                    Thread.sleep(10000);

            } catch (InitializationException | IOException /* | SeamValidationException */ e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                // exit
            }
        }

        /**
         * {@link Subscription.SubscriptionListener} implementation to receive notifications of data delivery
         */
        private class MyClassListener implements Subscription.SubscriptionListener {

            MyClassListener() {
                IGeoMilSymbol.SymbolStandard eStandard = IGeoMilSymbol.SymbolStandard.MIL_STD_2525B;
                int iMilStdVersion = (eStandard == IGeoMilSymbol.SymbolStandard.MIL_STD_2525B)? armyc2.c2sd.renderer.utilities.MilStdSymbol.Symbology_2525Bch2_USAS_13_14:
                        armyc2.c2sd.renderer.utilities.MilStdSymbol.Symbology_2525C;
                UnitDefTable oDefTable = UnitDefTable.getInstance();
                Map<String,UnitDef> oDefMap = oDefTable.getAllUnitDefs(iMilStdVersion);

                processSymbolTable(oDefMap, eStandard, 500);
            }

            @Override
            public void onRetract(Uuid uuid) {

            }

            @Override
            public void onUpdate(Root update) {
                if (update instanceof Vehicle) {
                    Vehicle vehicle = (Vehicle) update;
                    GeographicPoint gp = (GeographicPoint) vehicle.getLocation().getLocationGeometry();

                    Log.d(TAG, "MyClassListener: received " + vehicle.getClass().getSimpleName() + " (name=" + vehicle.getName() + ", type=" +
                            vehicle.getTypeCode().getTypeCodeString() + ", lat=" + gp.getLatitude() +
                            ", lon=" + gp.getLongitude() + ")");

                    try {
                        if (null == overlay) {
                            overlay = new Overlay(UUID.randomUUID());
                            map.addOverlay(overlay, true);
                        }

                        UUID uuid = UUID.nameUUIDFromBytes(vehicle.getGlobalId().getBytes());
                        IFeature feature = (Feature)featuresOnOverlay.get(uuid);

                        if(null != feature) {
                            feature.getPositions().get(0).setLatitude(gp.getLatitude());
                            feature.getPositions().get(0).setLongitude(gp.getLongitude());
                            feature.apply();
                            Log.d(TAG, "onEvent updated feature");
                        } else {
                            feature = featureList.get(nextSymbol++);
                            if (nextSymbol >= featureList.size())
                                nextSymbol = 0;
                            feature.getPositions().get(0).setLatitude(gp.getLatitude());
                            feature.getPositions().get(0).setLongitude(gp.getLongitude());
                            Log.d(TAG, "Lat = " + feature.getPositions().get(0).getLatitude()
                                    + " Lon = " + feature.getPositions().get(0).getLongitude());
                            feature.setGeoId(uuid);

                            overlay.addFeature(feature, true);
                            featuresOnOverlay.put(uuid, feature);
                            Log.d(TAG, "onEvent added feature");
                        }
                    } catch(EMP_Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * {@link Subscription.SubscriptionListener} implementation to receive notifications of data delivery
         */
        private class MyIdListener implements Subscription.SubscriptionListener {

            @Override
            public void onRetract(Uuid uuid) {

            }

            @Override
            public void onUpdate(Root update) {
                if (!(update instanceof Noun)) return;

                Noun noun = (Noun) update;
                Log.d(TAG, "MyIdListener: received " + noun.getClass().getName() + " with uuid=(" +
                        noun.getGlobalId().toString() + ")");
            }
        }
    }
}
