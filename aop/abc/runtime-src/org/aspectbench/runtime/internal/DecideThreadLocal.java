/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Damien Sereni
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

package org.aspectbench.runtime.internal;

/** Decide whether or not it is possible to use the ThreadLocal implementation
 *  of cflow (at runtime)
 * @author Damien Sereni
 */
public class DecideThreadLocal {

	private static boolean useThreadLocal = set(); 
	
	private static boolean set() {
	    if (System.getProperty(
		  "org.aspectbench.DontUseCflowThreadLocal",
				   "false").equals("true"))
		return false;

		String jversion = System.getProperty("java.version");
		// Assume jversion is of the form x.y.<something> or x.y
		// for x, y ints. Otherwise, for safety default to false

		int firstDot = jversion.indexOf('.');
		if (firstDot == -1) return false;
		
		String major = jversion.substring(0, firstDot);
		
		// If the string ended at the first dot, it's weird and we give up
		if (firstDot == jversion.length()-1)
			return false;
		
		String rest = jversion.substring(firstDot+1);
		
		int secondDot = rest.indexOf('.');
		String minor = (firstDot == -1) ? 
				rest : 
				rest.substring(0, secondDot);

		try {
			int majorNumber = Integer.parseInt(major);
			int minorNumber = Integer.parseInt(minor);

			if (   (majorNumber == 1 && minorNumber < 2)
			    || (majorNumber < 1))
			    return false;

			// We really are in version >= 1.2

			if (System.getProperty(
			      "org.aspectbench.DebugCflowThreadLocal",
			      "false").equals("true"))
			    System.out.println("Using cflow thread local");

			return true;

		} catch (NumberFormatException e) { return false; }
	}
	
	public static boolean ok() {
		return useThreadLocal;
	}
	
}
