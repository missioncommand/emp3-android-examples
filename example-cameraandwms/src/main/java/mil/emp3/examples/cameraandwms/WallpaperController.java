package mil.emp3.examples.cameraandwms;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import mil.emp3.api.RemoteMap;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.MirrorCacheModeEnum;
import mil.emp3.api.events.CameraEvent;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;


public class WallpaperController extends StandOutWindow {
    private static final String TAG = WallpaperController.class.getSimpleName();

    private Handler wallpaperHandler;

//    private RemoteMap imap = null;
    private static IMap imap = null;
    private ICamera icamera = null;
    private EventListenerHandle cameraListenerHandle = null;

    private ImageButton upButton;
    private ImageButton downButton;
    private ImageButton rightButton;
    private ImageButton leftButton;
    private ImageButton shrinkButton;
    private ImageButton zoomInButton;
    private ImageButton zoomOutButton;
    private ImageButton lockButton;

    private View background;


    private boolean isHolding;

    public WallpaperController(){}

    public WallpaperController(IMap map) {
        imap = map;
    }

    private class MoveCameraTask extends AsyncTask<View, Void, Void> {
        protected Void doInBackground(View... view) {
            while (true) {

                if (view[0].equals(upButton)) {
                    pan(0.0, 0.01);
                } else if (view[0].equals(downButton)) {
                    pan(0.0, -0.01);
                } else if (view[0].equals(rightButton)) {
                    pan(0.01, 0.0);
                } else if (view[0].equals(leftButton)) {
                    pan(-0.01, 0.0);
                } else if (view[0].equals(shrinkButton)) {
                    WallpaperController.this.hide(StandOutWindow.DEFAULT_ID);
                } else if (view[0].equals(zoomInButton)) {
                    zoom(1.0 / 1.01f);
                } else if (view[0].equals(zoomOutButton)) {
                    zoom(1.01f);
                } else if (view[0].equals(lockButton)) {
                }

                try { Thread.sleep(50); } catch (InterruptedException e) { e.printStackTrace(); }

                if (!isHolding) break;
            }
            return null;
        }
    }


    @Override
    public String getAppName() {
        return "Map Controller";
    }

    @Override
    public int getAppIcon() {
        return 0;
    }

    @Override
    public void createAndAttachView(int id, FrameLayout frame) {
        Log.d(TAG, "createAndAttachView");

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.mapdpad, null);


        background = (View) view.findViewById(R.id.backgroundView);
        background.setOnKeyListener(keyListener);

        upButton = (ImageButton) view.findViewById(R.id.upButton);
        upButton.setOnClickListener(buttonListener);
        upButton.setOnTouchListener(buttonHoldListener);

        downButton = (ImageButton) view.findViewById(R.id.downButton);
        downButton.setOnClickListener(buttonListener);
        downButton.setOnTouchListener(buttonHoldListener);

        leftButton = (ImageButton) view.findViewById(R.id.leftButton);
        leftButton.setOnClickListener(buttonListener);
        leftButton.setOnTouchListener(buttonHoldListener);

        rightButton = (ImageButton) view.findViewById(R.id.rightButton);
        rightButton.setOnClickListener(buttonListener);
        rightButton.setOnTouchListener(buttonHoldListener);

        shrinkButton = (ImageButton) view.findViewById(R.id.shrinkButton);
        shrinkButton.setOnClickListener(buttonListener);

        zoomInButton = (ImageButton) view.findViewById(R.id.zoomInButton);
        zoomInButton.setOnClickListener(buttonListener);
        zoomInButton.setOnTouchListener(buttonHoldListener);

        zoomOutButton = (ImageButton) view.findViewById(R.id.zoomOutButton);
        zoomOutButton.setOnClickListener(buttonListener);
        zoomOutButton.setOnTouchListener(buttonHoldListener);

        lockButton = (ImageButton) view.findViewById(R.id.lockButton);
        lockButton.setOnClickListener(buttonListener);

        frame.addView(view);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // imap = new RemoteMap(TAG, this, MirrorCacheModeEnum.EGRESS);

        try {
            imap.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                    Log.d(TAG, "new state " + mapStateChangeEvent.getNewState());

                    if (mapStateChangeEvent.getNewState() == MapStateEnum.MAP_READY) {
                        icamera = imap.getCamera();
                    }
                }
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapStateChangeEventListener", e);
        }
    }
    @Override
    public void onDestroy() {
        //imap.onDestroy();
        super.onDestroy(); //TODO this doesn't trigger
    }

    private ICameraEventListener cameraListener = new ICameraEventListener() {
        @Override
        public void onEvent(CameraEvent cameraEvent) {
            ICamera c = cameraEvent.getCamera();
            Log.d(TAG, "notified of update to camera");
            Log.d(TAG, "latitude " + c.getLatitude() );
            Log.d(TAG, "longitude " + c.getLongitude() );
            Log.d(TAG, "Altitude " + c.getAltitude() );
            //Log.d(TAG, "range " + c.getRange() );
            Log.d(TAG, "Heading " + c.getHeading() );
            Log.d(TAG, "Tilt" + c.getTilt() );
            Log.d(TAG, "passed reference: " + c );
            Log.d(TAG, "saved reference: " + icamera );
        }
    };



    private View.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            Log.d(TAG, "onKey " + keyEvent.getAction());
            return false;
        }
    };


    private View.OnTouchListener buttonHoldListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (1==1) return false;//TODO
            if (icamera == null) return false;

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                isHolding = true;
                new MoveCameraTask().execute(view);

            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                isHolding = false;
            }

            return true;
        }
    };


    private Button.OnClickListener buttonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (icamera != null) {

                /*try { //TODO remove me thx

                    final IGeoPosition oPosition = new GeoPosition();
                    oPosition.setLatitude((Math.random() * 5.0) + 40.0);
                    oPosition.setLongitude((Math.random() * 5.0) - 100.0);

                    final List<IGeoPosition> oPositionList = new ArrayList<>();
                    oPositionList.add(oPosition);

                    final MilStdSymbol milStdSymbol = new MilStdSymbol(IGeoMilSymbol.SymbolStandard.MIL_STD_2525C, "SFAPMFF--------");
                    milStdSymbol.setPositions(oPositionList);
                    milStdSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, "My First Icon");
                    milStdSymbol.setName("Feature 1");

                    Log.d(TAG, "Instantiating mirrorable..");
                    final MirrorableMilStdSymbol m = new MirrorableMilStdSymbol(milStdSymbol);

                    Log.d(TAG, "Invoking MC.update..");
                    MirrorCache.getInstance().update(m, null, milStdSymbol.getGeoId().toString());


                } catch (EMP_Exception e) {
                    Log.e(TAG, "ERROR: " + e.getMessage(), e);
                }*/

                if (view.equals(upButton)) {
                    pan(0, 0.05);
                } else if (view.equals(downButton)) {
                    pan(0, -0.05);
                } else if (view.equals(rightButton)) {
                    pan(0.05, 0);
                } else if (view.equals(leftButton)) {
                    pan(-0.05, 0);
                } else if (view.equals(shrinkButton)) {
                    WallpaperController.this.hide(StandOutWindow.DEFAULT_ID);
                } else if (view.equals(zoomInButton)) {
                    zoom(1.0/1.1f);
                } else if (view.equals(zoomOutButton)) {
                    zoom(1.1f);
//                    } else if (view.equals(lockButton) {
//                        icamera.setLock(!camera.isLocked());
                }
            }
        }
    };




    @Override
    public int getFlags(int id) {

        return StandOutFlags.FLAG_WINDOW_FOCUS_INDICATOR_DISABLE
                | StandOutFlags.FLAG_BODY_MOVE_ENABLE
                | StandOutFlags.FLAG_WINDOW_HIDE_ENABLE
                | StandOutFlags.FLAG_WINDOW_FOCUS_INDICATOR_DISABLE
                | StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TAP
                | StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TOUCH;
    }

    @Override
    public StandOutLayoutParams getParams(int id, Window window) {
        return new StandOutLayoutParams(id, 350, 500, 100, 100);
    }

    @Override
    public String getPersistentNotificationTitle(int id) {
        return getAppName() + " Running";
    }

    @Override
    public String getPersistentNotificationMessage(int id) {
        return "Click to add a new " + getAppName();
    }

    // return an Intent that creates a new MultiWindow
    @Override
    public Intent getPersistentNotificationIntent(int id) {
        return StandOutWindow.getShowIntent(this, getClass(), getUniqueId());
    }

    @Override
    public int getHiddenIcon() {
        return R.drawable.map_controls;
    }

    @Override
    public String getHiddenNotificationTitle(int id) {
        return getAppName() + " Hidden";
    }

    @Override
    public String getHiddenNotificationMessage(int id) {
        return "Click to show Map Controls";
    }

    // return an Intent that restores the MultiWindow
    @Override
    public Intent getHiddenNotificationIntent(int id) {
        return StandOutWindow.getShowIntent(this, getClass(), id);
    }

    private void pan(double xVelocity, double yVelocity) {
        Log.d(TAG, "pan");

        double latitude = icamera.getLatitude();
        double longitude = icamera.getLongitude();
//        double range = icamera.getRange();
        double altitude = icamera.getAltitude();
        double heading = icamera.getHeading();

        double panScalingFactor = 10f;
        double sin = Math.sin(heading*Math.PI/180f);
        double cos = Math.cos(heading*Math.PI/180f);

        final double newLat = latitude + (cos * yVelocity + sin * xVelocity) * panScalingFactor;// * altitude;
        final double newLon = longitude - (cos * xVelocity - sin * yVelocity) * panScalingFactor;// * altitude;

        icamera.setLatitude((float) newLat);
        icamera.setLongitude((float) newLon);
        icamera.apply(false);
    }


    private void zoom(double factor) {
        Log.d(TAG, "zoom");

        icamera.setAltitude(icamera.getAltitude() * factor);
        icamera.apply(false);
    }



}
