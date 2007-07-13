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

import abc.tm.weaving.weaver.tmanalysis.dynainst.DynamicInstrumenter;

/**
 * ShadowSwitch - stub for shadow switching. Is extended by {@link DynamicInstrumenter}.
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
			//enable all
			for(int i=0;i<enabled.length;i++) {
			    enabled[i] = true;
			}
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
		
		//FIXME improve and enable UI
		
//		String argString = System.getProperty("SHADOWGROUPS","");
//		parse(argString);
	}

	private static void parse(String argString) {
		String[] split = argString.split(":");
		String format = split[0];
		if(format.equals("enum")) {
			byNumber(split[1]);
		} else if(format.equals("upto")) {
			upToNumber(split[1]);
		}
	}
	
	private static void upToNumber(String arg) {
		int bound = Integer.parseInt(arg);
		if(bound<0) {
			throw new IllegalArgumentException("bound must be >=0 !");
		}
		for(int i=0;i<bound;i++) {
			enableShadowGroup(i);
		}
	}

	private static void byNumber(String enumeration) {
		StringTokenizer toki = new StringTokenizer(enumeration,",");
	    while(toki.hasMoreTokens()) {
	    	String token = toki.nextToken();
	    	int groupId = Integer.parseInt(token);
	    	enableShadowGroup(groupId);
	    }
	}

	public static void enableShadowGroup(int groupNumber) {
    	System.out.println("enabled shadow group #"+groupNumber);
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

    public static void dumpShadowCounts() {
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
