package mil.emp3.examples.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import org.cmapi.primitives.IGeoPosition;

import mil.emp3.examples.common.R;

public class LookAtDialog extends DialogFragment {

    ILookAtDialogListener listener;
    IGeoPosition startPosition;
    private EditText latitude;
    private EditText longitude;
    private EditText altitude;
    private int whichMap;
    public LookAtDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static LookAtDialog newInstance(String title, ILookAtDialogListener listener, IGeoPosition startPosition, int whichMap) {

        if(null == listener || null == startPosition) {
            throw new IllegalArgumentException("listener and startPosition mus be non-null");
        }
        LookAtDialog frag = new LookAtDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.setListener(listener);
        frag.setStartPosition(startPosition);
        frag.setWhichMap(whichMap);
        return frag;
    }

    public void setStartPosition(IGeoPosition startPosition) {
        this.startPosition = startPosition;
    }

    public void setListener(ILookAtDialogListener listener) {
        this.listener = listener;
    }

    public void setWhichMap(int whichMap) {
        this.whichMap = whichMap;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.lookat_dialog, container);
        getDialog().getWindow().setGravity(Gravity.LEFT | Gravity.BOTTOM);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Window dialogWindow = getDialog().getWindow();

        // Make the dialog possible to be outside touch
        dialogWindow.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);


        latitude = (EditText)  view.findViewById(R.id.latitude);
        longitude = (EditText)  view.findViewById(R.id.longitude);
        altitude = (EditText)  view.findViewById(R.id.altitude);

        latitude.setText(String.format("%1$6.3f", startPosition.getLatitude()));
        longitude.setText(String.format("%1$6.3f", startPosition.getLongitude()));
        altitude.setText(String.format("%1$d", (long)startPosition.getAltitude()));

//        latitude.setText(String.valueOf(startPosition.getLatitude()));
//        longitude.setText(String.valueOf(startPosition.getLongitude()));
//        altitude.setText(String.valueOf(startPosition.getAltitude()));

        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LookAtDialog.this.dismiss();
            }
        });

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != LookAtDialog.this.listener) {
                    LookAtDialog.this.listener.lookAt(whichMap, Double.valueOf(latitude.getText().toString()),
                            Double.valueOf(longitude.getText().toString()),
                            Double.valueOf(altitude.getText().toString()));
                }
            }
        });
    }

    public interface ILookAtDialogListener {
        void lookAt(int whichMap, double latitude, double longitude, double altitude);
    }
}