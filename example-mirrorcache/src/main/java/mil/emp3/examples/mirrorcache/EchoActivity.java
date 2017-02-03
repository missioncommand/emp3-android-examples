package mil.emp3.examples.mirrorcache;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPoint;
import org.cmapi.primitives.IGeoPosition;

import java.util.Map;

import mil.emp3.api.interfaces.ICamera;
import mil.emp3.mirrorcache.api.IMirrorCacheStateChangeListener;
import mil.emp3.mirrorcache.api.IMirrorable;
import mil.emp3.mirrorcache.api.MirrorCache;

/**
 * This activity demonstrates listening for MirrorCache object updates
 */
public class EchoActivity extends Activity {
    private static final String TAG = EchoActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_echo);

        final TextView tv = (TextView) findViewById(R.id.tv1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        MirrorCache.getInstance().onCreate(this);
        MirrorCache.getInstance().addStateChangeListener(new IMirrorCacheStateChangeListener() {
            @Override
            public void onMirrorred() {
            }

            @Override
            public void onDelete(IMirrorable iMirrorable) {
            }

            @Override
            public void onUpdate(IMirrorable o) {
                Log.d(TAG, "onUpdate");

                final StringBuilder sb = new StringBuilder();

                if (o instanceof ICamera) {
                    sb.append("\n[ Echo: ICamera ]\n");

                    final ICamera camera = (ICamera) o;
                    sb.append("\tgeoId: " + camera.getGeoId() + "\n" + "\n");

                    sb.append("\tlatitude: " + camera.getLatitude() + "\n");
                    sb.append("\tlongitude: " + camera.getLongitude() + "\n");
                    sb.append("\taltitude: " + camera.getAltitude() + "\n");
                    sb.append("\theading: " + camera.getHeading() + "\n");
                    sb.append("\ttilt: " + camera.getTilt() + "\n");
                    sb.append("\troll: " + camera.getRoll() + "\n");

                } else if (o instanceof IGeoMilSymbol) {
                    sb.append("\n[ Echo: IGeoMilSymbol ]\n");

                    final IGeoMilSymbol symbol = (IGeoMilSymbol) o;
                    sb.append("\tgeoId: " + symbol.getGeoId() + "\n");

                    sb.append("\tname: " + symbol.getName() + "\n");
                    sb.append("\tsymbolCode: " + symbol.getSymbolCode() + "\n");
                    sb.append("\tsymbolStandard: " + symbol.getSymbolStandard().toString() + "\n");

                    sb.append("\tpositions (" + symbol.getPositions().size() + "): \n");
                    for (IGeoPosition position : symbol.getPositions()) {
                        sb.append("\t\tlat=" + position.getLatitude() + ", long=" + position.getLongitude() + "\n");
                    }

                    sb.append("\tmodifiers (" + symbol.getModifiers().size() + "): \n");
                    for (Map.Entry entry : symbol.getModifiers().entrySet()) {
                        sb.append("\t\t" + entry.getKey() + " = " + entry.getValue() + "\n");
                    }

                } else if (o instanceof IGeoPoint) {
                    sb.append("\n[ Echo: IGeoPoint ]\n");

                    final IGeoPoint point = (IGeoPoint) o;
                    sb.append("\tgeoId: " + point.getGeoId() + "\n");

                    sb.append("\ticonUri: " + point.getIconURI() + "\n");

                    sb.append("\tpositions (" + point.getPositions().size() + "): \n");
                    for (IGeoPosition position : point.getPositions()) {
                        sb.append("\t\tlat=" + position.getLatitude() + ", long=" + position.getLongitude() + "\n");
                    }
                }

                tv.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, sb.toString());
                        tv.append(sb.toString());
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");

        MirrorCache.getInstance().onDestroy();

        super.onDestroy();
    }
}
