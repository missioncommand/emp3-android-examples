package mil.emp3.example.geographiclib;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import net.sf.geographiclib.Pair;

import mil.emp3.api.utils.GeodesicWrapper;
import mil.emp3.example.geographiclib.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding dataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    public void onClickExit(View view) {
        finish();
    }

    public void onClickCalculate(View view) {
        double lat1 = Double.parseDouble(dataBinding.Lat1.getText().toString());
        double lon1 = Double.parseDouble(dataBinding.Lon1.getText().toString());
        double lat2 = Double.parseDouble(dataBinding.Lat2.getText().toString());
        double lon2 = Double.parseDouble(dataBinding.Lon2.getText().toString());
        try {
            double geodesicDistance = GeodesicWrapper.computeDistanceBetween(lat1, lon1, lat2, lon2);
            double geodesicBearing = GeodesicWrapper.computeBearing(lat1, lon1, lat2, lon2);
            double rhumbDistance = GeodesicWrapper.computeRhumbDistance(lat1, lon1, lat2, lon2);
            double rhumbBearing = GeodesicWrapper.computeRhumbBearing(lat1, lon1, lat2, lon2);

            dataBinding.GeodesicDistance.setText(Double.toString(geodesicDistance));
            dataBinding.GeodesicBearing.setText(Double.toString(geodesicBearing));
            dataBinding.RhumbDistance.setText(Double.toString(rhumbDistance));
            dataBinding.RhumbBearing.setText(Double.toString(rhumbBearing));
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickCalculateLocation(View view) {
        double lat1 = Double.parseDouble(dataBinding.Lat1.getText().toString());
        double lon1 = Double.parseDouble(dataBinding.Lon1.getText().toString());
        double bearing = Double.parseDouble(dataBinding.GeodesicBearing.getText().toString());
        double distance = Double.parseDouble(dataBinding.GeodesicDistance.getText().toString());
        double rbearing = Double.parseDouble(dataBinding.RhumbBearing.getText().toString());
        double rdistance = Double.parseDouble(dataBinding.RhumbDistance.getText().toString());
        try {
            Pair location = GeodesicWrapper.computePositionAt(bearing, distance, lat1, lon1);
            dataBinding.GeodesicLat2.setText(Double.toString(location.first));
            dataBinding.GeodesicLon2.setText(Double.toString(location.second));
            Pair rlocation = GeodesicWrapper.computeRhumbPositionAt(rbearing, rdistance, lat1, lon1);
            dataBinding.RhumbLat2.setText(Double.toString(rlocation.first));
            dataBinding.RhumbLon2.setText(Double.toString(rlocation.second));
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
