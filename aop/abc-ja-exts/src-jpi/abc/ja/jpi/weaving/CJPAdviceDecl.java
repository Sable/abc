package abc.ja.jpi.weaving;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.util.ErrorInfo;
import polyglot.util.Position;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.tagkit.Host;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceFileTag;
import soot.tagkit.SourceLnPosTag;
import abc.ja.jpi.jrag.Access;
import abc.ja.jpi.jrag.JPITypeDecl;
import abc.ja.jpi.jrag.TypeAccess;
import abc.ja.jpi.jrag.ExhibitBodyDecl;
import abc.polyglot.util.ErrorInfoFactory;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.AdviceSpec;
import abc.weaving.aspectinfo.Aspect;
import abc.ja.jpi.jrag.ExhibitBodyDecl;
import abc.weaving.aspectinfo.MethodSig;
import abc.weaving.aspectinfo.Pointcut;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.residues.AdviceFormal;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.AndResidue;
import abc.weaving.residues.JoinPointInfo;
import abc.weaving.residues.Load;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;
import abc.weaving.residues.StaticJoinPointInfo;

public class CJPAdviceDecl extends AdviceDecl {

	private JPITypeDecl jpiTypeDecl;
	public CJPAdviceDecl(AdviceSpec spec, Pointcut pc, MethodSig impl,
			Aspect aspct, int jp, int jpsp, int ejp, List methods, Position pos, TypeAccess jpiTypeAccess) {
		super(spec, pc, impl, aspct, jp, jpsp, ejp, methods, pos);
		jpiTypeDecl = (JPITypeDecl)jpiTypeAccess.decl();
	}

    @SuppressWarnings("rawtypes")
	public Residue postResidue(ShadowMatch sm) {
        List/*<SootClass>*/ advicethrown = getImpl().getSootMethod().getExceptions();

        List/*<SootClass>*/ shadowthrown = sm.getExceptions();
        
        
        /*
         * Here we introduce our constrains about the throws clause for both jpi definition and base code.
         */
        
        List/*<SootClass>*/ jpiThrown = new LinkedList();
        for(Access exception : jpiTypeDecl.getExceptions()){
        	jpiThrown.add(((TypeAccess)exception).type().getSootClassDecl());
        }
        if (shadowthrown.size() != jpiTypeDecl.getNumException()){
        	reportGeneralError(sm, shadowthrown, jpiThrown);
        	return NeverMatch.v();
        }
        
        for(Object exception : shadowthrown){        	
        	if (!jpiThrown.contains(exception)){
        		reportBaseCodeError(sm, (SootClass)exception);
        		return NeverMatch.v();
        	}
        }
        for(Object exception : jpiThrown){
        	if (!shadowthrown.contains(exception)){
        		reportError(sm, (SootClass)exception);
        		return NeverMatch.v();
        	}
        }
        //finish our check.

        eachadvicethrow:
        for(Iterator advicethrownit=advicethrown.iterator();
            advicethrownit.hasNext();
            ) {
            SootClass advicethrow=(SootClass) (advicethrownit.next());

            // don't care about unchecked exceptions
            if(Scene.v().getOrMakeFastHierarchy().isSubclass
               (advicethrow,Scene.v().getSootClass("java.lang.RuntimeException"))) continue;

            if(Scene.v().getOrMakeFastHierarchy().isSubclass
               (advicethrow,Scene.v().getSootClass("java.lang.Error"))) continue;

            for(Iterator shadowthrownit=shadowthrown.iterator();
                shadowthrownit.hasNext();
                ) {

                SootClass shadowthrow=(SootClass) (shadowthrownit.next());
                if(Scene.v().getOrMakeFastHierarchy().isSubclass(advicethrow,shadowthrow))
                    break eachadvicethrow;
            }

            // FIXME: this should be a multi-position error
            reportError(sm, advicethrow);
            return NeverMatch.v();

        }

        Residue ret=AlwaysMatch.v();

        // cache the residue in the SJPInfo to avoid multiple field gets?
        // (could do this in the same place we get the JP stuff if we care)

        if(hasJoinPointStaticPart())
            ret=AndResidue.construct
                (ret,new Load
                 (new StaticJoinPointInfo(sm.getSJPInfo()),
                  new AdviceFormal
                  (joinPointStaticPartPos(),
                   RefType.v("org.aspectj.lang.JoinPoint$StaticPart"))));


        if(hasEnclosingJoinPoint())
            ret=AndResidue.construct
                (ret,new Load
                 (new StaticJoinPointInfo(sm.getEnclosing().getSJPInfo()),
                  new AdviceFormal
                  (enclosingJoinPointPos(),
                   RefType.v("org.aspectj.lang.JoinPoint$StaticPart"))));

        if(hasJoinPoint()) {
            ret=AndResidue.construct
                (ret,new Load
                 (new JoinPointInfo(sm),
                  new AdviceFormal
                  (joinPointPos(),
		   abc.weaving.residues.JoinPointInfo.sootType())));
            // make sure the SJP info will be around later for
            // the JoinPointInfo residue
            sm.recordSJPInfo();
        }

        ret=AndResidue.construct
            (ret,getAspect().getPer().getAspectInstance(getAspect(),sm));
        return ret;

    }

    private void reportGeneralError(ShadowMatch sm, List shadowthrown, List jpiThrown) {
    	for(ExhibitBodyDecl exhibitDecl : jpiTypeDecl.getExhibitDecls()){
	        abc.main.Main.v().getAbcExtension().reportError
	        (ErrorInfoFactory.newErrorInfo
	         (ErrorInfo.SEMANTIC_ERROR,
	          "exhibit clause "
	          +exhibitDecl.positionInfo()
	          +" applies here, and throws these exceptions "+throwListToString(jpiThrown)
	          +" which are not already thrown here :" +throwListToString(shadowthrown),
	          sm.getContainer(),
	          sm.getHost()));
    	}    	
	}
    
    
    private String throwListToString(List exceptions){
    	String res = "[";
    	for(Iterator it = exceptions.iterator();it.hasNext();){
    		res = res + it.next().toString();
    		if(it.hasNext())
    			res = res + ",";
    	}
    	return res + "]";
    }

	private void reportError(ShadowMatch sm, SootClass advicethrow) {
    	for(ExhibitBodyDecl exhibitDecl : jpiTypeDecl.getExhibitDecls()){
	        abc.main.Main.v().getAbcExtension().reportError
	        (ErrorInfoFactory.newErrorInfo
	         (ErrorInfo.SEMANTIC_ERROR,
	          "exhibit clause "
	          +exhibitDecl.positionInfo()
	          +" applies here, and throws exception "+advicethrow
	          +" which is not already thrown here",
	          sm.getContainer(),
	          sm.getHost()));
    	}
	}

	private void reportBaseCodeError(ShadowMatch sm, SootClass baseCodeThrow) {
		String filename=((SourceFileTag)sm.getContainer().getDeclaringClass().getTag("SourceFileTag")).getAbsolutePath();
		Host host = sm.getHost();
		String line = "";
		if(host.hasTag("SourceLnPosTag")) {
            SourceLnPosTag slpTag=(SourceLnPosTag) host.getTag("SourceLnPosTag");			
			line = slpTag.startLn()+"|"+slpTag.startPos() + "-" +slpTag.endLn() + "|" + slpTag.endPos();
		}
		else{
            LineNumberTag lnTag=(LineNumberTag) host.getTag("LineNumberTag");
            line = ""+lnTag.getLineNumber();
		}
    	for(ExhibitBodyDecl exhibitDecl : jpiTypeDecl.getExhibitDecls()){
	        abc.main.Main.v().getAbcExtension().reportError
	        (ErrorInfoFactory.newErrorInfo
	         (ErrorInfo.SEMANTIC_ERROR,
	          "Base code at"
              +" ("+filename
              +", line "+line+")"
	          +" throws exception :"+baseCodeThrow
	          +" which is not already declared by "+jpiTypeDecl.name(),
	          sm.getContainer(),
	          sm.getHost()));
    	}
	}
	
	
}
