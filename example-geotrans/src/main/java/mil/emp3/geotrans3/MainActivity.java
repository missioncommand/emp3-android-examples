package mil.emp3.geotrans3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import geotrans3.coordinates.Accuracy;
import geotrans3.coordinates.ConvertResults;
import geotrans3.coordinates.CoordinateTuple;
import geotrans3.coordinates.UTMCoordinates;
import geotrans3.enumerations.ConversionState;
import geotrans3.enumerations.CoordinateType;
import geotrans3.enumerations.HeightType;
import geotrans3.enumerations.SourceOrTarget;
import geotrans3.jni.JNICoordinateConversionService;
import geotrans3.jni.JNIDatumLibrary;
import geotrans3.jni.JNIEllipsoidLibrary;
import geotrans3.parameters.CoordinateSystemParameters;
import geotrans3.parameters.GeodeticParameters;
import geotrans3.parameters.UTMParameters;

public class MainActivity extends AppCompatActivity {


    static
    {
        // Load JNI Native library.
//        System.load("/data/data/nga-geotrans/jni/x86_64/libjnimsp_ccs.so");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String path = getApplicationContext().getFilesDir().getParent() + "/lib/lib";
        TextView converted = (TextView)findViewById(R.id.convert);
        System.load(path + "jnimsp_ccs.so");

        converted.setText(utmInstanceTest());
    }


    private JNIDatumLibrary jniDatumLibrary;
    private JNIEllipsoidLibrary jniEllipsoidLibrary;
    private JNICoordinateConversionService jniCoordinateConversionService;
    private String[] currentDatum = new String[2];
    private CoordinateSystemParameters[] currentParameters = new CoordinateSystemParameters[2];
    private Accuracy accuracy = new Accuracy(-1.0, -1.0, -1.0);
    private CoordinateTuple source;
    private CoordinateTuple target;
    private ConvertResults results;

    public String utmInstanceTest(){
        String resultStr = "";
        try {

            currentDatum[SourceOrTarget.SOURCE] = "WGE";
            currentDatum[SourceOrTarget.TARGET] = "WGE";
            currentParameters[SourceOrTarget.TARGET] = new GeodeticParameters(CoordinateType.GEODETIC, HeightType.NO_HEIGHT);
            UTMCoordinates utmCoordinate = new UTMCoordinates(CoordinateType.UTM,
                    50, 'S', 666792, 3547343);
            currentParameters[SourceOrTarget.SOURCE] = new UTMParameters(CoordinateType.UTM, utmCoordinate.getZone(), 0);
            jniCoordinateConversionService = new JNICoordinateConversionService(currentDatum[SourceOrTarget.SOURCE],
                    currentParameters[SourceOrTarget.SOURCE],
                    currentDatum[SourceOrTarget.TARGET],
                    currentParameters[SourceOrTarget.TARGET]);

            jniDatumLibrary = new JNIDatumLibrary(jniCoordinateConversionService.getDatumLibrary());
            jniEllipsoidLibrary = new JNIEllipsoidLibrary(jniCoordinateConversionService.getEllipsoidLibrary());
            source = new CoordinateTuple(CoordinateType.UTM);
            target = new CoordinateTuple(CoordinateType.GEODETIC);
            results = jniCoordinateConversionService.convertSourceToTarget(source, accuracy, target, accuracy);
            resultStr = Double.toString(results.getAccuracy().getCE90());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultStr;
    }
}
