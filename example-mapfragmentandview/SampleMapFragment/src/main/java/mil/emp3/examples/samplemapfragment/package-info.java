/**
 * <p>
 * <H1>Getting Started with EMP for Android</H1>
 * </p>
 * <p>
 * Extensible Mapping Platform (EMP) API supports WorldWind Android map engine. WorldWind Android source code (downloaded from git-hub) is stored in
 * worldwind-android-sdk repository on DI2E stash. The worldwind-android-sdk jenkins job produces two artifacts, which are stored on DI2E
 * Private_EMP_Releases and Releases (Application developers should refer to this repository) repository.
 * </p>
 *
 * <BL>
 * <LI>worldwind-android-sdk.aar: (GAV - mil.army.sec.smartClient:worldwind-android-sdk:version) An Android archive that is used by applications</LI>
 * <LI>worldwind-android-sdk.apk: (GAV - mil.army.sec.smartClient:worldwind-android-sdk:version) Sample application that exercises some of the features of WorldWind map engine </LI>
 * </BL>
 *
 * <p>
 * For applications using EMP API additional artifacts are required in order to use the WorldWind map. These artifacts are also stored in the previously mentioned
 * repositories. The list is followed by notes describing usage guidelines.
 * </P>
 *
 * <BL>
 * <LI>common-map-geospatial-notation: This has the Geospatial data model built from a specification found at https://github.com/CMAPI/common-map-geospatial-notation.git</LI>
 * <LI>emp3-android-sdk.jar: This is the basic EMP API that includes interfaces for drawing to the map and receiving user interaction events from the map.</LI>
 * <LI>emp3-android-sdk-view.aar:This has the EMP MapView and MapFragment objects that can be referred to in a XML layout or programmatically instantiated.</LI>
 * <LI>emp3-android-sdk-core.aar: This has the EMP core components implementation, applications should never directly use any components from this aar.</LI>
 * <LI>emp3-android-sdk-apk.apk:This contains the emp3-android-sdk.jar,  common-map-geospatial-notation .jar, mirrorcache-api and, mirrorcache-mirrorables. It is used in the COEv3 operating environment
 * to inject interface and implementation classes in the system class loader</LI>
 * <LI>worldwind-android-sdk.aar: This is the WorldWind mapping engine delivered by NASA. Applications never directly use this artifact.</LI>
 * <LI>emp3-android-worldwind.aar: This has the glue code that wraps the WorldWind SDK (worldwind-android-sdk.aar). Applications never directly use this artifact
 * instead it is used by the EMP API to operate on the WorldWind map.</LI>
 * <LI>emp3-android-worldwind.apk: This android application contains the emp3-android-worldwind.aar and worldwind-android-sdk.aar.</LI>
 * <LI>mil-sym-android-renderer.aar: Military symbol render-er</LI>
 * <LI>mirrorcache-api: This artifact encapsulates the classes and interfaces required to interact with the MirrorCache Service.</LI>
 * <LI>mirrorcache-mirrorables: This artifact contains wrapper classes for each object capable of being cached by the MirrorCache.</LI>
 * <LI>mirrorcache-service: This is the Android Bound Service for MirrorCache.</LI>
 * </BL>
 *
 * <p>
 * <H1>How do I set up the gradle build scripts to use EMP</H1>
 * </p>
 * <BL>
 * <LI>compile ("mil.army.sec.smartClient:emp3-android-sdk-view:SDK_VERSION@aar")  { transitive = true }</LI>
 * <LI>compile (group: 'mil.army.sec.smartClient', name: 'emp3-android-sdk-core', version: "$SDK_VERSION", ext: 'aar') { transitive = true }</LI>
 * </BL>
 *
 * <p>
 * <H1>How do I refer to the map engine</H1>
 * </p>
 * <p>
 * In the above description we didn't provide a compile line for emp3-android-worldwind.* artifacts because it depends on which of the two use cases application
 * chooses to implement. Following paragraphs provide description of these two use cases.
 * </p>
 * <p>
 * USE CASE 1: Loading the WorldWind map engine and the EMP provided glue code at run time from a separate Android application, namely emp3-android-worldwind.apk.
 * In this use case, application doesn't need ANY reference to either worldwind-android-sdk.aar or emp3-android-worldwind.aar. The emp3-android-worldwind.apk should be
 * installed as any other Android application before user's application can start and display the WorldWind map. The EMP API will search for the APK at run time and class
 * load the required classes. EMP API takes care of the .so files and resources that may exists in the emp3-android-worldwind.apk. This is the preferred use case.
 * emp3-android-worldwind.apk can be downloaded from DI2E/Nexus mil.army.sec.smartClient:emp3-android-worldwind:version.
 * </p>
 * <p>
 * USE CASE 2: Loading the WorldWind map engine and the EMP provided glue code at run time from within the client application. In this scenario application will need to
 * compile with worldwind-android-sdk.aar and emp3-android-worldwind.aar; required gradle dependency is:
 * </p>
 * <BL>
 * <LI>compile ("mil.army.sec.smartClient:emp3-android-worldwind:SDK_VERSION@aar") { transitive = true }</LI>
 * </BL>
 * <H1>Which APK to load</H1>
 * <p>
 * In the above two use cases how does the EMP API figure out which APK to load the classes from and what are the class names? In your layout for the MapFragment or
 * MapView you will need to provide that information via two custom attributes, map_engine_name and map_engine_apk_name,  as shown below:
 * </p>
 * <ul>
 * <li>map_engine_name is the name of the class that implements IMapInstance interface.</li>
 * <li>map_name is the name of the map instance, this is required and must be unique</li>
 * </ul>
 * <pre>
 *    &lt;mil.emp3.api.MapView xmlns:android="http://schemas.android.com/apk/res/android"
 *        xmlns:app="http://schemas.android.com/apk/res-auto"
 *        android:id="@+id/map"
 *        app:map_engine_name="mil.emp3.worldwind.MapInstance"
 *        app:map_name="map1"
 *        android:layout_width="fill_parent"
 *        android:layout_height="wrap_content"
 *        android:layout_weight="1"/&gt;
 * </pre>
 * <pre>
 *      map = (IMap) findViewById(R.id.map);
 *      try {
 *          map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
 *          public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
 *              Log.d(TAG, &quot;mapStateChangeEvent map&quot; + mapStateChangeEvent.getNewState());
 *              // If new map state is MAP_READY then you can begin to use the Map.
 *          }
 *          });
 *      } catch (EMP_Exception e) {
 *          e.printStackTrace();
 *      }
 * </pre>
 * </p>
 *
 * <H1>Where are the examples</H1>
 * <p>
 * EMP examples are stored in emp3-android-examples repository. There are four basic examples that show how an application can include a MapView or
 * MapFragment in the User interface either statically (via xml layout files) or dynamically (via java programming).
 * </p>
 * <ul>
 * <li>example-mapfragmentandview-SampleMapFragment - shows how to use MapFragment using Android layout files and presumes that map engine APK is already installed </li>
 * <li>example-mapfragmentandview-SampleMapFragmentPgm - shows how to use MapFragment dynamically in an application and presumes that map engine APK is already installed. </li>
 * <li>example-mapfragmentandview-SampleMapView - shows how to use MapView using Android layout files in an application  and supplies the map engine artifacts in the build script. </li>
 * <li>example-mapfragmentandview-SampleMapViewPgm - shows how to use MapView dynamically in anapplication and shows how you can supply an artifact for one map engine but
 * have another loaded from an Android APK. </li>
 * </ul>
 * <p>These examples show multiple ways of using the MapView and MapFragment in an application. Once the Map is displayed and a reference to IMap is acquired
 * , application needs to display and manage overlays/features on the Map.  All the tests in SampleMapFragement, SampleMapFragmentPgm,
 * SampleMapView, and SampleMapViewPgm are currently disables as they are being reworked. In the mean time please build and install the Capability Tester (emp3-android repository) application,
 * project test-vandv, to checkout the EMP capabilities. Code behind the Capability Tester is also a good place to find out how EMP API can be used. Examples in the
 * emp3-android-examples repository will be eventually reorganized to follow the Capability Tester paradigm.</p>
 * <H1> EMP life cycle</H1>
 * <p>Application must manage EMP life cycle via Emp3LifeCycleManager to avoid memory leaks. Following documentation is repeated here from the
 * Emp3LifeCycleManager class.</p>
 * <pre>
 * https://developer.android.com/guide/topics/resources/runtime-changes.html
 *
 * Some device configurations can change during runtime (such as screen orientation, keyboard availability, and language). When such a change occurs,
 * Android restarts the running Activity (onDestroy() is called, followed by onCreate()). The restart behavior is designed to help your application
 * adapt to new configurations by automatically reloading your application with alternative resources that match the new device configuration.

 * To properly handle a restart, it is important that your activity restores its previous state through the normal Activity lifecycle, in which Android
 * calls onSaveInstanceState() before it destroys your activity so that you can save data about the application state. You can then restore the
 * state during onCreate() or onRestoreInstanceState().
 *
 * NOTE that applications can prevent Android from restarting an activity for some events by setting correct flags in the activity tag of
 * the manifest: android:configChanges="screenSize|orientation"
 *
 * Above text is straight from the link on the first line of this comment. Following is specific to the EMP3 API. When using EMP3 API applications have
 * two options:
 *     1. Destroy the data held by EMP3 API and take full responsibility for recreating the state i.e. map engine, camera, overlays, features, WMS
 *     2. Let EMP3 hold the current state of the map and recreate it on activity restart
 *
 * REQUIRED: In order for any of the options to work properly without any memory leaks applications need to take following steps:
 *
 *     1. Every instance of the Map should be named. As soon as application gets a reference to the IMap, application should invoke the setName method
 *          with unique name for each Map instance.
 *     2. Application Main Activity should override the on saveInstanceState method. In that method application should invoke the
 *           Emp3LifeCycleManager.onSaveInstanceState(boolean keepState) method.
 *           - To take on the full responsibility for map state restoration invoke the method with keepState = false
 *           - To let the EMP3 API restore the state invoke the method with keepState = true.
 *
 *     3. If application has chosen to let the EMP API restore the state then it needs to detect that activity restarted and avoid performing any
 *           initialization of the map other than invoking setName on the map.
 *
 *     4. If application is invoking the map from a layout file and application has set the "map_name" attribute then application doesn't have to
 *           set the name either.
 *     5. Applications must override onPause and onResume in main activity and invoke onPause and onResume methods of this class before invoking
 *           corresponding super class methods.
 *
 * @param keepState - set to true to restores Map Data on activity restart (Overlays, Features, MapService, Listeners)
 *                  - set to false to clean Map Data on activity restart, Application will restore from its own cache.
 *
 * </pre>
 */
package mil.emp3.examples.samplemapfragment;