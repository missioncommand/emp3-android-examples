package mil.emp3.publisher;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import mil.army.us.coe.udm.lower.Vehicle;
import mil.army.us.coe.udm.lowertypecodes.VehicleEnum;
import mil.army.us.coe.udm.lowertypecodes.VehicleTypeCode;
import mil.army.us.coe.udm.upper.GeographicPoint;
import mil.army.us.coe.udm.upper.GeospatialLocation;
import mil.army.us.coe.udm.upper.Hostility;
import mil.army.us.coe.udm.upper.HostilityStatusCode;
import mil.army.us.coe.udm.upper.PrimaryMobility;
import mil.army.us.coe.udm.upper.Velocity;
import mil.army.us.coe.udm.utils.SeamValidationException;
import mil.coe.v3.core.CoreFactories;
import mil.coe.v3.core.exception.InitializationException;
import mil.coe.v3.core.uuid.Uuid;
import mil.coe.v3.dataservice.DataService;
import mil.coe.v3.dataservice.pubsub.Publisher;

// import mil.army.us.coe.udm.upper.ObjectTemporalCode;
// import mil.army.us.coe.udm.upper.ObjectStateCode;


public class MainActivity extends Activity {

    private static String TAG = MainActivity.class.getName();

    private ScrollView scrollView;
    private TextView textView;
    private Thread publisherThread = new Thread(new PublisherRunnable());
    private TextViewLogger logger = new TextViewLogger(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.textView);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        try{
            DataService.initialize();

            publisherThread.start();
        } catch (Exception e){
            e.printStackTrace();
            logger.log("Application failed to run: " + e.getMessage());
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


    public TextView getTextView() {
        return textView;
    }


    public ScrollView getScrollView() {
        return scrollView;
    }



    private class PublisherRunnable implements Runnable {

        @Override
        public void run() {
            try {
                // Create a Publisher and connect it to the bus
                Publisher publisher = DataService.publisherFactory().createPublisher();

                publisher.connect();

                int countOfVehicles = 1000;
                logger.log("Publishing " + countOfVehicles +" Vehicle objects every 5 seconds...\n");

                Collection<Vehicle> vehicles = generateVehicles(countOfVehicles);

                // publish the vehicles for 40 seconds
                for(int iteration = 0; iteration < 10000; iteration++) {
//                    if(iteration % 2 == 0) {
                        for (Vehicle v: vehicles) {
                            GeographicPoint gp = (GeographicPoint) v.getLocation().getLocationGeometry();
                            gp.setLatitude(gp.getLatitude() + Math.random() - 0.5);
                            gp.setLongitude(gp.getLongitude() + Math.random() - 0.5);
                        }
//                    } else {
//                        for (Vehicle v: vehicles) {
//                            GeographicPoint gp = (GeographicPoint) v.getLocation().getLocationGeometry();
//                            gp.setLatitude(gp.getLatitude()-.1);
//                            gp.setLongitude(gp.getLongitude()-.1);
//                        }
//                    }

                    for(Vehicle vehicle: vehicles) {
                        publisher.publish(vehicle);
                        Thread.sleep(1);
                    }
                }

                // Disconnect the publisher
                publisher.disconnect();

            } catch (InitializationException | IOException | SeamValidationException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                // just exit
            } finally {
                try {
                    // Shutdown the data service. This should be the last thing an application does.
                    DataService.shutdown();

                    logger.log("Publisher thread completed");
                } catch (InitializationException e) {
                    Log.e(TAG, "Failed to shut down data service", e);
                }
            }

        }

        private Collection<Vehicle> generateVehicles(int numToGenerate) throws SeamValidationException {
            Collection<Vehicle> vehicles = new ArrayList<>();

            float latitude = (float) 40.2171;
            float longitude = (float) -74.7429;

            for (int x=0; x<numToGenerate; ++x) {
                Vehicle vehicle = new Vehicle();
                GeographicPoint gp = new GeographicPoint();
                GeospatialLocation location = new GeospatialLocation();

                gp.setLatitude(latitude + (x * .001));
                gp.setLongitude(longitude + (x * .001));

                // gp.setLatitude(Math.random() * 90);
                // gp.setLongitude(Math.random() * 180);

                location.setLocationGeometry(gp);

                vehicle.setTypeCode(new VehicleTypeCode(VehicleEnum.AIR_VEHICLE));
                vehicle.setName("TRUCK" + (x + 1));
                vehicle.setLocation(location);


                byte[] bytes = new byte[] { (byte) (x + 48)};    // adding 48 to make it a printable character
                Uuid globalId = CoreFactories.uuidFactory().createUuid(Vehicle.getClassIdentifier(), "T", bytes);
                vehicle.setGlobalId(globalId);

                // vehicle.setObjectStateCode(ObjectStateCode.LIVE);
                // vehicle.setObjectTemporalCode(ObjectTemporalCode.OCCURRING);

                Hostility hostility = new Hostility();
                hostility.setHostilityStatusCode(HostilityStatusCode.ASSUMED_FRIEND);
                vehicle.setHostility(hostility);

                Velocity velocity = new Velocity();
                velocity.setSpeedRate(Math.random() * 40);

                PrimaryMobility mobility = new PrimaryMobility();
                mobility.setVelocity(velocity);
                vehicle.setPrimaryMobility(mobility);


                vehicles.add(vehicle);
            }
            return vehicles;
        }

    }
}
