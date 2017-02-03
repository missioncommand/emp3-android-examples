package mil.emp3.examples;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;

import java.util.Locale;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.examples.common.TestBase;

/**
 * This test loads each instance of the map with 10000 distinct features and moves them around. Test can only be terminated by
 * killing the application. You can modify this to stop the test after a specified interval.
 */
public class PerformanceTest extends TestBase implements Runnable {
    private static String TAG = PerformanceTest.class.getSimpleName();
    private boolean alertDialogIsUp = false;
    PerformanceTestThread pt1 = null;
    PerformanceTestThread pt2 = null;
    Thread testThread = null;
    public PerformanceTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions;
        if(null == m2) {
            actions = new String[3];
            actions[0] = "zIn-1";
            actions[1] = "zOut-1";
            actions[2] = "Stp-1";
        } else {
            actions = new String[6];
            actions[0] = "zIn-1";
            actions[1] = "zOut-1";
            actions[2] = "Stp-1";
            actions[3] = "zIn-2";
            actions[4] = "zOut-2";
            actions[5] = "Stp-2";
        }
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        String[] actions;
        if(null == m2) {
            actions = new String[4];
            actions[0] = "+MDT-1";
            actions[1] = "-MDT-1";
            actions[2] = "+FDT-1";
            actions[3] = "-FDT-1";

        } else {
            actions = new String[8];
            actions[0] = "+MDT-1";
            actions[1] = "-MDT-1";
            actions[2] = "+FDT-1";
            actions[3] = "-FDT-1";
            actions[4] = "+MDT-2";
            actions[5] = "-MDT-2";
            actions[6] = "+FTD-2";
            actions[7] = "-MDT-2";
        }

        return actions;
    }

    @Override
    public void actOn(String userAction) {

        if (userAction.equals("zIn-1")) {
            ICamera camera = m1.getCamera();
            camera.setAltitude(camera.getAltitude() * .9);
            camera.apply(false);
        } else if(userAction.equals("zOut-1")) {
            ICamera camera = m1.getCamera();
            camera.setAltitude(camera.getAltitude() * 1.10);
            camera.apply(false);
        } else if(userAction.equals("Stp-1")) {
            if(null != pt1) {
                pt1.stopTest();
                pt1 = null;
            }
            if((null == pt1) && (null == pt2)) {
                testThread.interrupt();
            }
        } else if (userAction.contentEquals("zIn-2")){
            ICamera camera = m2.getCamera();
            camera.setAltitude(camera.getAltitude() * .9);
            camera.apply(false);
        } else if(userAction.equals("zOut-2")) {
            ICamera camera = m2.getCamera();
            camera.setAltitude(camera.getAltitude() * 1.10);
            camera.apply(false);
        } else if(userAction.equals("Stp-2")) {
            if(null != pt2) {
                pt2.stopTest();
                pt2 = null;
            }
            if((null == pt1) && (null == pt2)) {
                testThread.interrupt();
            }
        } else if(userAction.equals("+MDT-1")) {
            if(m1.getFarDistanceThreshold() > m1.getMidDistanceThreshold()+5000.0) {
                m1.setMidDistanceThreshold(m1.getMidDistanceThreshold() + 5000.0);
            }
        }  else if(userAction.equals("-MDT-1")) {
            if((m1.getMidDistanceThreshold() > 5000.0) && (m1.getFarDistanceThreshold() > (m1.getMidDistanceThreshold() - 5000.0))) {
                m1.setMidDistanceThreshold(m1.getMidDistanceThreshold() - 5000.0);
            }
        } else if(userAction.equals("+FDT-1")) {
            m1.setFarDistanceThreshold(m1.getFarDistanceThreshold()+5000.0);
        } else if(userAction.equals("-FDT-1")) {
            if((m1.getFarDistanceThreshold() > 5000.0) && ((m1.getFarDistanceThreshold() - 5000.0) > m1.getMidDistanceThreshold())) {
                m1.setFarDistanceThreshold(m1.getFarDistanceThreshold() - 5000.0);
            }
        } else if(userAction.equals("+MDT-2")) {
            if(m2.getFarDistanceThreshold() > m2.getMidDistanceThreshold()+5000.0) {
                m2.setMidDistanceThreshold(m2.getMidDistanceThreshold() + 5000.0);
            }
        } else if(userAction.equals("-MDT-2")) {
            if((m2.getMidDistanceThreshold() > 5000.0) && (m2.getFarDistanceThreshold() > (m2.getMidDistanceThreshold() - 5000.0))) {
                m2.setMidDistanceThreshold(m2.getMidDistanceThreshold() - 5000.0);
            }
        } else if(userAction.equals("+FDT-2")) {
            m2.setFarDistanceThreshold(m2.getFarDistanceThreshold()+5000.0);
        } else if(userAction.equals("-FDT-2")) {
            if((m2.getFarDistanceThreshold() > 5000.0) && ((m2.getFarDistanceThreshold() - 5000.0) > m2.getMidDistanceThreshold())) {
                m2.setFarDistanceThreshold(m2.getFarDistanceThreshold() - 5000.0);
            }
        }else {
            Log.e(TAG, "Unsupported action " + userAction);
        }
    }

    @Override
    public void run() {
        try {
            test0();
        } catch (Exception e) {
            Log.d(TAG, "run:" , e);
        } finally {
            testComplete();
        }
    }

    private void test0() {
        try {
            startTest("test0");
            Thread.sleep(waitInterval);

            try {

                m1.addOverlay(o1, true);
                m1.setFarDistanceThreshold(120000);
                m1.setMidDistanceThreshold(100000);
                m1.setMilStdLabels(MilStdLabelSettingEnum.ALL_LABELS);
                if(null == m2) {
                    pt1 = new PerformanceTestThread(m1, 10000, false, false);
                } else {
                    pt1 = new PerformanceTestThread(m1, 5000, false, false);
                }

                if(null != m2) {
                    m2.addOverlay(o2, true);
                    m2.setFarDistanceThreshold(120000);
                    m2.setMidDistanceThreshold(100000);
                    m2.setMilStdLabels(MilStdLabelSettingEnum.ALL_LABELS);
                    pt2 = new PerformanceTestThread(m2, 5000, false, false);
                }

                pt1.start();
                if(null != pt2) {
                    pt2.start();
                }
                testThread = Thread.currentThread();
                while(!Thread.interrupted()) {
                    try {
                        Thread.sleep(large_waitInterval * 10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                if(null != pt1) {
                    pt1.stopTest();
                }
                if(null != pt2) {
                    pt2.stopTest();
                }
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    class PerformanceTestThread extends Thread {
        private long COUNT_PER_INTERVAL = 200;
        private long UPDATE_INTERVAL = 200;
        private String[] aTrackSymbol = {"SFAPMFF--------", "SFAPMFA--------", "SFAPMFJ--------", "SFAPMFL--------", "SFAPMHQ--------", "SFAPMHO--------", "SHAPMFF--------", "SHAPMFA--------", "SHAPMFJ--------", "SHAPMFL--------", "SHAPMHQ--------", "SHAPMHO--------", "SNAPCF---------", "SFAPCF---------", "SFAPCH---------"};
        private int iTrackSymbolCount = this.aTrackSymbol.length;
        private MilStdSymbol.Affiliation[] aAffiliations = {MilStdSymbol.Affiliation.FRIEND, MilStdSymbol.Affiliation.NEUTRAL, MilStdSymbol.Affiliation.HOSTILE};
        private final int iAffCount = aAffiliations.length;
        private int iCount = 2000;
        private boolean bAiffChange = false;
        private java.util.List<IFeature> oFeatureList = new java.util.ArrayList<>();
        private boolean bContinue = true;
        private final int TIME_LIST_SIZE = 100;
        private int iTimelistIndex = 0;
        private long[] alTimeList = new long[TIME_LIST_SIZE];
        private double dTimeSum = 0.0;
        private int iTimeSamples = 0;
        private String sMessage;
        private boolean bBatchUpdate = false;
        private IOverlay oOverlay;
        private ICamera oCamera;
        private IMap oMap;
        public PerformanceTestThread(IMap map,int count, boolean bAffChg, boolean bBatch) {
            this.oMap = map;
            this.iCount = count;
            this.bAiffChange = bAffChg;
            this.bBatchUpdate = bBatch;
            this.oOverlay = map.getAllOverlays().get(0);
            oCamera = map.getCamera();
            map.setFarDistanceThreshold(120000);
            map.setMidDistanceThreshold(100000);
        }

        protected IGeoPosition getRandomCoordinate() {
            IGeoPosition oPos = new GeoPosition();
            double dTemp;

            dTemp = oCamera.getLatitude() + (3 * Math.random()) - 1.5;
            oPos.setLatitude(dTemp);
            dTemp = oCamera.getLongitude() + (3 * Math.random()) - 1.5;
            oPos.setLongitude(dTemp);
            //oPos.setLongitude((Math.random() * 360.0) - 180.0);
            //oPos.setLatitude((Math.random() * 180.0) - 90);
            //oPos.setAltitude(Math.random() * 16000.0);
            oPos.setAltitude(0);

            return oPos;
        }

        private void createTracks() {
            java.util.List<IGeoPosition> oPosList;
            int iNextTrackSymbol = 0;
            int iNextAffiliation = 0;
            String sSymbolCode;
            int iDirectionOfMovement = 0;
            int iSpeed = 600;

            try {
                oOverlay.removeFeatures(oOverlay.getFeatures());
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            for (int iIndex = 0; iIndex < this.iCount; iIndex++) {
                try {
                    oPosList = new java.util.ArrayList<>();
                    oPosList.add(getRandomCoordinate());

                    sSymbolCode = aTrackSymbol[iNextTrackSymbol];
                    iNextTrackSymbol++;
                    iNextTrackSymbol %= this.iTrackSymbolCount;

                    // Allocate the new MilStd Symbol with a MilStd version and the symbol code.
                    mil.emp3.api.MilStdSymbol oSPSymbol = new mil.emp3.api.MilStdSymbol(IGeoMilSymbol.SymbolStandard.MIL_STD_2525C, sSymbolCode);

                    // Set the symbols affiliation.
                    oSPSymbol.setAffiliation(this.aAffiliations[iNextAffiliation]);
                    iNextAffiliation++;
                    iNextAffiliation %= this.iAffCount;

                    // Set the echelon.
                    oSPSymbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.UNIT, MilStdSymbol.Echelon.UNIT);
                    // Set the symbols altitude mode.
                    oSPSymbol.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
                    //oSPSymbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.HQ_BRIGADE);

                    // Set the position list with 1 position.
                    oSPSymbol.setPositions(oPosList);

                    // Give the feature a name.
                    oSPSymbol.setName("Unit " + iIndex);

                    // Set Direction of movement.
                    oSPSymbol.setModifier(IGeoMilSymbol.Modifier.DIRECTION_OF_MOVEMENT, iDirectionOfMovement + "");
                    iDirectionOfMovement += 10;
                    iDirectionOfMovement %= 360;

                    // Set Speed.
                    oSPSymbol.setModifier(IGeoMilSymbol.Modifier.SPEED, iSpeed + "");
                    oOverlay.addFeature(oSPSymbol, true);
                } catch (EMP_Exception e) {
                    e.printStackTrace();
                }
            }


            oFeatureList = oOverlay.getFeatures();

            for (int iIndex = 0; iIndex < this.TIME_LIST_SIZE; iIndex++) {
                this.alTimeList[iIndex] = 0;
            }
        }

        private void removeTracks() {
            try {
                oOverlay.removeFeatures(oFeatureList);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            mil.emp3.api.MilStdSymbol oSPSymbol;
            int iNextFeature = 0;
            long lStartTimestamp;
            long lDeltaTime = 0;
            IGeoPosition oPos, oNewPos;
            int iDOM;
            double dSpeed;
            double dDistance;
            double dRandom;
            long lLastMoved = System.currentTimeMillis();
            long dWaitTime  = this.UPDATE_INTERVAL; // msec
            java.util.List<IFeature> oBatchList = new java.util.ArrayList<>();

            this.createTracks();

            while (bContinue) {
                try {
                    oBatchList.clear();
                    sleep(dWaitTime);
                    lStartTimestamp = System.currentTimeMillis();
                    for (int iIndex = 0; iIndex < this.COUNT_PER_INTERVAL; iIndex++) {
                        oSPSymbol = (mil.emp3.api.MilStdSymbol) this.oFeatureList.get(iNextFeature);
                        oPos = oSPSymbol.getPosition();
                        iDOM = Integer.parseInt(oSPSymbol.getStringModifier(IGeoMilSymbol.Modifier.DIRECTION_OF_MOVEMENT));
                        dSpeed = (double) Integer.parseInt(oSPSymbol.getStringModifier(IGeoMilSymbol.Modifier.SPEED));
                        dDistance = dSpeed * 1609.0 * (double)(lStartTimestamp - lLastMoved) / 3.6e6;

                        oNewPos = GeoLibrary.computePositionAt((double) iDOM, dDistance, oPos);
                        oPos.setLatitude(oNewPos.getLatitude());
                        oPos.setLongitude(oNewPos.getLongitude());
                        dRandom = Math.random();
                        if (dRandom <= 0.2) {
                            // Make the heading 45Deg CCW
                            iDOM += 350;
                            iDOM %= 360;
                            oSPSymbol.setModifier(IGeoMilSymbol.Modifier.DIRECTION_OF_MOVEMENT, iDOM + "");
                        } else if (dRandom >= 0.8) {
                            // Make the heading 45Deg CW
                            iDOM += 10;
                            iDOM %= 360;
                            oSPSymbol.setModifier(IGeoMilSymbol.Modifier.DIRECTION_OF_MOVEMENT, iDOM + "");
                        }

                        if (this.bAiffChange) {
                            dRandom = Math.random();
                            if (dRandom <= 0.25) {
                                // Change the Affilication.
                                int iAffIndex = (int) Math.floor(Math.random() * this.iAffCount) % this.iAffCount;
                                oSPSymbol.setAffiliation(this.aAffiliations[iAffIndex]);
                            }
                        }

                        if (this.bBatchUpdate) {
                            oBatchList.add(oSPSymbol);
                        } else {
                            oSPSymbol.apply();
                        }

                        iNextFeature++;
                        iNextFeature %= this.iCount;
                        if (iNextFeature == 0) {
                            lLastMoved = lStartTimestamp;
                            break;
                        }
                    }

                    if (this.bBatchUpdate) {
                        try {
                            for (IFeature oSymbol: oBatchList) {
                                oSymbol.apply();
                            }
                            //this.oMainActivity.oRootOverlay.addFeatures(oBatchList, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    // Cal the time it took to update the COUNT_PER_INTERVAL units.
                    lDeltaTime = System.currentTimeMillis() - lStartTimestamp;
                    dWaitTime = (lDeltaTime < this.UPDATE_INTERVAL)? this.UPDATE_INTERVAL - lDeltaTime: 50;
                    if (this.alTimeList[this.iTimelistIndex] != 0) {
                        iTimeSamples--;
                        this.dTimeSum -= (double) this.alTimeList[this.iTimelistIndex];
                    }
                    iTimeSamples++;
                    this.alTimeList[this.iTimelistIndex] = lDeltaTime;
                    this.dTimeSum += (double) lDeltaTime;
                    this.iTimelistIndex++;
                    this.iTimelistIndex %= this.TIME_LIST_SIZE;
                    sMessage = String.format("Update %1$,4d features in %2$,4d msec. Avg of %3$,4d = %4$8.3f msec", this.COUNT_PER_INTERVAL, lDeltaTime, iTimeSamples,
                            this.dTimeSum / (double) iTimeSamples);

                    oCamera = oMap.getCamera();

                    String labels = oMap.getMilStdLabels().toString();

                    sMessage = String.format(Locale.US, "%1s L:N:A %2$6.3f %3$6.3f %4$6.0f F:M %5$6.1f %6$6.1f %7$d ", oMap.getName(),
                            oCamera.getLatitude(), oCamera.getLongitude(), oCamera.getAltitude(),
                            oMap.getFarDistanceThreshold(), oMap.getMidDistanceThreshold(),
                            iCount);
                    sMessage += oMap.getMilStdLabels().toString();
                    String uMessage = String.format(" U:%1$d in %2$d ms", this.COUNT_PER_INTERVAL, lDeltaTime);
                    sMessage += uMessage;
                    updateStatus(sMessage);

                } catch (InterruptedException Ex) {

                }
            }
            this.removeTracks();
        }

        public void stopTest() {
            this.bContinue = false;
            this.interrupt();
        }
    }
}
