package abc.ja.jpi.weaving;

import polyglot.util.ErrorInfo;
import polyglot.util.Position;
import soot.SootMethod;
import abc.ja.jpi.jrag.ExhibitBodyDecl;
import abc.ja.jpi.jrag.JPITypeDecl;
import abc.ja.jpi.jrag.TypeAccess;
import abc.polyglot.util.ErrorInfoFactory;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.aspectinfo.AbcType;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AbstractAfterAdvice;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.MethodSig;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;

public abstract class CJPAbstractAfterAdvice extends AbstractAfterAdvice {

	private JPITypeDecl jpiTypeDecl;
	
	public CJPAbstractAfterAdvice(Position pos, TypeAccess jpiTypeAccess) {
		super(pos);
		jpiTypeDecl = (JPITypeDecl)jpiTypeAccess.decl();
	}

	private void reportWarning(ShadowMatch sm, AbstractAdviceDecl ad){
		for(ExhibitBodyDecl exhibitDecl : jpiTypeDecl.getExhibitDecls()){
			abc.main.Main
			.v()
			.getAbcExtension()
			.reportError(
					ErrorInfoFactory.newErrorInfo(
							ErrorInfo.WARNING,
							sm.joinpointName()
									+ " join points do not support around advice, but some exhibit clause "
									+ "from " + exhibitDecl.positionInfo()
									+ " would otherwise apply here",
							sm.getContainer(), sm.getHost()));
		}
		
	}
	
    public Residue matchesAt(WeavingEnv we,ShadowMatch sm,AbstractAdviceDecl ad) {
        if(sm.supportsAfter()) return AlwaysMatch.v();
        // FIXME: should be a multi-position error
        if(ad instanceof AdviceDecl)
            reportWarning(sm, ad);

        return NeverMatch.v();
    }

	
}
