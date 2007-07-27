/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
 * Copyright (C) 2007 Patrick Lam
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
package org.aspectbench.tm.runtime.internal;

import java.util.StringTokenizer;

import org.aspectbench.tm.runtime.internal.labelshadows.LabelShadowSwitchFactory;

/**
 * ShadowSwitch - stub for shadow switching. Is extended by DynamicInstrumenter.
 *
 * @author Eric Bodden
 * @author Patrick Lam
 */
public class ShadowSwitch {
	
	public static boolean groupTable[][];	
	public static boolean enabled[];
	public static int counts[];
	
	static {
		initialize();
	}

	private static void initialize() {
		try {
			IShadowSwitchInitializer initializer = (IShadowSwitchInitializer) Class.forName("org.aspectbench.tm.runtime.internal.ShadowSwitchInitializer").newInstance();
			initializer.initialize();
			
			String argString = System.getProperty("SHADOWGROUPS","");
			parse(argString);

			//dump shadows at shutdown time
			Runtime.getRuntime().addShutdownHook( new Thread() {
				public void run() {
					dumpShadowCounts();
				}
			});		
			LabelShadowSwitchFactory.start();
		} catch (InstantiationException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}				
		
	}


	private static void parse(String argString) {
		if(argString.length()>0) {
			if(argString.equals("all")) {
				all();
				return;
			}
			
			String[] split = argString.split(":");
			String format = split[0];
			String arg = split[1];		
			if(format.equals("enum")) {
				byNumber(arg);
			} else if(format.equals("upto")) {
				upToNumber(arg);
			} else {
				System.err.println("No shadow groups enabled.");
			}
		} else {
			System.err.println("No shadow groups enabled.");
		}
	}
	
	private static void all() {
		for(int i=0;i<groupTable.length;i++) {
			enableShadowGroup(i);
		}
		System.err.println("Enabled all shadow groups.");
	}

	private static void upToNumber(String arg) {
		int bound = Integer.parseInt(arg);
		if(bound<0) {
			throw new IllegalArgumentException("bound must be >=0 !");
		}
		for(int i=0;i<bound;i++) {
			enableShadowGroup(i);
		}
		System.err.println("Enabled all shadow groups up to #"+bound);
	}

	private static void byNumber(String enumeration) {
		StringTokenizer toki = new StringTokenizer(enumeration,",");
	    while(toki.hasMoreTokens()) {
	    	String token = toki.nextToken();
	    	int groupId = Integer.parseInt(token);
	    	enableShadowGroup(groupId);
	    }
		System.err.println("Enabled shadow groups: "+enumeration);
	}

	public static void enableShadowGroup(int groupNumber) {
		for (int i = 0; i < groupTable[groupNumber].length; i++) {
			boolean toEnable = groupTable[groupNumber][i];
			enabled[i] = enabled[i] | toEnable;
		}		
	}


	public static void disableAllGroups() {
		for (int i = 0; i < enabled.length; i++) {
			enabled[i] = false;			
		}
	}
		
    private static synchronized void dumpShadowCounts() {
        System.err.println("*** Printing out shadow counts ***");

        int sum = 0;
        for (int i = 0; i < counts.length; i++) {
            System.err.println(i + ": " + counts[i]);
            sum += counts[i];
        }
        System.err.println("sum: "+sum);
        System.err.println("*** shadow counts end ***");
    }
	
}
