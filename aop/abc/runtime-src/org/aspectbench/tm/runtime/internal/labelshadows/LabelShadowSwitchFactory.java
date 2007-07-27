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

/**
 * Factory for switching label shadows.
 *
 * @author Eric Bodden
 */
public class LabelShadowSwitchFactory {
	
	/** Priority value higher than {@link Thread#NORM_PRIORITY}.  */
	private static final int ABOVE_NORMAL = 7;

	public static void start() {
		String params = System.getProperty("LABELSHADOWSWITCH");
		
		if(params==null) {
			return;
		}
		
		Runnable runnable = null;		
		if(params.equals("interactive")) {
			runnable = new InteractiveLabelShadowSwitchPrompt();
		} else if(params.equals("timed")){
			runnable = new TimedShadowSwitch();
		} else {
			System.err.println("#######################################");
			System.err.println("Illegal argument for LABELSHADOWSWITCH: "+params);
			System.err.println("USAGE:");
			System.err.println("LABELSHADOWSWITCH=interactive");
			System.err.println("LABELSHADOWSWITCH=timed");
			System.err.println("#######################################");
		}
		if(runnable!=null) {
			Thread thread = new Thread(runnable);
			thread.setPriority(ABOVE_NORMAL);
			thread.start();
		}
	}

}
