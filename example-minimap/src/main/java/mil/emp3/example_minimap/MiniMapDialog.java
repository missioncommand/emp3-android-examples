package mil.emp3.example_minimap;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import mil.emp3.api.interfaces.IMap;

public class MiniMapDialog extends DialogFragment {
    private static final String TAG = MiniMapDialog.class.getSimpleName();

    private IMap empMap;
    private View miniMapView;
    private int iX;
    private int iY;

    public MiniMapDialog() {
        super();
    }

    public void setMap(IMap map) {
        empMap = map;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog oDialog = super.onCreateDialog(savedInstanceState);

        oDialog.setTitle("Mini Map");
        Window window = oDialog.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.LEFT);

        return oDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        miniMapView = inflater.inflate(R.layout.minimap, null);

        if (null != empMap) {
            View miniMap = empMap.showMiniMap();
            ((RelativeLayout) miniMapView).addView(miniMap);
        }

        miniMapView.setOnTouchListener(new View.OnTouchListener() {
            private int dx = 0;
            private int dy = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dx = MiniMapDialog.this.iX - (int) motionEvent.getRawX();
                        dy = MiniMapDialog.this.iY - (int) motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        MiniMapDialog.this.iX = (int) (motionEvent.getRawX() + dx);
                        MiniMapDialog.this.iY = (int) (motionEvent.getRawY() + dy);
                        Window window = MiniMapDialog.this.getDialog().getWindow();
                        WindowManager.LayoutParams params = window.getAttributes();
                        params.x = MiniMapDialog.this.iX;
                        params.y = MiniMapDialog.this.iY;
                        window.setAttributes(params);
                        break;
                }
                return true;
            }
        });

        return miniMapView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = this.getDialog().getWindow();
        Resources resources = getActivity().getResources();
        int pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 320, resources.getDisplayMetrics());
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = pixels;
        params.height = pixels * 3 / 4;

        window.setAttributes(params);
        MiniMapDialog.this.iX = params.x;
        MiniMapDialog.this.iY = params.y;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (null != this.empMap) {
            this.empMap.hideMiniMap();
            this.empMap = null;
        }
    }
}
