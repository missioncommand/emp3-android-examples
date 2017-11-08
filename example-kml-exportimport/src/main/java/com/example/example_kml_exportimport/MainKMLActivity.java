package com.example.example_kml_exportimport;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.os.Handler;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;

import mil.emp3.api.KML;
import mil.emp3.api.Overlay;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.utils.kml.EmpKMLExporter;

public class MainKMLActivity extends AppCompatActivity
{
    private IMap map;
    final private HashMap<UUID, IFeature> oFeatureHash = new HashMap<>();
    final private Overlay                 overlay      = new Overlay();
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_kml);
        this.map = (IMap) findViewById(R.id.map);

        this.handler = new Handler(Looper.getMainLooper());
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

    public void exportToKmlExample(View view)
    {
        if(!hasRequiredPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            return;
        }

        //export the map as a kml file
        EmpKMLExporter.exportToString(this.map,
                                     true,
                                      new IEmpExportToStringCallback()
                                                                      {
                                                                          @Override
                                                                          public void exportSuccess(final String kmlString)
                                                                          {
                                                                              //Write KML export to a file
                                                                              final File storagePublicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                                                                              final File destination = new File(storagePublicDirectory, "MapExport.kml");

                                                                              //ensure that there isn't a file already created at that location
                                                                              if(destination.exists())
                                                                              {
                                                                                  destination.delete();
                                                                              }
                                                                              //write the kml string to a file
                                                                              try(FileOutputStream outputStream = new FileOutputStream(destination))
                                                                              {
                                                                                  final byte[] data = kmlString.getBytes();
                                                                                  outputStream.write(data, 0, data.length);
                                                                                  outputStream.flush();

                                                                                  //notify the user the kml file has been exported
                                                                                  MainKMLActivity.this.makeToast(String.format("Export KML complete. %s", destination.getAbsolutePath()));
                                                                              }
                                                                              catch (FileNotFoundException exception)
                                                                              {
                                                                                  MainKMLActivity.this.makeToast(String.format("Writing KML to file failed. %s", exception.getMessage()));
                                                                              }
                                                                              catch (IOException exception)
                                                                              {
                                                                                  MainKMLActivity.this.makeToast(String.format("Writing KML to file failed. %s", exception.getMessage()));
                                                                              }
                                                                          }

                                                                          @Override
                                                                          public void exportFailed(final Exception Ex)
                                                                          {
                                                                              MainKMLActivity.this.makeToast(String.format("Export to KML failed. %s", Ex.getMessage()));
                                                                          }
                                                                      });
    }

    public void importKmlExample(final View view)
    {
        //read the kml sample data
        try (final InputStream stream = getApplicationContext().getResources().openRawResource(R.raw.kml_samples))
        {
            //create a kml feature to add to the map
            final KML kmlFeature = new KML(stream);
            //add kml to existing overlay on the map
            this.overlay.addFeature(kmlFeature, true);

            //save instance of kml feature to our hashmap
            this.oFeatureHash.put(kmlFeature.getGeoId(), kmlFeature);

            //set the camera where the data is at
            final ICamera camera = this.map.getCamera();
            camera.setAltitude(400);
            camera.setLongitude(-122.08447);
            camera.setLatitude(37.42198);
            camera.apply(true);
        }
        catch (XmlPullParserException | EMP_Exception | IOException Ex )
        {
            Toast.makeText(MainKMLActivity.this,
                           String.format("Importing Kml failed. %s", Ex.getMessage()),
                           Toast.LENGTH_LONG).show();
        }
    }

    public void plotPoint(final View view)
    {
        try
        {
            PlotUtility.plotRandomPoints(100, this.map.getCamera(), this.overlay);
            PlotUtility.plotRandomUrlPoints(100, this.map.getCamera(), this.overlay);
        }
        catch (EMP_Exception Ex)
        {
            Toast.makeText(MainKMLActivity.this,
                           String.format("Plotting features failed. %s", Ex.getMessage()),
                           Toast.LENGTH_LONG).show();
        }
    }

    private static final int PERMISSION_REQUEST_CODE = 1;

    private boolean hasRequiredPermission(final String permission)
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
        {

            if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED)
            {

                Log.d("permission", String.format("permission denied to %s - requesting it", permission));
                final String[] permissions = {permission};

                requestPermissions(permissions, PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    private void makeToast(final String text) {
        handler.post(() -> Toast.makeText(MainKMLActivity.this, text, Toast.LENGTH_LONG).show());
    }
}
