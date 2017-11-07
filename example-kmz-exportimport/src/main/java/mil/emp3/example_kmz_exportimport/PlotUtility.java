package mil.emp3.example_kmz_exportimport;

import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import armyc2.c2sd.renderer.utilities.UnitDef;
import armyc2.c2sd.renderer.utilities.UnitDefTable;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;

/**
 * Created by jenifer.cochran@rgi-corp.local on 11/7/17.
 */

public class PlotUtility
{
    private final static String TAG = MainActivity.class.getSimpleName();

    private final static List<MilStdSymbol.Affiliation> oAffiliationList = Arrays.asList(MilStdSymbol.Affiliation.FRIEND,
                                                                                         MilStdSymbol.Affiliation.HOSTILE,
                                                                                         MilStdSymbol.Affiliation.NEUTRAL);

    private final static List<MilStdSymbol.Echelon> oEchelonList = Arrays.asList(MilStdSymbol.Echelon.UNIT,
                                                                                 MilStdSymbol.Echelon.SQUAD,
                                                                                 MilStdSymbol.Echelon.PLATOON_DETACHMENT,
                                                                                 MilStdSymbol.Echelon.COMPANY_BATTERY_TROOP,
                                                                                 MilStdSymbol.Echelon.BATTALION_SQUADRON,
                                                                                 MilStdSymbol.Echelon.BRIGADE);


    public static void plotManyMilStd(int                     iMaxCount,
                                      IOverlay                oRootOverlay,
                                      HashMap<UUID, IFeature> oFeatureHash,
                                      ICamera                 oCamera)
    {
        UnitDefTable                                     oDefTable = UnitDefTable.getInstance();
        org.cmapi.primitives.GeoMilSymbol.SymbolStandard eStandard =  org.cmapi.primitives.GeoMilSymbol.SymbolStandard.MIL_STD_2525C;
        int iMilStdVersion = (eStandard == org.cmapi.primitives.GeoMilSymbol.SymbolStandard.MIL_STD_2525B) ? armyc2.c2sd.renderer.utilities.MilStdSymbol.Symbology_2525Bch2_USAS_13_14 :
                                                                                                             armyc2.c2sd.renderer.utilities.MilStdSymbol.Symbology_2525C;
        Map<String,UnitDef> oDefMap      = oDefTable.getAllUnitDefs(iMilStdVersion);
        Set<String>         oSymbols     = oDefMap.keySet();
        UnitDef             oSymDef;
        int                 iCount       = 0;
        List<IFeature>      oFeatureList = new ArrayList<>();
        List<IGeoPosition>  oPosList;


        top:
        {
            for (String sBasicSymbolCode : oSymbols)
            {
                for (MilStdSymbol.Echelon eEchelon : oEchelonList)
                {
                    for (MilStdSymbol.Affiliation eAffiliation : oAffiliationList)
                    {
                        if (SymbolUtilities.isWarfighting(sBasicSymbolCode))
                        {
                            oSymDef = oDefMap.get(sBasicSymbolCode);

                            oPosList = new ArrayList<>();
                            oPosList.add(getRandomCoordinate(oCamera));

                            try {
                                // Allocate the new MilStd Symbol with a MilStd version and the symbol code.
                                mil.emp3.api.MilStdSymbol oSPSymbol = new mil.emp3.api.MilStdSymbol(eStandard, sBasicSymbolCode);

                                // Set the symbols affiliation.
                                oSPSymbol.setAffiliation(eAffiliation);
                                // Set the echelon.
                                oSPSymbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.UNIT, eEchelon);

                                // Set the position list with 1 position.
                                oSPSymbol.getPositions().clear();
                                oSPSymbol.getPositions().addAll(oPosList);

                                //Set a single modifier.
                                oSPSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSymDef.getDescription());
                                iCount++;

                                // Give the feature a name.
                                oSPSymbol.setName(String.format("Unit-%04d", iCount));

                                if ((iCount % 2) == 0)
                                {
                                    oSPSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSPSymbol.getName());
                                }

                                //Add it to the list we will be adding to the overlay.
                                oFeatureList.add(oSPSymbol);

                                // Keep a reference to the feature in a hash for later use.
                                oFeatureHash.put(oSPSymbol.getGeoId(), oSPSymbol);
                                if (oFeatureList.size() >= 200)
                                {
                                    try
                                    {
                                        // Add all the features to the overlay.
                                        // It is more efficient to add them in bulk.
                                        oRootOverlay.addFeatures(oFeatureList, true);
                                        Log.d(TAG, "Added " + oFeatureList.size() + " features.");
                                        oFeatureList.clear();
                                    }
                                    catch (EMP_Exception Ex)
                                    {

                                    }
                                }
                            }
                            catch (EMP_Exception Ex)
                            {

                            }
                        }

                        if ((iMaxCount > 0) && (iCount >= iMaxCount))
                        {
                            break top;
                        }
                    }
                }
            }
        }


        if (!oFeatureList.isEmpty())
        {
            try
            {
                // Add all the features to the overlay.
                // It is more efficient to add them in bulk.
                oRootOverlay.addFeatures(oFeatureList, true);
                Log.d(TAG, "Added " + oFeatureList.size() + " features.");
            }
            catch (EMP_Exception Ex)
            {

            }
        }
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
