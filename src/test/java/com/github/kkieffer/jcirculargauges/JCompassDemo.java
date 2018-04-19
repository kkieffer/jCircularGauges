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


import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Demo of the Gauge
 * 
 * @author kkieffer
 */
public class JCompassDemo {
     
    public static void main(String[] args) throws InterruptedException {

	JFrame myFrame = new JFrame("Compass Demo");
	
	Container thePane = myFrame.getContentPane();
        
        
        JCompass g = new JCompass(false);  //slightly more pitch sensitivity
        g.setCourse(45);
	thePane.add(g);

	myFrame.pack();
        myFrame.setVisible(true);
        
        
        //Now, cycle the bearing
        int b = 0;
        while (true) {
  
            final double bearing = b;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    g.setBearing(bearing);
                }

            });            
            Thread.sleep(100);

            b+=1.0;
            b = b% 360;
        }
 
    }
    
 
    
}
