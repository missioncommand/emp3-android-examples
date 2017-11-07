package mil.emp3.example_kmz_exportimport;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.cmapi.primitives.GeoPosition;

import java.io.File;

import mil.emp3.api.Overlay;
import mil.emp3.api.Point;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEmpExportToTypeCallBack;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.utils.kmz.EmpKMZExporter;


public class MainActivity extends AppCompatActivity
{
    private IMap map;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        map = (IMap) findViewById(R.id.map);
    }


    public void exportToKmzExample(View view)
    {
        if(hasRequiredPermissions() == false)
        {
            return;
        }
        Point             feature  = new Point();
        final GeoPosition location = new GeoPosition();
        Overlay           overlay  = new Overlay();

        //setup overlay
        overlay.setName("Test Overlay");

        //setup feature
        location.setLatitude(30.0);
        location.setLongitude(-25.0);

        feature.setPosition(location);

        try
        {
            this.map.addOverlay(overlay, true);
        }
        catch (EMP_Exception e)
        {
            e.printStackTrace();
        }

        File tempDirectory = this.getApplicationContext().getExternalFilesDir(null);
        tempDirectory.mkdirs();

        EmpKMZExporter.exportToKMZ(this.map,
                                   overlay,
                                   false,
                                   new IEmpExportToTypeCallBack<File>()
                                                                       {
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               //ExportObject is the kmz file
                                                                               exportObject.exists();
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                           }
                                                                       },
                                   tempDirectory.getAbsolutePath(),
                                   "My_Kmz_File");
    }

    private static final int PERMISSION_REQUEST_CODE = 1;

    private boolean hasRequiredPermissions()
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
        {

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED)
            {

                Log.d("permission", "permission denied to Write External Storage - requesting it");
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

                requestPermissions(permissions, PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

}
