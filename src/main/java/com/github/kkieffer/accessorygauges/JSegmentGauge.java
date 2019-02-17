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
package com.github.kkieffer.accessorygauges;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.JComponent;


/**
 * A JSegmentGauge shows bars which may be colored, and optionally labelled, to show a level. The gauge is either horizontal or vertical.  Indicies start
 * start on the left for horizontal and bottom for vertical
 * 
 * @author kkieffer
 */
public class JSegmentGauge extends JComponent {

    private final Color labelColor;
    private final Color[] segmentColors;
    private final String[] segmentLabels;
    private final Color strokeColor; //color for outline of each segment
    private final BasicStroke outlineStroke;  //stroke for outline of each segment
    private final boolean isHoriz; //true for horizontal meter, false for vertical
    private final int segmentGap; //pixels of gap between segments

    /**
     * 
     * @param isHorizontal true for a horizontal segments, false for vertical
     * @param numSegments number of segments in the meter
     * @param stroke attributes of the segment line border outline.  Use null to remove border 
     * @param strokeColor the color of the segment line border outline.  Use null to remove border
     * @param labelColor color of the segment label
     * @param gap the number of pixels between each segment
     */
    public JSegmentGauge(boolean isHorizontal, int numSegments, BasicStroke stroke, Color strokeColor, Color labelColor, int gap) {
        
     
        this.outlineStroke = stroke;
        this.isHoriz = isHorizontal;
        this.segmentGap = gap;
        this.strokeColor = strokeColor;
        this.labelColor = labelColor;
        
        segmentColors = new Color[numSegments];
        segmentLabels = new String[numSegments];
        for (int i=0; i<segmentColors.length; i++) {
            segmentColors[i] = Color.DARK_GRAY;
            segmentLabels[i] = "";
        }
        
    }
    
    
    
    @Override
    public Dimension getPreferredSize() {
        
        int strokeSize = outlineStroke == null ? 0 : (int)outlineStroke.getLineWidth();
        
        
        int longSide = (strokeSize + segmentColors.length) * 20 + (segmentColors.length-1) * segmentGap;
        int shortSide = 10 + strokeSize;
        
        Insets insets = this.getInsets();
        
        if (isHoriz)
            return new Dimension(longSide, shortSide + insets.top + insets.bottom);
        else
            return new Dimension(shortSide, longSide + insets.top + insets.bottom);
                
    }
    
    /**
     * Change the color of the specified segment index
     * @param index the segment index, which must be within the valid range
     * @param c the new Color
     */
    public void changeSegmentColor(int index, Color c) {
        segmentColors[index] = c;  
        repaint();
    }
    
    
    public void changeLabel(int index, String label) {
        segmentLabels[index] = label;
        repaint();
    }
    
    /**
     * Set the color of all segments to the same value
     * @param c the color to set
     */
    public void setAllSegmentColors(Color c) {
        for (int i=0; i<segmentColors.length; i++)
            segmentColors[i] = c;  
        repaint();
    }
    
    /**
     * Clear all labels
     */
    public void clearAllLabels() {
        for (int i=0; i<segmentLabels.length; i++)
            segmentLabels[i] = "";  
        repaint();
    }
    
    
    private int computeSize(int spaceAvailable) {       
        int totalGap = (segmentColors.length-1) * segmentGap; 
        return (spaceAvailable - totalGap)/segmentColors.length;  //divide the remaining space after removing the gap among segments
    }
    
    
    
    
    @Override
    public void paint(Graphics g) {
        
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        Dimension size = this.getSize();
        Insets insets = this.getInsets();
        
        size.width -= insets.left + insets.right;
        size.height -= insets.top + insets.bottom;
        
        int segmentWidth = isHoriz ? computeSize(size.width) : size.width;  //if horizontal, divide across width, otherwise use the full width
        int segmentHeight = isHoriz ? size.height : computeSize(size.height); //if horizontal, use the full height, otherwise divide across height

        int step = isHoriz ? segmentWidth + segmentGap : segmentHeight + segmentGap; //the amount of space to move to the next segment (including the gap)
       
        int fontSize = (int)(6 + Math.round(isHoriz ? segmentWidth : segmentHeight)/4);
        if (fontSize > 18)
            fontSize = 18;
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSize));
        
        int x = 0;
        int y = 0;
        
        if (isHoriz) 
            x = 0;     
        else
            y = size.height - step; //start at the bottom less the stride
        
        //Paint each segment
        for (int i=0; i<segmentColors.length; i++) {

            g.setColor(segmentColors[i]);
            g.fillRect(x, y, segmentWidth, segmentHeight);

            //Paint the border, if not null
            if (outlineStroke != null && strokeColor != null) {
                g.setColor(strokeColor);
                g2d.setStroke(outlineStroke);
                g.drawRect(x, y, segmentWidth, segmentHeight);
            }
            
            
            String label = segmentLabels[i];
            if (!label.isEmpty()) {
                g.setColor(labelColor);

                int fontWidth = g2d.getFontMetrics().stringWidth(label);
                int fontHeight = g2d.getFontMetrics().getHeight();

                int cx = x + segmentWidth/2;
                int cy = y + segmentHeight/2;
                g.drawString(label, cx - fontWidth/2, cy + fontHeight/2);
            }
            
            if (isHoriz) 
                x = x + step;  // move right a step        
            else
                y = y - step;  //otherwise move up a step
            
        }
    
        
    }
    
   

    
}
