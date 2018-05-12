# JCircularGauges

A collection of circular gauges.  Each one extends a Swing JComponent. 

## Gauge Types

Read the header comments in each class for details on how to use and customize.

## Artificial Horizon
Also known as an attitude gauge / gyro horizon.  These are typically seen in aircraft.
They display land with a horizon and sky, visually depicting the vehicle's pitch and roll.  The horizon moves up and down
to indicate pitch and rotates to indicate roll.


## Compass
The compass shows course and bearing needles.  The compass can rotate or can be set for always north up.


## Getting Started

Build and Run using Maven:  "mvn package"
Navigate to the "target" directory

Run: java -cp classes:test-classes com.github.kkieffer.jcirculargauges.JCompassDemo 
Run: java -cp classes:test-classes com.github.kkieffer.jcirculargauges.JArtificialHorizonDemo

Only the Java JRE 1.8 is required.  No other dependencies are needed.

## Demo

Demo classes are available in the test package
![Demo Screenshot](https://github.com/kkieffer/jCircularGauges/blob/master/artificialHorizonExample.jpg "Demo Screenshot")

![Demo Screenshot](https://github.com/kkieffer/jCircularGauges/blob/master/compassExample.jpg "Demo Screenshot")

## License

This project is licensed under the LGPL License - see the [LICENSE.md](LICENSE.md) file for details

