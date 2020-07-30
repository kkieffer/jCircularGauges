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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import javax.swing.JComponent;

/**
 *
 * @author kkieffer
 */
public class JCircularGauge extends JComponent {
    
       
    protected Color bezelColor;
    protected Color background;
    protected int outsideRadius;
    protected double realInsideRadius;
    private AffineTransform origTransform;
    protected AffineTransform centerGaugeTransform;
    protected float dialCenterDivider = 20;    
    
    private float[] dist = {0.0f, 0.89f, 0.9f, 0.95f, 1.0f};  //Bezel gradients, starting at .89 * radius

    /**
     * Create the JArtificialHorizon gauge with default parameters
     */
    public JCircularGauge() {
        setColors(null, null);
    }
    
    protected void setBezelGradients(float[] d) {
        dist = d;
    }
    
    /**
     * Customize the gauge colors
     * @param bezelColor the gauge color, null for default
     * @param background the gauge background color
     */
    public final void setColors(Color bezelColor, Color background) {
        this.bezelColor = bezelColor == null ? Color.DARK_GRAY : bezelColor;
        this.background = background == null ? Color.WHITE : background;
        repaint();
    }
    
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 600);
    }
 
        
    
    protected int getOutsideRadius() {  //Determine the smallest of height and width
        Dimension size = this.getSize();
        if (size.height > size.width)
            return size.width/2;
        else
            return size.height/2;
    }
    
   
    
    //Paint the rim of the gauge (assumes translated to center of dial)    
    protected void drawBezel(Graphics2D g) {
    
        int r = getOutsideRadius(); //*1.25
        Color[] colors = {new Color(0,0,0,0), new Color(0,0,0,0), bezelColor, Color.WHITE, bezelColor};
        
        RadialGradientPaint rgp = new RadialGradientPaint(new Point2D.Double(0,0), r, dist, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE);
        
        Paint paint = g.getPaint();
        g.setPaint(rgp);
        g.fillOval(-r, -r, 2*r, 2*r);
        g.setPaint(paint);
       
    }
    
    
    protected void paintGaugeBackground(Graphics2D g) {
        g.setColor(background);
        int r = getOutsideRadius();

        g.fillOval(-r, -r, 2*r, 2*r);
        
    }
    
    protected void drawDialCenter(Graphics2D g2d) {
        //Draw Center of dial
        double r = realInsideRadius/dialCenterDivider;
        g2d.fill(new Ellipse2D.Double(-r/2, -r/2, r, r));
    }
    
    protected void setupForPaint(Graphics2D g) {

        outsideRadius = getOutsideRadius();  //absolute outside radius which includes bezel
        realInsideRadius = outsideRadius * dist[2];   //the actual inside radius of the bezel
    	
        Graphics2D g2d = (Graphics2D)g;        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setFont(new Font("Arial", Font.PLAIN, (int)(6 + Math.round(outsideRadius/40.0))));
        
        //Translate to center of the gauge circle (our new origin 0,0 from here on out)
        origTransform = g2d.getTransform();
        Dimension size = this.getSize();
        g2d.translate(size.width/2, size.height/2);  
               
        centerGaugeTransform = g2d.getTransform();
    }
    
    
    protected void completePaint(Graphics2D g) {
        
        g.setTransform(origTransform);
    }
    
}
