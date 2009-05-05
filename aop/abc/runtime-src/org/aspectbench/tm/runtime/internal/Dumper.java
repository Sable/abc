/* abc - The AspectBench Compiler
 * Copyright (C) 2009 Eric Bodden
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.aspectj.lang.JoinPoint.StaticPart;

public class Dumper {
	
	protected static PrintWriter out = null;
	
	public static void dump(Object[] objects, StaticPart sp, String methodName) {
		
		if(out==null) {
			try {
				final PrintWriter writer = new PrintWriter(new File("abc.da.rttrace"));
				out = writer;
				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						//close file in the end
						writer.close();
					}
				});
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		
		out.print(methodName);
		out.print(';');
		for(int i=0;i<objects.length;i++) {
			int id = UID.idOf(objects[i]);
			out.print(id);
			out.print(';');
		}
		String fullKind = sp.getKind();
		String[] splitted = fullKind.split("\\$");		
		out.print(splitted[2]);
		out.println();
		out.flush();		
	}

}
