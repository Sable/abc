/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
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
package abc.weaving.weaver;

import java.util.Iterator;

import soot.SootClass;
import soot.SootMethod;
import abc.main.Main;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;

/**
 * Class for easy traversal of all advice applications of all weavable methods.
 * Usage:
 * <code>
 * 		AdviceApplicationVisitor.v().traverse(new AdviceApplicationVisitor.AdviceApplicationHandler() {
 *  		public void adviceApplication(AdviceApplication aa, SootMethod m) {
 *			  // process it
 *			}
 *		};
 * </code>
 * @author Eric Bodden
 */
public class AdviceApplicationVisitor {
	
	protected static AdviceApplicationVisitor instance;
	
	private AdviceApplicationVisitor() {}
		
	/**
	 * Traverses all advice applications of all weavable classes
	 * calling the given {@link AdviceApplicationHandler}.
	 * @param aah an {@link AdviceApplicationHandler}
	 */
	public final void traverse(AdviceApplicationHandler aah) {
	
		GlobalAspectInfo gai = Main.v().getAbcExtension().getGlobalAspectInfo();
		for (Iterator classIter = gai.getWeavableClasses().iterator(); classIter.hasNext();) {
			AbcClass abcClass = (AbcClass) classIter.next();
			SootClass c = abcClass.getSootClass();
			
			for (Iterator methodIter = c.methodIterator(); methodIter.hasNext();) {
				SootMethod m = (SootMethod) methodIter.next();
				
				MethodAdviceList adviceList = gai.getAdviceList(m);
				
				if(adviceList!=null) {
					for (Iterator aaIter = adviceList.allAdvice().iterator(); aaIter.hasNext();) {
						AdviceApplication aa = (AdviceApplication) aaIter.next();
						
						aah.adviceApplication(aa,m);
						
					}
				}
				
			}
			
		}
		
	}
	
	/**
	 * @return the singleton instance
	 */
	public static AdviceApplicationVisitor v() {
		if(instance==null) {
			instance = new AdviceApplicationVisitor();
		}
		return instance;
	}
	
	/**
	 * Callback for advice applications.
	 * @author Eric Bodden
	 */
	public interface AdviceApplicationHandler {
		
		/**
		 * This method is called once for each
		 * advice application.
		 * @param aa the advice application
		 * @param m the container method
		 */
		public void adviceApplication(AdviceApplication aa, SootMethod m);
		
	}
}
