package mil.emp3.example_kmz_exportimport;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.UUID;
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
    private IMap map;
    final private Overlay overlay = new Overlay();
    final private HashMap<UUID, IFeature> oFeatureHash = new HashMap<>();

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
            this.map.addOverlay(overlay, true);
        }
        catch (EMP_Exception e)
        {
            e.printStackTrace();
        }
    }

    /***
     * Plots military symbology randomly on the map (used as an example of items
     * that can be exported as a kmz)
     * @param view the view that sent the event
     */
    public void plotMilitarySymbology(final View view)
    {
        PlotUtility.plotManyMilStd(100, this.overlay, this.oFeatureHash, this.map.getCamera());
    }

    /***
     * Imports a kmz that is in the resource folder
     * @param view the view that sent the event
     */
    public void importKmzExample(final View view)
    {
        if(hasRequiredPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == false)
        {
            return;
        }

        if(hasRequiredPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == false)
        {
            return;
        }

        //get the example file
        final File targetFile = FileUtility.getExampleKmzFile(getApplicationContext());
        try
        {
            //create a service that points to the kmz file
            final IMapService mapService = new KMLS(getApplicationContext(),
                                                    targetFile.toURI().toURL().toString(),
                                                    new KMLSServiceListener(new LinkedBlockingQueue<>(), this.map));
            //create a name for the kmz service
            mapService.setName("kmzSample_Test");
            //add the service to the map
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


    /***
     * Exports the overlay as a kmz
     * @param view the view that sent the event
     */
    public void exportToKmzExample(View view)
    {
        if(hasRequiredPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == false)
        {
            return;
        }
        //create a temp directory for the exporter
        final File tempDirectory = this.getApplicationContext().getExternalFilesDir(null);
        tempDirectory.mkdirs();

        //Export the overlay as a kmz file
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

    private boolean hasRequiredPermission(final String permission)
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
        {

            if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED)
            {

                Log.d("permission", String.format("permission denied to %s - requesting it"));
                final String[] permissions = {permission};

                requestPermissions(permissions, PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

}
