/*
Copyright (C) 2018 K. Kieffer

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/

package com.github.kkieffer.jcirculargauges;

import static java.awt.BasicStroke.CAP_SQUARE;
import static java.awt.BasicStroke.JOIN_MITER;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;

/**
 * This is an artifical horizon gauge, also known as an attitude gauge / gyro horizon.  These are typically seen in aircraft.
 * They display land with a horizon and sky, visually depicting the vehicle's pitch and roll.  The horizon moves up and down
 * to indicate pitch and rotates to indicate roll.
 * 
 * This gauge uses the actual angle to represent roll, for instance 30 degrees rotation on the dial is 30 degrees roll.  The
 * pitch sensitivity can be adjusted.  The sensitivity is a multiplier against the pitch to translation value, where 90 degrees
 * pitch up is the top of the gauge and -90 degrees is the bottom of the gauge.  The default sensitivity is 1.0.  As an example,
 * if the specified pitch is 45 degrees up, then the horizon will be shown below the center of the gauge, at a distance half that
 * of the radius of the gauge. For a vehicle going straight up (+90), only sky would be shown.  Since these angles are not 
 * common, the gauge sensitivity can be increased.  For instance a value of 1.5 will have 45 degrees pitch up 3/4 of the way along
 * the gauge radius, thus exaggerating the effect of pitch on the horizon. Conversely, a negative value can be used to reduce sensitivity.
 * 
 * The gauge scales with preferred size, although too small of a size crowds the text.  Minor ticks are drawn for roll if the gauge
 * is large enough
 * 
 * The bezel, ground, and sky colors can be customized.  The default is silver, brown, and blue.
 * 
 *  Note that once the artificial horizon falls outside the gauge, the perspective lines also disappear.  The entire gauge
 * fills with either sky or ground, and pitch cannot be determined.  Roll is still measured.
 * 
 * @author kkieffer
 */
public class JArtificialHorizonGauge extends JCircularGauge {
    private static final long serialVersionUID = -7690241755303029242L;
	private static final Color BROWN = new Color(160, 90, 70);  //default ground
    private static final Color BLUE = new Color(175, 225, 255); //default sky
    
    private static final double DEFAULT_PITCH_SENSITIVITY = 1.0;  //default sensitivity
    
    
    private double angle;
    private double pitchSensitivity;
    private double translateFactor;
    private Color groundColor;
    private Color skyColor;
    private Color indicatorColor;

    protected int yawRadius;
    protected int rollPitchRadius;
    
    //jcompass
    private double course;
    private Color courseNeedleColor;
    private boolean northUp;
    private double bearing;
    protected boolean thickerCardinalLine = true;
    protected double tickScale = 0.1;  //fraction of the inside radius for the length of the tick
    double bezelBuffer = 80; // width of black radius between bezel and roll/pitch/yaw indicator (used to display compass heading
    // TODO remove bezelBuffer
    
    
    /**
     * Create the JArtificialHorizon gauge with default parameters
     */
    public JArtificialHorizonGauge() {
        this(DEFAULT_PITCH_SENSITIVITY);
    }
    
    /**
     * Create the JArtificialHorizon gauge with default parameters
     * @param pitchSensitivity the pitch sensitivity, where 1.0 corresponds to 45 degrees pitch at half the radius of the gauge
     */
    public JArtificialHorizonGauge(double pitchSensitivity) {
        this.pitchSensitivity = pitchSensitivity;
        setAttitude(0.0, 0.0);
        setColors(null, null, null, null);
    }
    
    
    /**
     * Customize the gauge colors
     * @param indicatorColor colors of ticks and labels
     * @param bezelColor the gauge color, null for default
     * @param groundColor the ground color, null for default
     * @param skyColor the sky color, null for default
     */
    public final void setColors(Color indicatorColor, Color bezelColor, Color groundColor, Color skyColor) {
        super.setColors(bezelColor, null);
        this.indicatorColor = indicatorColor == null ? Color.BLACK : indicatorColor;
        this.groundColor = groundColor == null ? BROWN : groundColor;
        this.skyColor = skyColor == null ? BLUE : skyColor;
        courseNeedleColor = Color.RED;
    }
   
    /**
     * Update the pitch and roll values, in degrees
     * @param roll positive value roll to right / starboard
     * @param pitch positive value is pitch up
     */
    public final void setAttitude(double roll, double pitch) {
        angle = Math.toRadians(roll);
        translateFactor = (-pitch / 90.0) * pitchSensitivity;
        repaint();
    }

    /*
     * method overload; additional parameter of yaw
     */
    public final void setAttitude(double roll, double pitch, double yaw) {
        angle = Math.toRadians(roll);
        translateFactor = (-pitch / 90.0) * pitchSensitivity;
        setCourse(yaw);
        repaint();
    }
    

    protected static void drawCardinalLetter(Graphics2D g2d, String letter, int yOffset) {
        Rectangle2D stringBounds = g2d.getFontMetrics().getStringBounds(letter, g2d);
        g2d.drawString(letter, (int)-stringBounds.getCenterX(), yOffset + (int)stringBounds.getMaxY());
    }
   private void drawBackground(Graphics2D g2d, double backgroundRadius, double translate) {
       //Edit: add black compass background
       g2d.setColor(Color.BLACK);
       //g2d.fillOval((int)-insideRadius - 50, (int)-insideRadius - 50, (int)insideRadius*2 + 100, (int)insideRadius*2 + 100);
       g2d.fillOval((int)-outsideRadius, (int)-outsideRadius, (int)outsideRadius*2, (int)outsideRadius*2);
       
       //If pitching down (horizon goes up), the draw the ground first, otherwise dry the sky
       g2d.setColor(translate > 0 ? groundColor : skyColor);
      
       //Fill the gauge the background (sky or ground)
       g2d.fillOval((int)-backgroundRadius, (int)-backgroundRadius, (int)backgroundRadius*2, (int)backgroundRadius*2);
           
       //AffineTransform centerDialTransform = g2d.getTransform();

       if (Math.abs(translate) <= backgroundRadius) {
       
           //Now switch to draw the other one
           g2d.setColor(translate > 0 ? skyColor : groundColor);

           //Value d is half the length of the new horizon.  If pitch is zero, then d == radius, otherwise d is smaller than radius
           double d = Math.sqrt(Math.pow(backgroundRadius, 2) -  Math.pow(translate, 2));

           //Theta is the angle from the line that intersects the origin and horizon at edge of the gauge, and the radius perpendicular to the horizon
           int theta = (int)Math.toDegrees(Math.PI/2 - Math.asin(d/backgroundRadius));

           //Now, draw an arc that stretches from the radius at one horizon intersection with gauge end, to the other
           int arcstart;
           int arclen;
           if (translate > 0) {
               arcstart = (int)Math.round(Math.toDegrees(-angle) + theta);
               arclen = 180 - (2 * theta);               
           } else {
               arcstart = (int)Math.round(Math.toDegrees(-angle) - theta);
               arclen = -180 + (2 * theta);
           }

           int r = (int)Math.round(backgroundRadius);
           g2d.fillArc(-r, -r, r*2, r*2, arcstart, arclen);

           //Once we've filled the space we are left with two right triangles to fill
           //If pitching down, fill in the ground triangles, otherwise sky triangles
           g2d.setColor(translate > 0 ? groundColor : skyColor);

           //Rotate through the roll angle
           g2d.rotate(angle);

           //Now fill the two triangles. Normally, the triangles come to a point in the origin, but due to rounding effects, this
           //has paint artifacts.  So by extending that point all the way to the gauge end, we can hide those while not affecting
           //anything.  This is a difficult concept to explain but to illustrate this, you can change the color to something else
           //to illustrate the triangles being drawn.
           int t = (int)Math.round(translate);
           int l = (int)Math.round(d);
           int z = (int)backgroundRadius * (translate < 0 ? -1 : 1);
           Polygon p = new Polygon(new int[]{0, -l, l}, new int[]{z, -t, -t},  3);
           g2d.fillPolygon(p);


           g2d.setColor(indicatorColor);

           //Draw dashed perspective lines from the horizon to the origin
           g2d.setStroke(new BasicStroke(1, CAP_SQUARE, JOIN_MITER, 10.0f, new float[]{5.0f}, 0.0f));
           g2d.drawLine(0, 0, (int)(l*0.8), -t);
           g2d.drawLine(0, 0, (int)(-l*0.8), -t);


           //Draw dashed perspective lines on the ground
           g2d.setStroke(new BasicStroke(2, CAP_SQUARE, JOIN_MITER, 10.0f, new float[]{8.0f}, 0.0f));
           g2d.setColor(groundColor.darker());
           g2d.translate(0, -translate);
           //Perspective lines
           for (int i=-20; i<0; i+=10) {

               int px = (int)(-i*d/40 * Math.cos(Math.toRadians(i)));
               int py = (int)(-i*d/40 * -Math.sin(Math.toRadians(i)));

               g2d.drawLine(0, 0, px, py);
               g2d.drawLine(0, 0, -px, py);

           }
           g2d.setStroke(new BasicStroke(1));
           g2d.translate(0, translate);

       } else {
           g2d.rotate(angle);  //just rotate through the roll angle
       }
   }
   private void drawPitchRollLabels(Graphics2D g2d, double radius) {

       //Draw the pitch lines and labels      
       g2d.setColor(indicatorColor);
       int y;
       for (int i=-30; i<=30; i+= 5) {
           if (i==0)
               g2d.setStroke(new BasicStroke(4));  //thicker zero line
           else
               g2d.setStroke(new BasicStroke(1));
           y = (int)Math.round(i * radius * pitchSensitivity / 90.0);
           
           int width = (int)(radius/4);
           if ((i % 10) != 0) //smaller minor ticks
               width /= 2;
           else
               g2d.drawString(String.valueOf(i), 3, y-2);  //label for major ticks

           g2d.drawLine(width, y, -width, y);
           

       }
       
       //int rollIndicatorRadius = (int)(-realInsideRadius + realInsideRadius/10.0 + bezelBuffer) ;
       int rollIndicatorRadius = (int)(-radius + radius/10);// + radius/10.0 + bezelBuffer) ;
       int tickLength = (int)(radius + rollIndicatorRadius);
       
       //Draw the roll indicator arrow
       g2d.drawLine(0, 0, 0, rollIndicatorRadius);
       g2d.fillPolygon(new int[]{0, -tickLength/4, tickLength/4},
                  new int[]{rollIndicatorRadius, rollIndicatorRadius+tickLength/2, rollIndicatorRadius+tickLength/2},
                  3);

       //Back to no rotation    
       g2d.setTransform(centerGaugeTransform);
       //Draw the roll indicators and labels
       g2d.rotate(Math.toRadians(-60));
       for (int i=-60; i<=60; i+=5) {
           
           if ((i % 10) == 0) {  //major tick
               g2d.drawString(String.valueOf(i), 2, rollIndicatorRadius);
               g2d.drawLine(0, rollIndicatorRadius, 0, (int)-radius);         
           }
           else if (outsideRadius > 250) //draw minor tick, if large enough
               g2d.drawLine(0, rollIndicatorRadius - tickLength/2, 0, (int)-radius);         
          
           g2d.rotate(Math.toRadians(5.0));
       }
   }
   private void drawCompassLabels(Graphics2D g2d) {

       //center the compass
       Dimension size = this.getSize();
       g2d.translate(size.width/2, size.height/2);  
       
       centerGaugeTransform = g2d.getTransform();
       g2d.setTransform(centerGaugeTransform);
       
       //implement the compass (yaw)
       int indicatorRadius = (int)(-yawRadius + yawRadius*tickScale);// (realInsideRadius/3.5)); //-75 to move it into the black circle. 
       int majorTickIncrement;
       if (outsideRadius < 75)
           majorTickIncrement = 90;
       else if (outsideRadius < 150)
           majorTickIncrement = 30;
       else if (outsideRadius < 200)
           majorTickIncrement = 15;
       else
           majorTickIncrement = 10;

       //Draw the indicators and labels
       for (int i=0; i<360; i+=5) {
           
           if ((i % majorTickIncrement) == 0) {  //major tick
               g2d.drawString(String.valueOf(i), 2, indicatorRadius+20);
               
               int lineStart;
               if (thickerCardinalLine) {
               	//-40 to elongate the thicker lines
                   lineStart = indicatorRadius + yawRadius - 40; //double for N, W, E, S
                   g2d.setStroke(new BasicStroke(4));  //thicker line
               }
               else {
                   lineStart = indicatorRadius - 75;
                   g2d.setStroke(new BasicStroke(1));  //normal line
               }
               Font origFont = g2d.getFont();
               Font largeFont = origFont.deriveFont((float)origFont.getSize()*2);
               g2d.setFont(largeFont);
               
               int letterShift = 20; // to move the letters closer to the center
               
               switch (i) {
                  case 0:
                       drawCardinalLetter(g2d, "N", indicatorRadius + 2*yawRadius - letterShift); 
                       break;
                   case 90:
                       drawCardinalLetter(g2d, "E", indicatorRadius + 2*yawRadius - letterShift);
                       break;
                   case 180:
                       drawCardinalLetter(g2d, "S", indicatorRadius + 2*yawRadius - letterShift);
                       break;
                   case 270:
                       drawCardinalLetter(g2d, "W", indicatorRadius + 2*yawRadius - letterShift);
                       break;
                   default:
                       lineStart = indicatorRadius-15; //-15 to move the major tick marks further back
                       g2d.setFont(origFont);
                       g2d.setStroke(new BasicStroke(1));  //normal thin line
                       break;
               }

               g2d.drawLine(0, lineStart, 0, (int)-yawRadius ); //-20 to shorten the major tick marks
               g2d.setStroke(new BasicStroke(1));  
               g2d.setFont(origFont);

           }
           else if (outsideRadius > 250) //draw minor tick, if large enough
               g2d.drawLine(0, indicatorRadius, 0, (int)-yawRadius);    
          
           g2d.rotate(Math.toRadians(5.0));
       }
       
        if (northUp && Double.isFinite(bearing))
           g2d.rotate(bearing);
       
       g2d.setStroke(new BasicStroke(2.0f));
       
   }
   private void drawCompassArrow(Graphics2D g2d) {

       //rotates the red yaw arrow
       if (!northUp)
           g2d.rotate(-bearing);

       g2d.rotate(course);

       g2d.setColor(courseNeedleColor); 
       drawCourseNeedle(g2d, -(int)yawRadius);
   }
   
   private static void drawCourseNeedle(Graphics2D g2d, int radius) {
       int arrowSize = 30;
       g2d.drawLine(0, 0, 0, radius+arrowSize/2 /*- 70*/); //-70 to elongate the arrow
       int shift = 0;// increase to move the arrowhead up
       g2d.fillPolygon(new int[]{0, -arrowSize/2, arrowSize/2},
                  new int[]{radius-shift, radius+arrowSize-shift, radius+arrowSize-shift}, 
                  3);

   }
   
    @Override
    public void paint(Graphics g) {        

        //General graphics setup
        Graphics2D g2d = (Graphics2D)g;        
        setupForPaint(g2d);
        yawRadius = (int)realInsideRadius;
        
        //Because of rounding effects with integers, we need to extend the inside radius a bit, to the middle
        //of the gauge ring.  This will hide corner artifacts of the summing of the arc and triangles
    	double insideRadius = (yawRadius - bezelBuffer );  //inside radius to use for drawing
        double translate = insideRadius * translateFactor;  //how far to translate the horizon vertically, negative is down, positive is up
       

        drawBackground(g2d, insideRadius, translate);
        drawPitchRollLabels(g2d, insideRadius);
        
        //Restore to origin
        g2d.setTransform(centerGaugeTransform);
                
        //Now paint the bezel
        drawBezel(g2d);
        
        completePaint(g2d);
        
        //Edit: Add compass
        drawCompassLabels(g2d);
        //Restore to origin
        g2d.setTransform(centerGaugeTransform);
     	drawCompassArrow(g2d);
        //Restore to origin
        g2d.setTransform(centerGaugeTransform);
        
        //Restore to origin
        //g2d.setTransform(centerGaugeTransform);
   
        //Draw Center of dial
        drawDialCenter(g2d);
        completePaint(g2d);
        
    }
    
    /**
     * Set the direction in degrees of the course needle
     * @param c the course, from 0-360.  Values outside this range will be modulus 360.
     */
    public final void setCourse(double c) {
        c = c % 360;
        if (c < 0)
            c = 360 - c;
        course = Math.toRadians(c);
        repaint();
    }
    
    
    
    
    
    
}
