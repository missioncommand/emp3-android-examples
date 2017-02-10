package mil.emp3.examples.capabilities.navItems;

import android.app.Activity;

import org.cmapi.primitives.GeoColor;
import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoStrokeStyle;

import mil.emp3.api.Circle;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Path;
import mil.emp3.api.Polygon;
import mil.emp3.api.Text;
import mil.emp3.api.enums.FontSizeModifierEnum;
import mil.emp3.api.enums.IconSizeEnum;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.examples.capabilities.common.Emp3TesterDialogBase;
import mil.emp3.examples.capabilities.common.ExecuteTest;
import mil.emp3.examples.capabilities.common.NavItemBase;
import mil.emp3.examples.capabilities.utils.ExampleBuilder;
import mil.emp3.examples.capabilities.utils.MyGeoPosition;

/**
 * EMP supports number of ways to change the graphic styles for variety of features. This example shows how to use all the methods
 * associated with that capability.
 * cycleLabelStyle, cycleStrokeStyle, cycleFillStyle, and cycleMapStyle have the core of the example code.
 *
 * GeoLabelStyle, GeoStrokeStyle, GeoFillStyle
 * IconSize, MilStdLabelSetting and FontSizeModifier
 */
public class Styles extends NavItemBase {
    private static String TAG = HighlightFeatures.class.getSimpleName();

    // User can launch up to two maps, so all the members are setup to allow for two maps.
    // It is possible to share overlays and features across maps but this example doesn't do that.

    private IOverlay overlay_a[] = new IOverlay[ExecuteTest.MAX_MAPS];
    private IOverlay overlay_b[]= new IOverlay[ExecuteTest.MAX_MAPS];
    private IOverlay overlay_a_child[]= new IOverlay[ExecuteTest.MAX_MAPS];

    private Circle circle[]= new Circle[ExecuteTest.MAX_MAPS];
    private Polygon polygon[] = new Polygon[ExecuteTest.MAX_MAPS];
    private MilStdSymbol milStdSymbol[] = new MilStdSymbol[ExecuteTest.MAX_MAPS];
    private Text text[] = new Text[ExecuteTest.MAX_MAPS];
    private Path path[] = new Path[ExecuteTest.MAX_MAPS];

    private Thread examples[] = new Thread[ExecuteTest.MAX_MAPS];

    private IGeoLabelStyle labelStyle[] = new IGeoLabelStyle[ExecuteTest.MAX_MAPS];
    private IGeoStrokeStyle strokeStyle[] = new IGeoStrokeStyle[ExecuteTest.MAX_MAPS];
    private IGeoFillStyle fillStyle[] = new IGeoFillStyle[ExecuteTest.MAX_MAPS];

    // Following are set directly on IMap
    private IconSizeEnum iconSize[] = new IconSizeEnum[ExecuteTest.MAX_MAPS];
    private MilStdLabelSettingEnum milStdLabelSetting[] = new MilStdLabelSettingEnum[ExecuteTest.MAX_MAPS];
    private FontSizeModifierEnum fontSizeModifier[] = new FontSizeModifierEnum[ExecuteTest.MAX_MAPS];

    public Styles(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Start", "Stop"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        return null;
    }
    protected void test0() {

        try {
            testThread = Thread.currentThread();
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(large_waitInterval * 10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } finally {
            endTest();
        }
    }

    @Override
    public boolean actOn(String userAction) {

        // User can launch one or two maps and then can select whihc map to run the example on.
        final int whichMap = ExecuteTest.getCurrentMap();

        try {
            if(Emp3TesterDialogBase.isEmp3TesterDialogBaseActive()) {
                updateStatus("Dismiss the dialog first");
                return false;
            }

            if (userAction.equals("Exit")) {
                ExampleBuilder.stopAllExamples(examples);
                testThread.interrupt();
            } else if(userAction.equals("ClearMap")) {
                ExampleBuilder.stopAllExamples(examples);
                clearMaps();
            } else if(userAction.equals("Start")) {
                if(null == examples[whichMap]) {
                    examples[whichMap] = new Thread(new Example(whichMap));
                    examples[whichMap].start();
                }
            } else if(userAction.equals("Stop")) {
                if(null != examples[whichMap]) {
                    examples[whichMap].interrupt();
                    examples[whichMap] = null;
                }
            }
        } catch (Exception e) {
            updateStatus(TAG, e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void clearMapForTest() {
        String userAction = "ClearMap";
        actOn(userAction);
    }

    @Override
    protected boolean exitTest() {
        String userAction = "Exit";
        return(actOn(userAction));
    }

    /**
     * @param whichMap
     */
    private void stopExample(int whichMap) {
        clearMap(maps[whichMap]);
    }

    private void addTextFeature(int whichMap, IOverlay[] overlay, boolean visible) {
        text[whichMap] = new Text();
        text[whichMap].setText("Example Text");
        text[whichMap].getPositions().add(new MyGeoPosition(33.947, -118.390, 0));
        try {
            overlay[whichMap].addFeature(text[whichMap], visible);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    private void addPathFeature(int whichMap, IOverlay[] overlay, boolean visible) {
        path[whichMap] = new Path();
        path[whichMap].getPositions().add(new MyGeoPosition(33.934375, -118.390, 0));
        path[whichMap].getPositions().add(new MyGeoPosition(33.933669, -118.388, 0));
        path[whichMap].getPositions().add(new MyGeoPosition(33.929375, -118.385, 0));
        try {
            overlay[whichMap].addFeature(path[whichMap], visible);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize style settings
     * @param whichMap
     */
    private void initializeStyles(int whichMap) {
        labelStyle[whichMap] = new GeoLabelStyle();
        labelStyle[whichMap].setTypeface(IGeoLabelStyle.Typeface.REGULAR);
        labelStyle[whichMap].setColor(new MyGeoColor(255, 0, 0, 1));
        labelStyle[whichMap].setFontFamily("MONOSPACE");
        labelStyle[whichMap].setJustification(IGeoLabelStyle.Justification.LEFT);
        labelStyle[whichMap].setOutlineColor(new MyGeoColor(0, 255, 255, 1));
        labelStyle[whichMap].setSize(12.0);

        strokeStyle[whichMap] = new GeoStrokeStyle();
        strokeStyle[whichMap].setStipplingFactor(1);
        strokeStyle[whichMap].setStipplingPattern(Short.valueOf("0101010101010101", 2));
        strokeStyle[whichMap].setStrokeColor(new MyGeoColor(255, 255, 0, 1));
        strokeStyle[whichMap].setStrokeWidth(5);

        fillStyle[whichMap] = new GeoFillStyle();
        fillStyle[whichMap].setFillColor(new MyGeoColor(128, 128, 0, 1));
        fillStyle[whichMap].setFillPattern(IGeoFillStyle.FillPattern.crossHatched);

        // Following are set directly on IMap
        iconSize[whichMap] = IconSizeEnum.TINY;
        milStdLabelSetting[whichMap] = MilStdLabelSettingEnum.REQUIRED_LABELS;
        fontSizeModifier[whichMap] = FontSizeModifierEnum.SMALLEST;
    }

    private void cycleLabelStyle(int whichMap) {
        if(IGeoLabelStyle.Typeface.REGULAR == labelStyle[whichMap].getTypeface()) {
            labelStyle[whichMap].setTypeface(IGeoLabelStyle.Typeface.ITALIC);
            labelStyle[whichMap].setColor(new MyGeoColor(0, 255, 255, 1));
            labelStyle[whichMap].setFontFamily("SERIF");
            labelStyle[whichMap].setJustification(IGeoLabelStyle.Justification.RIGHT);
            labelStyle[whichMap].setOutlineColor(new MyGeoColor(255, 0, 0, 1));
            labelStyle[whichMap].setSize(18.0);
        } else {
            labelStyle[whichMap].setTypeface(IGeoLabelStyle.Typeface.REGULAR);
            labelStyle[whichMap].setColor(new MyGeoColor(255, 0, 0, 1));
            labelStyle[whichMap].setFontFamily("MONOSPACE");
            labelStyle[whichMap].setJustification(IGeoLabelStyle.Justification.LEFT);
            labelStyle[whichMap].setOutlineColor(new MyGeoColor(0, 255, 255, 1));
            labelStyle[whichMap].setSize(12.0);
        }
        text[whichMap].setLabelStyle(labelStyle[whichMap]);
        text[whichMap].apply();
    }

    private void cycleStrokeStyle(int whichMap) {
        if(strokeStyle[whichMap].getStipplingFactor() == 1) {
            strokeStyle[whichMap].setStipplingFactor(5);
            strokeStyle[whichMap].setStipplingPattern(Short.valueOf("0111000001110000", 2));
            strokeStyle[whichMap].setStrokeColor(new MyGeoColor(0, 255, 255, 1));
            strokeStyle[whichMap].setStrokeWidth(2);
        } else {
            strokeStyle[whichMap].setStipplingFactor(1);
            strokeStyle[whichMap].setStipplingPattern(Short.valueOf("0101010101010101", 2));
            strokeStyle[whichMap].setStrokeColor(new MyGeoColor(255, 255, 0, 1));
            strokeStyle[whichMap].setStrokeWidth(5);
        }
        circle[whichMap].setStrokeStyle(strokeStyle[whichMap]);
        circle[whichMap].apply();

        path[whichMap].setStrokeStyle(strokeStyle[whichMap]);
        path[whichMap].apply();
    }

    private void cycleFillStyle(int whichMap) {
        if(fillStyle[whichMap].getFillPattern() == IGeoFillStyle.FillPattern.crossHatched) {
            fillStyle[whichMap].setFillColor(new MyGeoColor(0, 128, 128, 1));
            fillStyle[whichMap].setFillPattern(IGeoFillStyle.FillPattern.solid);
        } else {
            fillStyle[whichMap].setFillColor(new MyGeoColor(128, 128, 0, 1));
            fillStyle[whichMap].setFillPattern(IGeoFillStyle.FillPattern.crossHatched);
        }
        polygon[whichMap].setFillStyle(fillStyle[whichMap]);
        polygon[whichMap].apply();
    }

    private void cycleMapStyle(int whichMap) {
        if(maps[whichMap].getIconSize() == IconSizeEnum.TINY) {
            iconSize[whichMap] = IconSizeEnum.LARGE;
            milStdLabelSetting[whichMap] = MilStdLabelSettingEnum.ALL_LABELS;
            fontSizeModifier[whichMap] = FontSizeModifierEnum.LARGEST;
        } else {
            iconSize[whichMap] = IconSizeEnum.TINY;
            milStdLabelSetting[whichMap] = MilStdLabelSettingEnum.REQUIRED_LABELS;
            fontSizeModifier[whichMap] = FontSizeModifierEnum.SMALLEST;
        }

        try {
            maps[whichMap].setIconSize(iconSize[whichMap]);
            maps[whichMap].setMilStdLabels(milStdLabelSetting[whichMap]);
            maps[whichMap].setFontSizeModifier(fontSizeModifier[whichMap]);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    class Example implements Runnable {
        int whichMap;
        Example(int whichMap) {
            this.whichMap = whichMap;
        }

        @Override
        public void run() {
            try {
                ExampleBuilder.buildOverlayHierarchy(maps, whichMap, overlay_a, overlay_b, overlay_a_child);
                ExampleBuilder.setupCamera(maps[whichMap]);
                ExampleBuilder.createAndAddFeatures(whichMap, overlay_a, overlay_b, overlay_a_child, circle, polygon, milStdSymbol);
                addTextFeature(whichMap, overlay_a, true);
                addPathFeature(whichMap, overlay_b, true);
                initializeStyles(whichMap);

                while (!Thread.interrupted()) {
                    try {
                        Thread.sleep(3 * 1000);
                        ExampleBuilder.setupCamera(maps[whichMap]); // In case user has moved it.
                        cycleLabelStyle(whichMap);

                        Thread.sleep(3 * 1000);
                        ExampleBuilder.setupCamera(maps[whichMap]); // In case user has moved it.
                        cycleStrokeStyle(whichMap);

                        Thread.sleep(3 * 1000);
                        ExampleBuilder.setupCamera(maps[whichMap]); // In case user has moved it.
                        cycleFillStyle(whichMap);

                        Thread.sleep(3 * 1000);
                        ExampleBuilder.setupCamera(maps[whichMap]); // In case user has moved it.
                        cycleMapStyle(whichMap);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } finally {
                stopExample(whichMap);
            }
        }
    }

    /**
     * Some utility classes.
     */

    class MyGeoColor extends GeoColor {
        MyGeoColor(int red, int green, int blue, double alpha) {
            this.setRed(red);
            this.setGreen(green);
            this.setBlue(blue);
            this.setAlpha(alpha);
        }
    }
}
