/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.aspectbench.tm.runtime.internal.labelshadows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Interactive command line UI for switching label shadows.
 *
 * @author Eric Bodden
 */
public class InteractiveLabelShadowSwitchPrompt extends	AbstractLabelShadowSwitch {

	/**
	 * {@inheritDoc}
	 */
	public void run() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("At the moment, label shadows for all shadows are enabled.");
		
		while(true) {
			System.out.println("Please type the name of any class containing a tracematch to disable/enable its label shadows.\n" +
					"Type 'x' to exit this console.");
			try {
				String line = in.readLine();
				if(line.toLowerCase().equals("x")) {
					break;
				}
				boolean enabled = switchTraceMatch(line);
				
				System.err.println("new label-edge status: "+(enabled?"enabled":"disabled"));
				
			} catch (IOException e) {
				break;
			}
		}		

	}
	
}
