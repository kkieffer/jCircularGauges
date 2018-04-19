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

import java.awt.BasicStroke;
import static java.awt.BasicStroke.CAP_SQUARE;
import static java.awt.BasicStroke.JOIN_MITER;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;

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
    
    private static final Color BROWN = new Color(160, 90, 70);  //default ground
    private static final Color BLUE = new Color(175, 225, 255); //default sky
    
    private static final double DEFAULT_PITCH_SENSITIVITY = 1.0;  //default sensitivity
    
    
    private double angle;
    private double pitchSensitivity;
    private double translateFactor;
    private Color groundColor;
    private Color skyColor;
    
    /**
     * Create the JArtificialHorizon gauge with default parameters
     */
    public JArtificialHorizonGauge() {
        this(DEFAULT_PITCH_SENSITIVITY);
    }
    
    /**
     * Create the JArtificialHorizon gauge with default parameters
     * @param pitchSensitivity the pitch sensitivity, where 1.0 cooresponds to 45 degrees pitch at half the radius of the gauge
     */
    public JArtificialHorizonGauge(double pitchSensitivity) {
        this.pitchSensitivity = pitchSensitivity;
        setAttitude(0.0, 0.0);
        setColors(null, null, null);
    }
    
    
    /**
     * Customize the gauge colors
     * @param bezelColor the gauge color, null for default
     * @param groundColor the ground color, null for default
     * @param skyColor the sky color, null for default
     */
    public final void setColors(Color bezelColor, Color groundColor, Color skyColor) {
        super.setColors(bezelColor, null);
        this.groundColor = groundColor == null ? BROWN : groundColor;
        this.skyColor = skyColor == null ? BLUE : skyColor;
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
    

    
    
    @Override
    public void paint(Graphics g) {
             
        //Because of rounding effects with integers, we need to extend the inside radius a bit, to the middle
        //of the gauge ring.  This will hide corner artifacts of the summing of the arc and triangles
        double insideRadius = outsideRadius * 0.99;  //inside radius to use for drawing
        double translate = insideRadius * translateFactor;  //how far to translate the horizon vertically, negative is down, positive is up
        
        //General graphics setup
        Graphics2D g2d = (Graphics2D)g;        
        setupForPaint(g2d);

                
        //If pitching down (horizon goes up), the draw the ground first, otherwise dry the sky
        g2d.setColor(translate > 0 ? groundColor : skyColor);
       
        //Fill the gauge the background (sky or ground)
        g2d.fillOval((int)-insideRadius, (int)-insideRadius, (int)insideRadius*2, (int)insideRadius*2);
            
        AffineTransform centerDialTransform = g2d.getTransform();

        if (Math.abs(translate) <= insideRadius) {
        
            //Now switch to draw the other one
            g2d.setColor(translate > 0 ? skyColor : groundColor);

            //Value d is half the length of the new horizon.  If pitch is zero, then d == radius, otherwise d is smaller than radius
            double d = Math.sqrt(Math.pow(insideRadius, 2) -  Math.pow(translate, 2));

            //Theta is the angle from the line that intersects the origin and horizon at edge of the gauge, and the radius perpendicular to the horizon
            int theta = (int)Math.toDegrees(Math.PI/2 - Math.asin(d/insideRadius));

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

            int r = (int)Math.round(insideRadius);
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
            int z = (int)insideRadius * (translate < 0 ? -1 : 1);
            Polygon p = new Polygon(new int[]{0, -l, l}, new int[]{z, -t, -t},  3);
            g2d.fillPolygon(p);


            g2d.setColor(Color.BLACK);

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

        } else
            g2d.rotate(angle);  //just rotate through the roll angle

         
        //Draw the pitch lines and labels      
        g2d.setColor(Color.BLACK);
        int y;
        for (int i=-30; i<=30; i+= 5) {
            if (i==0)
                g2d.setStroke(new BasicStroke(4));  //thicker zero line
            else
                g2d.setStroke(new BasicStroke(1));
            y = (int)Math.round(i * insideRadius * pitchSensitivity / 90.0);
            
            int width = (int)(insideRadius/4);
            if ((i % 10) != 0) //smaller minor ticks
                width /= 2;
            else
                g2d.drawString(String.valueOf(i), 3, y-2);  //label for major ticks

            g2d.drawLine(width, y, -width, y);
            

        }
        
        int rollIndicatorRadius = (int)(-realInsideRadius + realInsideRadius/10.0);
        int tickLength = (int)(realInsideRadius + rollIndicatorRadius);
        
        //Draw the roll indicator arrow
        g2d.drawLine(0, 0, 0, rollIndicatorRadius);
        g2d.fillPolygon(new int[]{0, -tickLength/4, tickLength/4},
                   new int[]{rollIndicatorRadius, rollIndicatorRadius+tickLength/2, rollIndicatorRadius+tickLength/2},
                   3);
            
        //Back to no rotation    
        g2d.setTransform(centerDialTransform);
        
        //Draw the roll indicators and labels
        g2d.rotate(Math.toRadians(-60));
        for (int i=-60; i<=60; i+=5) {
            
            if ((i % 10) == 0) {  //major tick
                g2d.drawString(String.valueOf(i), 2, rollIndicatorRadius);
                g2d.drawLine(0, rollIndicatorRadius, 0, (int)-realInsideRadius);         
            }
            else if (outsideRadius > 250) //draw minor tick, if large enough
                g2d.drawLine(0, rollIndicatorRadius - tickLength/2, 0, (int)-realInsideRadius);         
           
            g2d.rotate(Math.toRadians(5.0));
        }

        //Restore to origin
        g2d.setTransform(centerDialTransform);
                
        //Now paint the bezel
        paintBezel(g2d);
        
        
        completePaint(g2d);

        
    }
    
    
    
    
    
}
