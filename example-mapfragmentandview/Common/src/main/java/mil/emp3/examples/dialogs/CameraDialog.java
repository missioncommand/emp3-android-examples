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

import mil.emp3.api.interfaces.ICamera;
import mil.emp3.examples.common.R;

public class CameraDialog extends DialogFragment {

    ICameraDialogListener listener;
    ICamera startPosition;
    private EditText latitude;
    private EditText longitude;
    private EditText altitude;
    private EditText heading;
    private EditText tilt;
    private EditText roll;

    private int whichMap;

    public CameraDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static CameraDialog newInstance(String title, ICameraDialogListener listener, ICamera startPosition, int whichMap) {

        if (null == listener || null == startPosition) {
            throw new IllegalArgumentException("listener and startPosition mus be non-null");
        }
        CameraDialog frag = new CameraDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.setListener(listener);
        frag.setStartPosition(startPosition);
        frag.setWhichMap(whichMap);
        return frag;
    }

    public void setStartPosition(ICamera startPosition) {
        this.startPosition = startPosition;
    }

    public void setListener(ICameraDialogListener listener) {
        this.listener = listener;
    }

    public void setWhichMap(int whichMap) {
        this.whichMap = whichMap;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.camera_dialog, container);
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


        latitude = (EditText) view.findViewById(R.id.latitude);
        longitude = (EditText) view.findViewById(R.id.longitude);
        altitude = (EditText) view.findViewById(R.id.altitude);
        heading = (EditText) view.findViewById(R.id.heading);
        tilt = (EditText) view.findViewById(R.id.tilt);
        roll = (EditText) view.findViewById(R.id.roll);

        latitude.setText(String.format("%1$6.3f", startPosition.getLatitude()));
        longitude.setText(String.format("%1$6.3f", startPosition.getLongitude()));
        altitude.setText(String.format("%1$d", (long) startPosition.getAltitude()));
        heading.setText(String.format("%1$6.3f", startPosition.getHeading()));
        tilt.setText(String.format("%1$6.3f", startPosition.getTilt()));
        roll.setText(String.format("%1$d", (long) startPosition.getRoll()));


        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraDialog.this.dismiss();
            }
        });

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != CameraDialog.this.listener) {
                    CameraDialog.this.listener.cameraSet(whichMap, Double.valueOf(latitude.getText().toString()),
                            Double.valueOf(longitude.getText().toString()),
                            Double.valueOf(altitude.getText().toString()),
                            Double.valueOf(heading.getText().toString()),
                            Double.valueOf(tilt.getText().toString()),
                            Double.valueOf(roll.getText().toString())
                            );
                }
            }
        });
    }

    public interface ICameraDialogListener {
        void cameraSet(int whichMap, double latitude, double longitude, double altitude, double heading, double tilt, double roll);
    }
}

