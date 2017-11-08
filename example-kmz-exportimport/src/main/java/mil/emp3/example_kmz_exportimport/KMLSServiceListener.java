package mil.emp3.example_kmz_exportimport;

import android.util.Log;

import java.util.concurrent.BlockingQueue;

import mil.emp3.api.enums.KMLSEventEnum;
import mil.emp3.api.events.KMLSEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IKMLSEventListener;

/**
 * Created by jenifer.cochran@rgi-corp.local on 11/7/17.
 */

public class KMLSServiceListener implements IKMLSEventListener
{
    public static String TAG = KMLSServiceListener.class.getSimpleName();
    IMap map;

    BlockingQueue<KMLSEventEnum> queue;
    public KMLSServiceListener(final BlockingQueue<KMLSEventEnum> queue, final IMap map)
    {
        this.queue = queue;
        this.map = map;
    }


    @Override
    public void onEvent(KMLSEvent event)
    {
        try
        {
            Log.d(TAG, "KMLSServiceListener-onEvent " + event.getEvent().toString() + " status " + event.getTarget().getStatus(map));
        }
        catch(EMP_Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
