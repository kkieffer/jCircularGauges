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


import java.awt.Color;
import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Demo of the Gauge (alternates between North Up and Bearing Up
 * 
 * @author kkieffer
 */
public class JSpeedometerDemo {
     
    public static void main(String[] args) throws InterruptedException {

	JFrame myFrame = new JFrame("Speedometer Demo");
	
	Container thePane = myFrame.getContentPane();
        
        
        JSpeedometer g = new JSpeedometer(10, "knots");  
        g.setColors(Color.RED, null, Color.BLACK);
	thePane.add(g);

	myFrame.pack();
        myFrame.setVisible(true);
        
        
        //Now, cycle the speed
        int b = 0;
        while (true) {
  
            final double spd = b;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    g.setSpeed(spd);
                }

            });            
            Thread.sleep(100);

            b+=1.0;
            if (b > 180)
                b = 0;
            
        }
 
    }
    
 
    
}
