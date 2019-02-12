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
import java.awt.Container;
import java.awt.Dimension;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author kkieffer
 */
public class JSegmentGaugeDemo {
    
    
    public static void main(String[] args) throws InterruptedException {

	JFrame myFrame = new JFrame("SegmentGauge Demo");
	
	Container thePane = myFrame.getContentPane();
        
        
        //Create a horizontal meter, running left to right, with the colors above and a light grey background
        final JSegmentGauge g = new JSegmentGauge(true, 12, new BasicStroke(1), Color.BLACK, Color.WHITE, 2);
        
        g.changeLabel(0, "X1");
        g.changeLabel(1, "X2");
        g.changeLabel(2, "Y1");
        
        myFrame.setPreferredSize(new Dimension(200, 100));
        
	thePane.add(g);

	myFrame.pack();
        myFrame.setVisible(true);
        
        Random r = new Random();
        while (true) {

             final int i = r.nextInt(12);
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     
                     g.setAllSegmentColors(Color.GRAY);
                     g.changeSegmentColor(i, Color.RED);
                 }

             });            
             Thread.sleep(100);


         }
 
    }
    
}
