package mil.emp3.example_kmz_exportimport;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jenifer.cochran@rgi-corp.local on 11/7/17.
 */

public class FileUtility
{

    public static File getExampleKmzFile(Context applicationContext)
    {
        File targetFile = null;
        try( InputStream stream = applicationContext.getResources().openRawResource(R.raw.example))
        {
            byte[] buffer = new byte[stream.available()];
            stream.read(buffer);

            targetFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() +File.separator + "example.kmz");
            if(targetFile.exists())
            {
                targetFile.delete();
            }
            try(OutputStream outStream = new FileOutputStream(targetFile))
            {
                outStream.write(buffer);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return targetFile;

    }
}
