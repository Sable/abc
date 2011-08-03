package abc.ja.jpi.weaving;

import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import soot.FastHierarchy;
import soot.NullType;
import soot.PrimType;
import soot.Scene;
import soot.Type;
import soot.VoidType;
import abc.ja.jpi.jrag.Access;
import abc.ja.jpi.jrag.CJPAdviceDecl;
import abc.ja.jpi.jrag.ExhibitBodyDecl;
import abc.ja.jpi.jrag.JPITypeDecl;
import abc.ja.jpi.jrag.TypeAccess;
import abc.polyglot.util.ErrorInfoFactory;
import abc.soot.util.Restructure;
import abc.weaving.aspectinfo.AbcType;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.AroundAdvice;
import abc.weaving.aspectinfo.MethodSig;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;

public class CJPAroundAdvice extends AroundAdvice {

	private JPITypeDecl jpiTypeDecl;
	public CJPAroundAdvice(AbcType rtype, MethodSig proceed, Position pos, TypeAccess jpiTypeAccess) {
		super(rtype, proceed, pos);
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
	
	public Residue matchesAt(WeavingEnv we, ShadowMatch sm,
			AbstractAdviceDecl ad) {
		if (!sm.supportsAround()) {
			// FIXME: should be a multi-position error
			if (ad instanceof AdviceDecl)
				reportWarning(sm, ad);
			return NeverMatch.v();
		}

		Type shadowType = sm.getReturningContextValue().getSootType();

		if (shadowType.equals(NullType.v()))
			shadowType = VoidType.v();
		Type adviceType = getReturnType().getSootType();
		if (adviceType.equals(NullType.v()))
			throw new InternalCompilerError("");

		try {
			checkTypes(shadowType, adviceType);
		} catch (RuntimeException e) {
			reportError(sm, shadowType, adviceType, e);
			return NeverMatch.v(); // don't weave if type error
		}

		return AlwaysMatch.v();
	}

	private void checkTypes(Type shadowType, Type adviceType) {
		Type objectType = Scene.v().getSootClass("java.lang.Object").getType();
		if (adviceType.equals(objectType)) {
			// object type advice can always be applied
		} else {
			final boolean bVoidAdvice = adviceType.equals(VoidType.v());
			final boolean bVoidShadow = shadowType.equals(VoidType.v());
			if (bVoidAdvice && !bVoidShadow)
				throw new RuntimeException(
						"Can't apply around advice with void return type to a non void shadow");

			if (!bVoidAdvice && bVoidShadow)
				throw new RuntimeException(
						"Can't apply around advice with non-object non-void return type to a void shadow");

			FastHierarchy hier = Scene.v().getOrMakeFastHierarchy();
			if (bVoidAdvice && bVoidShadow) {
				//
			} else {
				if (shadowType instanceof PrimType
						|| adviceType instanceof PrimType) {
					if (!(shadowType instanceof PrimType && adviceType instanceof PrimType))
						throw new RuntimeException(
								"Can't convert between primitive type and reference type");

					if (!Restructure.JavaTypeInfo.isSimpleWideningConversion(
							adviceType, shadowType))
						throw new RuntimeException("Illegal narrowing cast");
				} else {
					if (hier.canStoreType(adviceType, shadowType)) {

					} else
						throw new RuntimeException(
								"Advice return type can't be converted");
				}
				if (Restructure.JavaTypeInfo.isForbiddenConversion(shadowType,
						adviceType))
					throw new RuntimeException("Incompatible types");
			}
		}
	}

	private void reportError(ShadowMatch sm, Type shadowType, Type adviceType, RuntimeException e) {
		for(ExhibitBodyDecl exhibitDecl : jpiTypeDecl.getExhibitDecls()){
			abc.main.Main
					.v()
					.getAbcExtension()
					.reportError(
							ErrorInfoFactory.newErrorInfo(ErrorInfo.SEMANTIC_ERROR,
									"Invalid application of around advice from "
									+ exhibitDecl.positionInfo() + " : " + e.getMessage()
									+ " (shadow type: " + shadowType
									+ "; advice return type: " + adviceType + ")", 
									sm.getContainer(), 
									sm.getHost()));
			// Main.v().error_queue.enqueue(
			// ErrorInfo.SEMANTIC_ERROR, s);
		}
	}

}
