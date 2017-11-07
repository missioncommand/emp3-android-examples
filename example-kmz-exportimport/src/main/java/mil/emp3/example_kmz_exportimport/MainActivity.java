package mil.emp3.example_kmz_exportimport;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.cmapi.primitives.GeoPosition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import mil.emp3.api.Camera;
import mil.emp3.api.KMLS;
import mil.emp3.api.Overlay;
import mil.emp3.api.enums.KMLSEventEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEmpExportToTypeCallBack;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IMapService;
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
            this.oCamera = this.map.getCamera();
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

    public void importKmzExample(View view)
    {
        if(hasRequiredPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == false)
        {
            return;
        }

        if(hasRequiredPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == false)
        {
            return;
        }

        File targetFile = FileUtility.getExampleKmzFile(getApplicationContext());
        try
        {
            IMapService mapService = new KMLS(getApplicationContext(), targetFile.toURI().toURL().toString(), new KMLSServiceListener(new LinkedBlockingQueue<KMLSEventEnum>(), this.map));
            mapService.setName("kmzSample_Test");
            this.map.addMapService(mapService);
        }
        catch (EMP_Exception e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }


    public void exportToKmzExample(View view)
    {
        if(hasRequiredPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == false)
        {
            return;
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
                                                                               Toast.makeText(MainActivity.this,
                                                                                             String.format("Export successful. Saved to %s",exportObject.getAbsolutePath()),
                                                                                             Toast.LENGTH_LONG).show();

                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               Toast.makeText(MainActivity.this,
                                                                                              String.format("Export failed. %s",Ex.getMessage()),
                                                                                              Toast.LENGTH_LONG).show();
                                                                           }
                                                                       },
                                   tempDirectory.getAbsolutePath(),
                                   "My_Kmz_File");
    }

    private static final int PERMISSION_REQUEST_CODE = 1;

    private boolean hasRequiredPermission(String permission)
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
        {

            if (checkSelfPermission(permission)
                    == PackageManager.PERMISSION_DENIED)
            {

                Log.d("permission", "permission denied to Write External Storage - requesting it");
                String[] permissions = {permission};

                requestPermissions(permissions, PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

}
