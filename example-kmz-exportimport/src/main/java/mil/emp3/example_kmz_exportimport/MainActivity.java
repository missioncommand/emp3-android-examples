package mil.emp3.example_kmz_exportimport;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.cmapi.primitives.GeoPosition;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import mil.emp3.api.Camera;
import mil.emp3.api.Overlay;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEmpExportToTypeCallBack;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.utils.kmz.EmpKMZExporter;


public class MainActivity extends AppCompatActivity
{
    private IMap    map;
    private ICamera oCamera = new Camera();
    private Overlay overlay = new Overlay();
    private HashMap<UUID, IFeature> oFeatureHash = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.map = (IMap) findViewById(R.id.map);

        //setup overlay
        this.overlay.setName("Test Overlay");

        try
        {
            this.map.setCamera(this.oCamera, false);
            this.map.addOverlay(overlay, true);
        }
        catch (EMP_Exception e)
        {
            e.printStackTrace();
        }
    }

    public void plotMilitarySymbology(View view)
    {
        PlotUtility.plotManyMilStd(100, this.overlay, this.oFeatureHash, this.oCamera);
    }


    public void exportToKmzExample(View view)
    {
        if(hasRequiredPermissions() == false)
        {
            return;
        }

        try
        {
            this.map.addOverlay(this.overlay, true);
        }
        catch (EMP_Exception e)
        {
            e.printStackTrace();
        }

        File tempDirectory = this.getApplicationContext().getExternalFilesDir(null);
        tempDirectory.mkdirs();

        EmpKMZExporter.exportToKMZ(this.map,
                                   this.overlay,
                                   false,
                                   new IEmpExportToTypeCallBack<File>()
                                                                       {
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               //ExportObject is the kmz file
                                                                               Toast.makeText(MainActivity.this.getApplicationContext(),
                                                                                             String.format("Export successful. Saved to %s",exportObject.getAbsolutePath()),
                                                                                             Toast.LENGTH_LONG);
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               Toast.makeText(MainActivity.this.getApplicationContext(),
                                                                                              String.format("Export failed. %s",Ex.getMessage()),
                                                                                              Toast.LENGTH_LONG);
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
