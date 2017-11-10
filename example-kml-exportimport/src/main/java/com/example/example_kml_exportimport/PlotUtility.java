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


    public static void plotRandomPoints(final int      numberOfPoints,
                                        final ICamera  camera,
                                        final IOverlay overlay) throws EMP_Exception
    {
        for(int pointCount = 0; pointCount < numberOfPoints; pointCount++)
        {
            plotPoint(camera, overlay);
        }
    }

    public static void plotRandomUrlPoints(final int      numberOfPoints,
                                           final ICamera  camera,
                                           final IOverlay overlay) throws EMP_Exception
    {
        for(int pointCount = 0; pointCount < numberOfPoints; pointCount++)
        {
            plotUrlPoint(camera, overlay);
        }
    }

    public static void plotPoint(final ICamera  camera,
                                 final IOverlay overlay) throws EMP_Exception
    {
         final Point        oPoint     = new Point();
         final GeoIconStyle pointStyle = new GeoIconStyle();

         pointStyle.setSize(50.0);

         oPoint.setPosition(getRandomCoordinate(camera));
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
        oPoint.setPosition(getRandomCoordinate(camera));
        oPoint.setIconURI("http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png");

        overlay.addFeature(oPoint, true);
    }


    protected static IGeoPosition getRandomCoordinate(ICamera oCamera)
    {
        IGeoPosition oPos = new GeoPosition();
        double dTemp;

        dTemp = oCamera.getLatitude() + (3 * Math.random()) - 1.5;
        oPos.setLatitude(dTemp);
        dTemp = oCamera.getLongitude() + (3 * Math.random()) - 1.5;
        oPos.setLongitude(dTemp);
        oPos.setAltitude(Math.random() * 16000.0);

        return oPos;
    }
}
