/*
Copyright (C) 2019 K. Kieffer

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
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/**
 * This is a speedometer gauge with numeric readout, from 0 some maximum value, with 0 pointing straight down.  Major ticks with integer labels are drawn at 20 degree increments,
 * and minor ticks are drawn every 10 degrees if the gauge is sized large enough.  Fonts scale with gauge size.
 *  
 * The speed can be set between 0 and any value, but the needle will only go just above the maximum tick value, which is just over 140 degrees right of top
 * The units of speed can be set as well as the major tick increment.
 * 
 * Setting the increment value determines the range of the gauge.   
 *
 * @author kkieffer
 */
public class JSpeedometer extends JCircularGauge {
    
    private static final int MAJOR_TICKS = 14;
    
    private final double maxSpeed;
    private String unit;
    private double currentSpeed;
    private Color indicatorColor;
    private final int tickIncrement;
    
    /**
     * Create the JSpeedometer gauge 
     * @param increment the major tick increment
     * @param unit the speed unit label
     */
    public JSpeedometer(int increment, String unit) {
        maxSpeed = increment * MAJOR_TICKS;
        this.tickIncrement = increment;
        this.unit = unit;
        indicatorColor = Color.BLACK;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
   
     /**
     * Set the colors of the gauge
     * @param indicator the needle and labels, marks.  If null, color is black
     * @param bezelColor the gauge color, null for default
     * @param background the gauge background color
     */
    public void setColors(Color indicator, Color bezelColor, Color background) {
        indicatorColor = indicator == null ? Color.BLACK : indicator;
        super.setColors(bezelColor, background);
    }
    
    /**
     * Set the current speed in terms of the units specified
     */
    public final void setSpeed(double spd) {
        if (spd < 0)
            spd = 0;
        currentSpeed = spd;
        repaint();
    }
    
    

    private void drawNeedle(Graphics2D g2d, double radius, double tickLen) {
            
       double angle = -180 + (280 * currentSpeed /maxSpeed );
       if (angle > 110)
           angle = 110; //slightly more to indicate over

       g2d.rotate(Math.toRadians(angle));

       double width = realInsideRadius/20.0;
        
       Path2D path = new Path2D.Double();
       path.moveTo(-width/2+1, 0);
       path.lineTo(-1, radius + tickLen + 3);
       path.lineTo(1, radius + tickLen + 3);
       path.lineTo(width/2-1, 0);
       path.closePath();
       
       g2d.fill(path);
        
       g2d.rotate(Math.toRadians(-angle));

    }
    
    
    
    @Override
    public void paint(Graphics g) {
        
        Graphics2D g2d = (Graphics2D)g;
        
        setupForPaint(g2d);
        
        this.paintGaugeBackground(g2d);

        g2d.setColor(indicatorColor);
 
        
        int indicatorRadius = (int)(-realInsideRadius + realInsideRadius/10.0);
        int tickLength = (int)(realInsideRadius + indicatorRadius);
        
        double angle = -180;
        g2d.rotate(Math.toRadians(angle));

        double speedLabel = 0;        
        int smallTick = 10;
                
        //Draw the speed indicators and labels
        for (int i=(int)angle; i<=100; i+=smallTick) {
                        
            
            if ((i % 20) == 0) {  //major tick

                int lineStart = indicatorRadius + tickLength; //double for N, W, E, S

                g2d.setStroke(new BasicStroke(4));  //thicker line
                Font origFont = g2d.getFont();
                Font largeFont = origFont.deriveFont((float)origFont.getSize()*1.3f);
                g2d.setFont(largeFont);
                String label = String.valueOf((int)Math.round(speedLabel));
                int fontWidth = g2d.getFontMetrics().stringWidth(label);
                int fontHeight = g2d.getFontMetrics().getHeight();

                AffineTransform a = g2d.getTransform();
                g2d.translate(fontWidth/2 + 8, indicatorRadius + tickLength/4);  //to desired location next to tick
                g2d.rotate(Math.toRadians(-angle)); //so text is upright
                g2d.translate(-fontWidth/2, fontHeight/2); //to left of text
                g2d.drawString(label, 0, 0);
                g2d.setTransform(a);
                
              
                g2d.drawLine(0, lineStart, 0, (int)-realInsideRadius);
                g2d.setStroke(new BasicStroke(1));  
                g2d.setFont(origFont);
                speedLabel += tickIncrement;

            }
            else if (outsideRadius > 150) //draw minor tick, if large enough
                g2d.drawLine(0, indicatorRadius - tickLength/2, 0, (int)-realInsideRadius);         
           
            
            g2d.rotate(Math.toRadians(smallTick));
            angle+= smallTick;

        }

        //Restore to origin
        g2d.setTransform(centerGaugeTransform);
        
        drawNeedle(g2d, indicatorRadius, tickLength);
         
        //Paint the value
        Font origFont = g2d.getFont();
        Font largeFont = origFont.deriveFont((float)origFont.getSize()*4);
        g2d.setFont(largeFont);
        String label = String.valueOf((int)Math.round(currentSpeed));
        int fontWidth = g2d.getFontMetrics().stringWidth(label);

        g2d.translate((int)(realInsideRadius/2), (int)(realInsideRadius/2));
        g2d.drawString(label, -fontWidth, 0);
        
        //Paint the unit
        largeFont = origFont.deriveFont((float)origFont.getSize()*2);
        g2d.setFont(largeFont);
        fontWidth = g2d.getFontMetrics().stringWidth(unit);
        int unitFontHeight = g2d.getFontMetrics().getHeight();

        g2d.drawString(unit, -fontWidth, 0 + unitFontHeight);

        
        
        //Restore to origin
        g2d.setTransform(centerGaugeTransform);
        
           
        //Draw Center of dial
        double r = realInsideRadius/14;
        g2d.fill(new Ellipse2D.Double(-r/2, -r/2, r, r));
 
        //Now paint the bezel
        paintBezel(g2d);
            
        completePaint(g2d);

        
    }
    
    
    
    
    
}
