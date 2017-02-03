package mil.emp3.examples.common;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.exceptions.EMP_Exception;

public class MilStdSymbolUtils {
    public static MilStdSymbol buildMilStdSymbol(double latitude, double longitude, IGeoMilSymbol.SymbolStandard standard, String symbolCode,
                                   String name, String modifier) throws EMP_Exception {
        java.util.List<IGeoPosition> oPositionList = new java.util.ArrayList<>();
        IGeoPosition oPosition = new GeoPosition();
        oPosition.setLatitude(latitude);
        oPosition.setLongitude(longitude);
        oPositionList.add(oPosition);
        mil.emp3.api.MilStdSymbol oSPSymbol = new mil.emp3.api.MilStdSymbol(standard, symbolCode);

        oSPSymbol.getPositions().clear();
        oSPSymbol.getPositions().addAll(oPositionList);
        oSPSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, modifier);
        oSPSymbol.setName(name);
        return oSPSymbol;
    }
}
