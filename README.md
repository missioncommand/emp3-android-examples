# emp3-android-examples
[![Build Status](https://travis-ci.org/missioncommand/emp3-android-examples.svg?branch=master)](https://travis-ci.org/missioncommand/emp3-android-examples)

## About
The emp3-android-examples repository contains code that shows, by examples, how Extensible Mapping Platform should be 
used. There are many examples in this repository and this README document tries to point you to the correct example 
based on your requirements.

Displaying one more Maps in the application.

   - In order to display a map in an application you will need to use either MapFragment or MapView class.
   - You can create these classes programmatically or via a layout configuration file.

   HelloEmp3 example displays a single instance of the map via configuration using MapView Class.
   MultiEngine example displays two instances of the map via configuration using MapFragment Class
   SampleMapFragment example displayes two instances of the map via configuration using MapFragment Class
   SampleMapFragmentPgm example displays two instances of the map programmatically using MapFragment Class
   SampleMapView displays two instances of the map via configuration using MapView Class
   SampleMapViewPgm displayes two instances of the map programmatically using MapView Class.

 
Once  map is displayed, next thing is to display features, draw and edit features, adjust camera, apply various styles 
to the  map etc. These capabilities are demonstrated in Capabilities example. You will find many examples of EMP
usage in the Capabilities project.

There are other examples dedicated to demonstration of specific functionality, as listed below:

    - Camera and WMS
    - Geo Package
    - LookAt
    - Mirror Cache
    - Plotting Mil Std Symbols
    - User Interaction Events

Documentation on these examples is pending and some of them may get deleted while some might get merged. This is work in 
progress.
