package com.example.example_kml_exportimport;

import android.util.Log;

import org.cmapi.primitives.GeoColor;
import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import armyc2.c2sd.renderer.IconRenderer;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import armyc2.c2sd.renderer.utilities.UnitDef;
import armyc2.c2sd.renderer.utilities.UnitDefTable;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Overlay;
import mil.emp3.api.Point;
import mil.emp3.api.Rectangle;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.EmpGeoColor;

/**
 * @author Jenifer Cochran
 */
public class PlotUtility
{
    private final static String TAG = MainKMLActivity.class.getSimpleName();

    public static void plotPoint(final ICamera  camera,
                                 final IOverlay overlay) throws EMP_Exception
    {
         final Point        oPoint     = new Point();
         final GeoIconStyle pointStyle = new GeoIconStyle();

         pointStyle.setSize(500000.0);

         oPoint.setPosition(getCameraPosition(camera));
         overlay.addFeature(oPoint, true);
         Log.i(TAG, oPoint.toString());
    }

    public static void plotUrlPoint(final ICamera  camera,
                                    final IOverlay overlay) throws EMP_Exception
    {
        final Point         oPoint     = new Point();
        final IGeoIconStyle oIconStyle = new GeoIconStyle();

        //set the style
        oIconStyle.setOffSetY(0);
        oIconStyle.setOffSetX(20);

        //set up the point
        oPoint.setIconStyle(oIconStyle);
        oPoint.setPosition(getCameraPosition(camera));
        oPoint.setIconURI("http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png");

        overlay.addFeature(oPoint, true);
    }

    public static void plotRectangle(final ICamera camera,
                                     final IOverlay overlay) throws EMP_Exception
    {
        final IGeoColor          lineColor   = new EmpGeoColor(1.0, 0, 0, 255);
        final IGeoColor          fillColor   = new EmpGeoColor(0.6, 255, 0, 0);
        final IGeoStrokeStyle    strokeStyle = new GeoStrokeStyle();
        final IGeoFillStyle      fillStyle   = new GeoFillStyle();
        final IGeoPosition       position    = getCameraPosition(camera);
        final Rectangle          oFeature    = new Rectangle();

        strokeStyle.setStrokeColor(lineColor);
        strokeStyle.setStrokeWidth(3);
        fillStyle.setFillColor(fillColor);

        oFeature.setPosition(position);
        oFeature.setHeight(10000000);
        oFeature.setWidth(5000000);
        oFeature.setAzimuth(0);
        oFeature.setStrokeStyle(strokeStyle);
        oFeature.setFillStyle(fillStyle);
        oFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
        overlay.addFeature(oFeature, true);
    }

    private static IGeoPosition getCameraPosition(final ICamera camera)
    {
        final IGeoPosition oPos = new GeoPosition();

        oPos.setLatitude(camera.getLatitude());
        oPos.setLongitude(camera.getLongitude());
        oPos.setAltitude(0);

        return oPos;
    }
}
