package abc.ja.jpi.weaving;

import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import soot.FastHierarchy;
import soot.NullType;
import soot.PrimType;
import soot.Scene;
import soot.Type;
import soot.VoidType;
import abc.ja.jpi.jrag.JPITypeDecl;
import abc.soot.util.Restructure;
import abc.weaving.aspectinfo.AbcType;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.MethodSig;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;

public class GenericCJPAroundAdvice extends CJPAroundAdvice {

	public GenericCJPAroundAdvice(AbcType rtype, MethodSig proceed,
			Position pos, JPITypeDecl jpiDecl) {
		super(rtype, proceed, pos, jpiDecl);
	}
		
	public void checkTypes(Type shadowType, Type adviceType) {
		Type objectType = Scene.v().getSootClass("java.lang.Object").getType();
		if (adviceType.equals(objectType)) {
			// object type advice can always be applied
		} 
		else {
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
			} 
			else {
				if (shadowType instanceof PrimType
						|| adviceType instanceof PrimType) {
					if (!(shadowType instanceof PrimType && adviceType instanceof PrimType))
						throw new RuntimeException(
								"Can't convert between primitive type and reference type");
	
					if (!Restructure.JavaTypeInfo.isSimpleWideningConversion(
							adviceType, shadowType))
						throw new RuntimeException("Illegal narrowing cast");
				} 
				else {
					//here the parameter order is inverted
					if (hier.canStoreType(shadowType, adviceType)){
						
					}
					else
						throw new RuntimeException(
						"Advice return type can't be converted");
	
				}
				if (Restructure.JavaTypeInfo.isForbiddenConversion(shadowType,
						adviceType))
					throw new RuntimeException("Incompatible types");
			}
		}
	}
}
