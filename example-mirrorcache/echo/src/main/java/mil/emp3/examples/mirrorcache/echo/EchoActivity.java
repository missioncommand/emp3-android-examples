package mil.emp3.examples.mirrorcache.echo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import org.cmapi.primitives.IGeoBase;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import mil.emp3.api.MirrorCache;
import mil.emp3.api.Overlay;
import mil.emp3.api.RemoteMap;
import mil.emp3.api.enums.MirrorCacheModeEnum;
import mil.emp3.api.events.ContainerEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IContainerEventListener;

/**
 * This activity demonstrates listening for MirrorCache object updates
 * by echoing them to the screen.
 */
public class EchoActivity extends AppCompatActivity {
    private static final String TAG = EchoActivity.class.getSimpleName();
    static final private SimpleDateFormat SDF = new SimpleDateFormat("hh:mm:ss,SSS");

    private MirrorCache mc;
    private IMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_echo);

        map = new RemoteMap("noop", getApplicationContext(), MirrorCacheModeEnum.INGRESS);

        final TextView tv = (TextView) findViewById(R.id.tv1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        new AsyncTask<Void, Void, Exception>() {
            @Override protected void onPreExecute() {
                tv.append(now() + " >> Connecting...");
            }
            @Override protected Exception doInBackground(final Void... params) {
                try {
                    mc = new MirrorCache(new URI(getString(R.string.text_default_url)));
                    mc.connect();

                } catch (Exception e) {
                    return e;
                }
                return null;
            }
            @Override protected void onPostExecute(final Exception e) {
                if (e == null) { // success
                    tv.append(" connected.\n");

                    tv.append(now() + " >> Subscribing to " + getString(R.string.text_default_productId) + "...");
                    new AsyncTask<Void, Void, Exception>() {
                        @Override protected Exception doInBackground(Void... params) {
                            try {
                                final Overlay overlay = mc.subscribe(getString(R.string.text_default_productId));
                                map.addOverlay(overlay, false);

                                overlay.addContainerEventListener(new IContainerEventListener() {
                                    @Override public void onEvent(final ContainerEvent event) {

                                        tv.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                tv.append(now() + " Overlay[" + overlay.getName() + "].onEvent():" + event.getEvent() + ":\n");

                                                for (IGeoBase child : event.getAffectedChildren()) {
                                                    final String geoId = child.getGeoId().toString();
                                                    tv.append("\t [" + geoId + "] " + child.toString() + "\n");
                                                }
                                            }
                                        });
                                    }
                                });

                            } catch (Exception e) {
                                return e;
                            }
                            return null;
                        }
                        @Override protected void onPostExecute(final Exception e) {
                            if (e == null) { // success
                                tv.append(" subscribed.\n");
                                tv.append(now() + " >> Ready to echo received updates.\n");

                            } else if (e != null) { // failure
                                Log.e(TAG, e.getMessage(), e);
                                tv.append("\n" + now() + " [ERROR] " + e.getMessage() + "\n");
                            }
                        }
                    }.execute();

                } else if (e != null) { // failure
                    Log.e(TAG, e.getMessage(), e);
                    tv.append("\n" + now() + " [ERROR] " + e.getMessage() + "\n");
                }
            }
        }.execute();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");

        try {
            mc.disconnect();
        } catch (EMP_Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        super.onDestroy();
    }

    static private String now() {
        return SDF.format(new Date());
    }
}
