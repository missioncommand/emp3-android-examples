package mil.emp3.examples;

import android.app.Activity;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Overlay;
import mil.emp3.api.enums.Property;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IEmpPropertyList;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.EmpPropertyList;
import mil.emp3.examples.common.TestBase;

public class MultiThreadTest extends TestBase implements Runnable {
    private static String TAG = MultiThreadTest.class.getSimpleName();
    final int maxFeatures = 100;
    final int minWaitMS = 20;

    final MyOverlay o10 = new MyOverlay();
    final MyOverlay o11 = new MyOverlay();
    final MyOverlay o12 = new MyOverlay();
    final MyOverlay o13 = new MyOverlay();
    final MyOverlay o14 = new MyOverlay();
    final MyOverlay o15 = new MyOverlay();
    final MyOverlay o16 = new MyOverlay();
    final MyOverlay o17 = new MyOverlay();
    final MyOverlay o18 = new MyOverlay();
    final MyOverlay o19 = new MyOverlay();


    public MultiThreadTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public void run() {
        if((null == m1) || (null == m2)) {
            testComplete("THIS IS A MULTIMAP TEST, CANNOT BE RUN IN THIS APPLICATION");
            return;
        }
        try {
            test0();
            test1();
            test3();
        } finally {
            testComplete();
        }
    }

    private void test0() {
        startTest("test0");
        final List<IFeature> list = generateMilStdSymbolList(maxFeatures, latitude, longitude);
        Thread addThread = null;
        Thread applyThread = null;
        Thread removeThread = null;

        try {
            m1.addOverlay(o1, true);
            o1.addFeatures(list, true);

            AddTask add = new AddTask(o1, list, minWaitMS);
            ApplyTask apply = new ApplyTask(o1, list, minWaitMS);
            RemoveTask remove = new RemoveTask(o1, list, minWaitMS);

            addThread = new Thread(add);
            applyThread = new Thread(apply);
            removeThread = new Thread(remove);

            addThread.start();
            applyThread.start();
            removeThread.start();
            Thread.sleep(60000);

        } catch (EMP_Exception | InterruptedException e) {
            e.printStackTrace();
        } finally {
            addThread.interrupt();
            applyThread.interrupt();
            removeThread.interrupt();
            try {
                addThread.join();
                applyThread.join();
                removeThread.join();
                Log.d(TAG, "ALL threads terminated");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            endTest();
        }
    }

    private class AddTask implements Runnable {
        private final String TAG = AddTask.class.getSimpleName();
        private List<IFeature> features;
        IOverlay overlay;
        int waitMS;

        AddTask(IOverlay overlay, List<IFeature> features, int waitMS)  {
            this.features = features;
            this.overlay = overlay;
            this.waitMS = waitMS;
        }
        @Override
        public void run() {
            if(Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
                Log.e(TAG, "This is UI thread");
            } else {
                Log.e(TAG, "This is not UI thread");
            }
            Random r = new Random();
            while(!Thread.currentThread().isInterrupted()) {
                int result = r.nextInt(maxFeatures);
                try {
                    overlay.addFeature(features.get(result), true);
                    Thread.sleep(result < waitMS ? waitMS : result);
                } catch (EMP_Exception | InterruptedException e) {
                    e.printStackTrace();
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    private class ApplyTask implements Runnable {
        private final String TAG = ApplyTask.class.getSimpleName();

        private List<IFeature> features;
        IOverlay overlay;
        int waitMS;

        ApplyTask(IOverlay overlay, List<IFeature> features, int waitMS)  {
            this.features = features;
            this.overlay = overlay;
            this.waitMS = waitMS;
        }
        @Override
        public void run() {
            if(Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
                Log.e(TAG, "This is UI thread");
            } else {
                Log.e(TAG, "This is not UI thread");
            }
            Random r = new Random();

            while(!Thread.currentThread().isInterrupted()) {
                int result = r.nextInt(maxFeatures);
                try {
                    deltaMilStdSymbolPosition((MilStdSymbol)features.get(result), .001, .001);
                    features.get(result).apply();
                    Thread.sleep(result < waitMS ? waitMS : result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    private class RemoveTask implements Runnable {
        private final String TAG = ApplyTask.class.getSimpleName();

        private List<IFeature> features;
        IOverlay overlay;
        int waitMS;

        RemoveTask(IOverlay overlay, List<IFeature> features, int waitMS)  {
            this.features = features;
            this.overlay = overlay;
            this.waitMS = waitMS;
        }
        @Override
        public void run() {
            if(Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
                Log.e(TAG, "This is UI thread");
            } else {
                Log.e(TAG, "This is not UI thread");
            }
            Random r = new Random();

            while(!Thread.currentThread().isInterrupted()) {
                int result = r.nextInt(maxFeatures);
                try {
                    overlay.removeFeature(features.get(result));
                    Thread.sleep(result < waitMS ? waitMS : result);
                } catch (EMP_Exception | InterruptedException e) {
                    e.printStackTrace();
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    private void test1() {
        startTest("test1");
        final List<IFeature> list = generateMilStdSymbolList(maxFeatures, latitude, longitude);
        final List<ParentRelation> parentChildList = new ArrayList<>();
        Thread addThread = null;
        Thread applyThread = null;
        Thread removeThread = null;

        try {

            o10.setName("o10");
            o11.setName("o11");
            o12.setName("o12");
            o13.setName("o13");
            o14.setName("o14");
            o15.setName("o15");
            o16.setName("o16");
            o17.setName("o17");
            o18.setName("o18");
            o19.setName("o19");

            m1.addOverlay(o10, true); parentChildList.add(new ParentRelation(m1, o10));
            m1.addOverlay(o11, true); parentChildList.add(new ParentRelation(m1, o11));
            m1.addOverlay(o12, true); parentChildList.add(new ParentRelation(m1, o12));
            m1.addOverlay(o13, true); parentChildList.add(new ParentRelation(m1, o13));
            m1.addOverlay(o14, true); parentChildList.add(new ParentRelation(m1, o14));
            m1.addOverlay(o15, true); parentChildList.add(new ParentRelation(m1, o15));
            m1.addOverlay(o16, true); parentChildList.add(new ParentRelation(m1, o16));
            m1.addOverlay(o17, true); parentChildList.add(new ParentRelation(m1, o17));
            m1.addOverlay(o18, true); parentChildList.add(new ParentRelation(m1, o18));
            m1.addOverlay(o19, true); parentChildList.add(new ParentRelation(m1, o19));

            List<IOverlay> overlays = m1.getAllOverlays();
            int remainingFeatures = list.size();
            int featuresPerOverlay = remainingFeatures / overlays.size();

            for(IOverlay overlay: overlays) {
                for(int ii = 0; ii < featuresPerOverlay; ii++) {
                    overlay.addFeature(list.get(remainingFeatures-1), true);
                    ((MyOverlay) overlay).addMyFeature(list.get(remainingFeatures-1));
                    remainingFeatures--;
                }
            }

            Thread.sleep(waitInterval);

            AddOverlayTask add = new AddOverlayTask(parentChildList, minWaitMS);
            RemoveOverlayTask remove = new RemoveOverlayTask(overlays, minWaitMS * 10);

            addThread = new Thread(add);
            removeThread = new Thread(remove);

            addThread.start();
            removeThread.start();

            Thread.sleep(60000);
        } catch (EMP_Exception  | InterruptedException  e) {
            e.printStackTrace();
        } finally {
            addThread.interrupt();
            removeThread.interrupt();
            try {
                addThread.join();
                removeThread.join();
                Log.d(TAG, "ALL threads terminated");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            endTest();
        }
    }

    private class ParentRelation {
        public IContainer parent;
        public IOverlay child;
        public ParentRelation(IContainer parent, IOverlay child) {
            this.parent = parent;
            this.child = child;
        }
    }

    private class MyOverlay extends Overlay {
        public List<IFeature> features = new ArrayList<>();
        public void addMyFeature(IFeature feature) {
            features.add(feature);
        }
    }

    private class AddOverlayTask implements Runnable {
        private final String TAG = ApplyTask.class.getSimpleName();

        private List<ParentRelation> parentChildList;
        int waitMS;

        AddOverlayTask(List<ParentRelation> parentChildList, int waitMS)  {
            this.parentChildList = parentChildList;
            this.waitMS = waitMS;
        }
        @Override
        public void run() {
            if(Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
                Log.e(TAG, "This is UI thread");
            } else {
                Log.e(TAG, "This is not UI thread");
            }
            Random r = new Random();

            while(!Thread.currentThread().isInterrupted()) {
                int result = r.nextInt(parentChildList.size());
                try {
                    ParentRelation pr = parentChildList.get(result);
                    MyOverlay myOverlay = (MyOverlay) pr.child;
                    if(pr.parent instanceof IMap) {
                        IMap m = (IMap) pr.parent;
                        m.addOverlay(pr.child, true);
                        Log.w(TAG, "Add " + pr.child.getName() + " to " + pr.parent.getName());
                        pr.child.addFeatures(myOverlay.features, true);
                    } else if(pr.parent instanceof IOverlay) {
                        IOverlay o = (IOverlay) pr.parent;
                        o.addOverlay(pr.child, true);
                        Log.w(TAG, "Add " + pr.child.getName() + " to " + pr.parent.getName());
                    }
                    Thread.sleep(result < waitMS ? waitMS : result);
                } catch (EMP_Exception | InterruptedException e) {
                    e.printStackTrace();
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    private class RemoveOverlayTask implements Runnable {
        private final String TAG = ApplyTask.class.getSimpleName();

        private List<IOverlay> overlays;
        int waitMS;

        RemoveOverlayTask(List<IOverlay> overlays, int waitMS)  {
            this.overlays = overlays;
            this.waitMS = waitMS;
        }
        @Override
        public void run() {
            if(Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
                Log.e(TAG, "This is UI thread");
            } else {
                Log.e(TAG, "This is not UI thread");
            }
            Random r = new Random();

            while(!Thread.currentThread().isInterrupted()) {
                int result = r.nextInt(overlays.size());
                try {
                    List<IContainer> parents = overlays.get(result).getParents();
                    if((parents != null) && (0 != parents.size())){
                        if(parents.get(0) instanceof IMap) {
                            IMap m = (IMap) parents.get(0);
                            m.removeOverlay(overlays.get(result));
                        } else if(parents.get(0) instanceof IOverlay) {
                            IOverlay o = (IOverlay) parents.get(0);
                            o.removeOverlay(overlays.get(result));
                        }
                    }
                    Thread.sleep(result < waitMS ? waitMS : result);
                } catch (EMP_Exception | InterruptedException e) {
                    e.printStackTrace();
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    public void test3() {
        try {
            startTest("test3");

            try {
                m1.addOverlay(o1, true);
                m2.addOverlay(o2, true);
                o1.addFeature(p1, true);
                o2.addFeature(p1, true);
                p1.addFeature(p1_1, true);
                o2.addFeature(p1_1, true);
                updateTestStatus(2, 2);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(waitInterval);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final IEmpPropertyList properties = new EmpPropertyList();
                        properties.put(Property.ENGINE_CLASSNAME.getValue(), "mil.emp3.openstreet.MapInstance");
                        properties.put(Property.ENGINE_APKNAME.getValue(), "mil.emp3.openstreetapk");

                        // m2.swapMapEngine("mil.emp3.arcgis.MapInstance", "mil.emp3.arcgis");
                        m2.swapMapEngine(properties);
                        // m2.swapMapEngine("mil.emp3.worldwind.MapInstance", "mil.emp3.worldwind");

                        updateTestStatus(2, 2);
                    } catch (EMP_Exception e) {
                        e.printStackTrace();
                    }
                }
            });


            Thread.sleep(large_waitInterval);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final IEmpPropertyList properties = new EmpPropertyList();
                        properties.put(Property.ENGINE_CLASSNAME.getValue(), "mil.emp3.worldwind.MapInstance");
                        properties.put(Property.ENGINE_APKNAME.getValue(), "mil.emp3.worldwind");


                        m2.swapMapEngine(properties);
                        updateTestStatus(2, 2);
                    } catch (EMP_Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            Thread.sleep(large_waitInterval);

            SwapEngineTask set = new SwapEngineTask("mil.emp3.openstreet.MapInstance", "mil.emp3.openstreetapk");
            Thread setThread = new Thread(set);
            setThread.start();
            setThread.join();

            Thread.sleep(large_waitInterval);
            SwapEngineTask set2 = new SwapEngineTask("mil.emp3.worldwind.MapInstance", "mil.emp3.worldwind");
            Thread set2Thread = new Thread(set2);
            set2Thread.start();
            set2Thread.join();

            Thread.sleep(waitInterval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    private class SwapEngineTask implements Runnable {
        private String engineName;
        private String engineApkName;

        SwapEngineTask(String engineName, String engineApkName) {
            this.engineName = engineName;
            this.engineApkName = engineApkName;
        }
        @Override
        public void run() {
            final IEmpPropertyList properties = new EmpPropertyList();
            properties.put(Property.ENGINE_CLASSNAME.getValue(), engineName);
            properties.put(Property.ENGINE_APKNAME.getValue(), engineApkName);


            try {
                m2.swapMapEngine(properties);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        }
    }
}
