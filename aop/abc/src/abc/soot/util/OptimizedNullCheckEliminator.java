/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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

package abc.soot.util;

import abc.weaving.aspectinfo.Singleton;
import abc.weaving.weaver.CflowCodeGenUtils;
import soot.SootMethodRef;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.toolkits.annotation.nullcheck.NullCheckEliminator;
import soot.jimple.toolkits.annotation.nullcheck.NullnessAnalysis;

/**
 * An optimized null checl eliminator that knows about certain methods not returning <code>null</code>.
 * @author Eric Bodden
 */
public class OptimizedNullCheckEliminator extends NullCheckEliminator {
	
	public OptimizedNullCheckEliminator() {
		super(factory());
	}
	
	private static AnalysisFactory factory() {
		return new NullCheckEliminator.AnalysisFactory() {
			public NullnessAnalysis newAnalysis(soot.toolkits.graph.UnitGraph g) {
				return new NullnessAnalysis(g) {
					public boolean isAlwaysNonNull(Value v) {
						if (super.isAlwaysNonNull(v))
							return true;
						if (v instanceof InvokeExpr) {
							InvokeExpr ie = (InvokeExpr) v;
							SootMethodRef m = ie.getMethodRef();
							if (m.name().equals("makeJP") && m.declaringClass().getName().equals("org.aspectbench.runtime.reflect.Factory"))
								return true;
							if (CflowCodeGenUtils.isFactoryMethod(m))
								return true;
							if (m.name().equals("aspectOf") && 
									m.isStatic() && 
									m.parameterTypes().size()==0 &&
									abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getAspectFromSootClass(m.declaringClass())!=null && // it's an aspect
									abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getAspectFromSootClass(m.declaringClass()).getPer() instanceof Singleton &&
									m.returnType().equals(m.declaringClass().getType()) // correct return type
									) {
								return true;
							}
						}
						return false;
					}
				};
			}
		};
	}

}
