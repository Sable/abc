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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;


/**
 * Abstract base class for label shadow switches.
 *
 * @author Eric Bodden
 */
public abstract class AbstractLabelShadowSwitch implements Runnable {

	protected Set disabled = new HashSet();
	
	public abstract void run();

	/**
	 * @param className
	 * @return true if the tracematch is now enabled or false if it is not
	 */
	protected boolean switchTraceMatch(String className) {
		boolean isEnabled = !disabled.contains(className);
		try {
			Class cl = Class.forName(className);
			
			String methodName;
			if(isEnabled) {
				methodName = "disableLabelShadows";
				disabled.add(className);
			} else {
				methodName = "enableLabelShadows";
				disabled.remove(className);
			}

			Method aspectOfMethod = cl.getMethod("aspectOf",new Class[0]);
			Object aspectInstance = aspectOfMethod.invoke(null,new Object[0]);
			
			Method method = cl.getMethod(methodName,new Class[0]);
			method.invoke(aspectInstance,new Object[0]);
						
			isEnabled = !isEnabled;
			
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found: "+e.getMessage());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} 
		
		return isEnabled;
	}

}