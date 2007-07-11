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
