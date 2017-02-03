package mil.emp3.subscriber;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
import mil.coe.v3.core.uuid.Uuid;
import mil.emp3.api.Point;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;

/**
 * Created by deepakkarmarkar on 5/6/2016.
 */
public class FeatureRemoveTest implements Runnable {

    IOverlay overlay, overlay2;
    private static String TAG = FeatureRemoveTest.class.getSimpleName();
    private static boolean alreadyRun = false;

    public FeatureRemoveTest(IOverlay overlay, IOverlay overlay2) {
        this.overlay = overlay;
        this.overlay2 = overlay2;
    }
    @Override
    public void run() {
        if(alreadyRun) return;
        alreadyRun = true;

        // test1();
        // test2();
        test3();

    }

    // At the end of the test no features should be on the screen
    private void test1() {
        try {

            Log.d(TAG, "Starting Feature Remove Test1\n");

            double latitude = 40.2171;
            double longitude = -74.7429;
            int count = 1;

            Point p1 = generatePoint("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));
            overlay.addFeature(p1, true);
            Log.d(TAG, "Feature added " + ("TRUCK" + count));
            Thread.sleep(10000);

            count++;
            Point p2 = generatePoint("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));
            p1.addFeature(p2, true);
            Log.d(TAG, "Feature added " + ("TRUCK" + count));
            Thread.sleep(10000);

            p1.removeFeature(p2);
            Log.d(TAG, "Feature removed " + "TRUCK2");
            Thread.sleep(10000);

            overlay.removeFeature(p1);
            Log.d(TAG, "Feature removed " + "TRUCK1");

            overlay.addFeature(p1, true);
            Log.d(TAG, "Feature added " + ("TRUCK" + count));
            p1.addFeature(p2, true);
            Log.d(TAG, "Feature added " + ("TRUCK" + count));
            Thread.sleep(10000);
            overlay.removeFeature(p1);
            Log.d(TAG, "Feature removed " + "TRUCK1");

            Log.d(TAG, "Ending Feature Remove Test1\n");

        } catch (EMP_Exception e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // A feature is added to another feature and a an overlay and then simply removed from the parent feature.
    // It should still remain visible. Issue in StorageObjectWrapper addParent was fixed.

    private void test2() {
        try {

            Log.d(TAG, "Starting Feature Remove Test2\n");

            double latitude = 40.2171;
            double longitude = -74.7429;
            int count = 1;

            Point p1 = generatePoint("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));
            overlay.addFeature(p1, true);
            Log.d(TAG, "Feature p1 added " + ("TRUCK" + count));
            Thread.sleep(10000);

            count++;
            Point p2 = generatePoint("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));
            p1.addFeature(p2, true);
            overlay2.addFeature(p2, true);
            Log.d(TAG, "Feature p2 added to p1 and overlay2 " + ("TRUCK" + count));
            Thread.sleep(10000);

            p1.removeFeature(p2);
            Log.d(TAG, "Feature p2 removed from p1" + "TRUCK2");
            Thread.sleep(10000);

            overlay2.removeFeature(p2);
            Log.d(TAG, "Feature p2 removed from overlay 2" + "TRUCK2");

            Thread.sleep(10000);
            overlay.removeFeature(p1);
            Log.d(TAG, "Feature p1 removed from overlay1 " + "TRUCK1");

            Log.d(TAG, "Ending Feature Remove Test2\n");

        } catch (EMP_Exception e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Remove a list of features where some in the list may have parent children relationship.

    private void test3() {
        try {

            Log.d(TAG, "Starting Feature Remove Test3\n");

            double latitude = 40.2171;
            double longitude = -74.7429;
            int count = 1;

            Point p1 = generatePoint("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));
            overlay.addFeature(p1, true);
            Log.d(TAG, "Feature p1 added " + ("TRUCK" + count));
            Thread.sleep(10000);

            count++;
            Point p2 = generatePoint("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));
            p1.addFeature(p2, true);
            overlay2.addFeature(p2, true);
            Log.d(TAG, "Feature p2 added to p1 and overlay2 " + ("TRUCK" + count));
            Thread.sleep(10000);

            List<IFeature> list = new ArrayList<>();
            list.add(p1); list.add(p2);
            overlay.removeFeatures(list);

            Log.d(TAG, "Feature p2 & P1 removed from overlay");
            Log.d(TAG, "Feature p2 should stay around a it is on overlay2 also");
            Thread.sleep(10000);

            overlay2.removeFeature(p2);
            Log.d(TAG, "Feature p2 removed from overlay 2" + "TRUCK2");

            Thread.sleep(10000);

            Log.d(TAG, "Ending Feature Remove Test3\n");

        } catch (EMP_Exception e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    Point generatePoint(String description, UUID uuid, double latitude, double longitude) {
        Point geoPoint = new Point(latitude, longitude);
        geoPoint.setGeoId(uuid);
        geoPoint.setDescription(description);
        return geoPoint;
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

            vehicle.setTypeCode(new VehicleTypeCode(VehicleEnum.AIR_VEHICLE));            vehicle.setName("TRUCK" + (x + 1));
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

