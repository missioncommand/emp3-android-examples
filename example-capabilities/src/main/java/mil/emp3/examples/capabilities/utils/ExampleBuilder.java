package mil.emp3.examples.capabilities.utils;

import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoMilSymbol;

import mil.emp3.api.Circle;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Overlay;
import mil.emp3.api.Polygon;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;

/**
 * Utility methods used by many if not all examples. Methods in this class are copied from AddUpdateRemove class. Since purpose of
 * AddUpdateRemove is to show overlay/feature build capabilities, it will continue to maintain a copy of these methods. For other
 * examples we will use this ExampleBuilder class so that we can focus on the capability being demonstrated.
 *
 * Method signatures here look odd as those members could actually be declared here. We will refactor that later.
 */
public class ExampleBuilder {

    public static void stopAllExamples(Thread[] examples) {
        for(int ii = 0; ii < examples.length; ii++) {
            if(null != examples[ii]) {
                examples[ii].interrupt();
                examples[ii] = null;
            }
        }
    }

    /**
     * You can build any hierarchy as long as there are no cycles. EMP shall check for cycles and will throw an exception
     * if one is detected.
     * @param whichMap
     */
    public static void buildOverlayHierarchy(IMap[] maps, int whichMap, IOverlay[] overlay_a, IOverlay[] overlay_b, IOverlay[] overlay_a_child) {
        IMap map = maps[whichMap];
        boolean visible = true;
        try {
            overlay_a[whichMap] = new Overlay();
            overlay_b[whichMap] = new Overlay();
            overlay_a_child[whichMap] = new Overlay();

            // You must add the parent overlay to the map before you can add a child to that overlay
            map.addOverlay(overlay_a[whichMap], visible);
            overlay_a[whichMap].addOverlay(overlay_a_child[whichMap], visible);
            map.addOverlay(overlay_b[whichMap], visible);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up camera to a fixed position around which example features will be added.
     * @param map
     */
    public static void setupCamera(IMap map) {
        boolean animate = false;
        ICamera camera = map.getCamera();
        camera.setLatitude(33.940);
        camera.setLongitude(-118.394);
        camera.setAltitude(3342);
        camera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
        camera.setHeading(0);
        camera.setTilt(0);
        camera.setRoll(0);
        camera.apply(animate);
    }

    /**
     * Assumes that buildOverlayHierarchy was already executed.
     */
    public static void createAndAddFeatures(int whichMap, IOverlay[] overlay_a, IOverlay[] overlay_b, IOverlay[] overlay_a_child,
                                      Circle[] circle, Polygon[] polygon, MilStdSymbol[] milStdSymbol) {
        boolean visible = true;

        circle[whichMap] = new Circle();
        circle[whichMap].setName("myCircle");  // This is NOT required
        circle[whichMap].getPositions().add(new MyGeoPosition(33.947, -118.402, 0));
        circle[whichMap].setRadius(200);
        try {
            overlay_a[whichMap].addFeature(circle[whichMap], visible);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }

        polygon[whichMap] = new Polygon();
        polygon[whichMap].setName("myPolygon");
        polygon[whichMap].getPositions().add(new MyGeoPosition(33.939375, -118.405725, 0));
        polygon[whichMap].getPositions().add(new MyGeoPosition(33.938669, -118.400342, 0));
        polygon[whichMap].getPositions().add(new MyGeoPosition(33.934375, -118.397326, 0));
        polygon[whichMap].getPositions().add(new MyGeoPosition(33.933214, -118.402899, 0));
        try {
            overlay_b[whichMap].addFeature(polygon[whichMap], visible);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }

        milStdSymbol[whichMap] = new MilStdSymbol();
        milStdSymbol[whichMap].setSymbolCode("SFG*EVSC----***");
        milStdSymbol[whichMap].getPositions().add(new MyGeoPosition(33.940, -118.394, 0));
        milStdSymbol[whichMap].setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, "Ground Track");
        milStdSymbol[whichMap].setName("Missile Support");
        milStdSymbol[whichMap].setAffiliation(MilStdSymbol.Affiliation.FRIEND);
        try {
            overlay_a_child[whichMap].addFeature(milStdSymbol[whichMap], visible);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

}
