package abc.weaving.aspectinfo;

import polyglot.types.SemanticException;
import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

import soot.*;
import abc.main.Main;
import abc.polyglot.util.ErrorInfoFactory;
import abc.soot.util.Restructure;
import abc.weaving.matching.*;
import abc.weaving.residues.Residue;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.weaver.AroundWeaver;
import abc.soot.util.LocalGeneratorEx;

/** Advice specification for around advice. */
public class AroundAdvice extends AbstractAdviceSpec {
    private AbcType rtype;
    private MethodSig proceed;

    public AroundAdvice(AbcType rtype, MethodSig proceed, Position pos) {
	super(pos);
	this.rtype = rtype;
	this.proceed = proceed;
    }

    public AbcType getReturnType() {
	return rtype;
    }

    /** get the signature of the dummy placeholder method that is called
     *  as a representation of proceed calls inside this around advice.
     */
    public MethodSig getProceedImpl() {
	return proceed;
    }

    public String toString() {
	return rtype+" around";
    }

    private void reportError(String s, ShadowMatch sm) {
    	abc.main.Main.v().error_queue.enqueue
	    (ErrorInfoFactory.newErrorInfo
	     (ErrorInfo.SEMANTIC_ERROR,
	      s,
	      sm.getContainer(),
	      sm.getHost()));
    	//Main.v().error_queue.enqueue(
			//	ErrorInfo.SEMANTIC_ERROR, s);
    }
    public Residue matchesAt(WeavingEnv we,ShadowMatch sm,AbstractAdviceDecl ad) {
    	if (!sm.supportsAround()) {
	    // FIXME: should be a multi-position error
	    if(ad instanceof AdviceDecl) 
		abc.main.Main.v().error_queue.enqueue
		    (ErrorInfoFactory.newErrorInfo
		     (ErrorInfo.WARNING,
		      sm.joinpointName()+" join points do not support around advice, but some advice "
		      +"from aspect "+ad.getAspect().getName()+" would otherwise apply here",
		      sm.getContainer(),
		      sm.getHost()));
	    return null;
    	}
    	Type shadowType=sm.getReturningContextValue().getSootType();
    	Type originalShadowType=shadowType;
    	if (shadowType.equals(NullType.v()))
    		shadowType=VoidType.v();
    	Type adviceType=getReturnType().getSootType();
    	if (adviceType.equals(NullType.v())) throw new InternalCompilerError("");
    	
    	try {
    		checkTypes(shadowType, adviceType);
    	} catch (RuntimeException e) {
    		reportError(e.getMessage() +
					"***  Shadow type: " + originalShadowType + " Advice Type: " + adviceType
					+ " ShadowMatch: " + sm.toString() + 
					  " Type: " + sm.getClass().getName()
					,sm);
    		return null; // don't weave if type error
    	}
    	
    	return AlwaysMatch.v;
    }

	/**
	 * @param shadowType
	 * @param adviceType
	 * @throws SemanticException
	 * 
	 * 
	 */
    // TODO: verify that this is the desired type check
	private void checkTypes(Type shadowType, Type adviceType) {
		Type objectType=Scene.v().getSootClass("java.lang.Object").getType();
    	if (adviceType.equals(objectType)) {
    		// object type advice can always be applied
    	} else {
    		final boolean bVoidAdvice=adviceType.equals(VoidType.v());
    		final boolean bVoidShadow=shadowType.equals(VoidType.v());
    		if (bVoidAdvice && !bVoidShadow)
    			throw new RuntimeException(
    					"Can't apply around advice with void return type to a non void shadow.");
			
    		if (!bVoidAdvice && bVoidShadow)
    			throw new RuntimeException(
    					"Can't apply around advice with non-object non-void return type to a void shadow");
			
    		FastHierarchy hier=Scene.v().getOrMakeFastHierarchy();
    		if (bVoidAdvice && bVoidShadow) {
    			// 
    		} else {
    			if (shadowType instanceof PrimType || adviceType instanceof PrimType) {
    				if (!(shadowType instanceof PrimType && adviceType instanceof PrimType))
    					throw new RuntimeException("Can't convert between primitive type and reference type");
    				
    				if (!Restructure.JavaTypeInfo.isSimpleWideningConversion(adviceType, shadowType))
    					throw new RuntimeException("Illegal narrowing cast");
    			} else {
    				if (hier.canStoreType(adviceType, shadowType)) {
    					
    				} else 
    					throw new RuntimeException("Advice return type can't be converted");
    			}
    			if (Restructure.JavaTypeInfo.isForbiddenConversion(shadowType, adviceType))
    				throw new RuntimeException("Incompatible types");
    		}
    	}
	}
    
    public void weave(SootMethod method,LocalGeneratorEx localgen,AdviceApplication adviceappl) {
	AroundWeaver.doWeave(method.getDeclaringClass(),method,localgen,adviceappl);
    }
}
