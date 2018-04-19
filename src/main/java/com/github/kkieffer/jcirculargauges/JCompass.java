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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * This is a typical compass gauge, from 0 to 360 degrees, with 0 = N, 90 = E, etc.  Major ticks with degree labels are drawn at 10 degree increments,
 * the N,E,W,S indicators are drawn, and minor ticks are drawn every 5 degrees if the compass is sized large enough.  Fonts scale with gauge size.
 * 
 * The primary needle is to show bearing of the vehicle, an optional secondary needle shows the desired course.
 * 
 * The compass can be configured to be north up, where N is always at the top and the primary needle moves.  If not set for north up, the compass rotates
 * while the bearing needle always points up.  Regardless of the setting, the course needle always points to the set course.

 * @author kkieffer
 */
public class JCompass extends JCircularGauge {
    
   
    private final boolean northUp;
    private double bearing;
    private double course;
    private boolean showCourseNeedle = true;
    
    /**
     * Create the JCompass gauge 
     * @param northUp true to always have north up, otherwise, gauge rotates and bearing is always up
     */
    public JCompass(boolean northUp) {
        this.northUp = northUp;
    }

    /**
     * Set the direction in degrees of the bearing needle
     * @param b the bearing, from 0-360.  Values outside this range will be modulus 360.
     */
    public final void setBearing(double b) {
        b = b % 360;
        if (b < 0)
            b = 360 - b;
        bearing = Math.toRadians(b);
        repaint();
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
    
    /**
     * Show or hide the course needle.  The default is to show.
     * @param show 
     */
    public void showCourseNeedle(boolean show) {
        this.showCourseNeedle = show;
    }
    

    private void drawBearingNeedle(Graphics2D g2d, int radius, int tickLen) {
        if (!northUp)
            g2d.rotate(bearing);

        
        g2d.drawLine(0, 0, 0, radius);
        g2d.fillPolygon(new int[]{0, -tickLen/2, tickLen/2},
                   new int[]{radius, radius+tickLen, radius+tickLen},
                   3);
        
        if (!northUp)
            g2d.rotate(-bearing);

    }
    
    
    private void drawCourseNeedle(Graphics2D g2d, int radius, int tickLen) {
       
        g2d.drawLine(0, 0, 0, radius);
        g2d.fillPolygon(new int[]{0, -tickLen/2, tickLen/2},
                   new int[]{radius, radius+tickLen, radius+tickLen},
                   3);
 
    }
   
    
    
    @Override
    public void paint(Graphics g) {
        
        Graphics2D g2d = (Graphics2D)g;
        
        setupForPaint(g2d);
        
        this.paintGaugeBackground(g2d);

        g2d.setColor(Color.BLACK);

        //Draw Center of dial
        int r = (int)realInsideRadius/20;
        g2d.fillOval(-r/2, -r/2, r, r);
        
        int rollIndicatorRadius = (int)(-realInsideRadius + realInsideRadius/10.0);
        int tickLength = (int)(realInsideRadius + rollIndicatorRadius);
        
        
        if (!northUp)
            g2d.rotate(-bearing);
        
        //Draw the roll indicators and labels
        for (int i=0; i<360; i+=5) {
            
            if ((i % 10) == 0) {  //major tick
                g2d.drawString(String.valueOf(i), 2, rollIndicatorRadius);
                
                int lineStart = rollIndicatorRadius + tickLength; //double for N, W, E, S
                g2d.setStroke(new BasicStroke(4));  //thicker line
                Font origFont = g2d.getFont();
                Font largeFont = origFont.deriveFont((float)origFont.getSize()*2);
                g2d.setFont(largeFont);
                
                
                switch (i) {
                    case 0:
                        Rectangle2D stringBounds = g2d.getFontMetrics().getStringBounds("N", g2d);
                        g2d.drawString("N", (int)-stringBounds.getCenterX(), rollIndicatorRadius + 2*tickLength + (int)stringBounds.getMaxY());
                        break;
                    case 90:
                        stringBounds = g2d.getFontMetrics().getStringBounds("E", g2d);
                        g2d.drawString("E", (int)-stringBounds.getCenterX(), rollIndicatorRadius + 2*tickLength + (int)stringBounds.getMaxY());
                        break;
                    case 180:
                        stringBounds = g2d.getFontMetrics().getStringBounds("S", g2d);
                        g2d.drawString("S", (int)-stringBounds.getCenterX(), rollIndicatorRadius + 2*tickLength + (int)stringBounds.getMaxY());
                        break;
                    case 270:
                        stringBounds = g2d.getFontMetrics().getStringBounds("W", g2d);
                        g2d.drawString("W", (int)-stringBounds.getCenterX(), rollIndicatorRadius + 2*tickLength + (int)stringBounds.getMaxY());
                        break;
                    default:
                        lineStart = rollIndicatorRadius;
                        g2d.setFont(origFont);
                        g2d.setStroke(new BasicStroke(1));  //thicker zero line
                        break;
                }

                g2d.drawLine(0, lineStart, 0, (int)-realInsideRadius);
                g2d.setStroke(new BasicStroke(1));  
                g2d.setFont(origFont);

            }
            else if (outsideRadius > 250) //draw minor tick, if large enough
                g2d.drawLine(0, rollIndicatorRadius - tickLength/2, 0, (int)-realInsideRadius);         
           
            g2d.rotate(Math.toRadians(5.0));
        }
        
         if (northUp)
            g2d.rotate(bearing);
        
        drawBearingNeedle(g2d, rollIndicatorRadius, tickLength);
 
         //Restore to origin
        g2d.setTransform(centerGaugeTransform);
        
        if (showCourseNeedle) {
            if (!northUp)
                g2d.rotate(-bearing);

            g2d.rotate(course);

            g2d.setColor(Color.RED);
            drawCourseNeedle(g2d, rollIndicatorRadius*2/3, tickLength);

            //Restore to origin
            g2d.setTransform(centerGaugeTransform);

        }

        //Now paint the bezel
        paintBezel(g2d);
            
        completePaint(g2d);

        
    }
    
    
    
    
    
}
