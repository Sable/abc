/*
 * Created on 09-Feb-2005
 *
 */
package abcexer2;

import java.util.Collection;
import java.util.List;

import soot.Scene;
import soot.SootClass;
import soot.tagkit.Host;
import abc.aspectj.parse.AbcLexer;
import abc.aspectj.parse.LexerAction_c;
import abc.weaving.matching.SJPInfo;
import abcexer2.weaving.matching.ArrayGetShadowMatch;
import abcexer2.weaving.matching.ExtendedSJPInfo;

/**
 * @author Sascha Kuzins
 */
public class AbcExtension extends abc.main.AbcExtension {
	public abc.aspectj.ExtensionInfo makeExtensionInfo(Collection jar_classes,
			Collection aspect_sources) {
		return new abcexer2.ExtensionInfo(jar_classes, aspect_sources);
	}
	
	protected List/*<ShadowType>*/ listShadowTypes()
    {
        List/*<ShadowType*/ shadowTypes = super.listShadowTypes();

        shadowTypes.add(ArrayGetShadowMatch.shadowType());

        return shadowTypes;
    }

    public void initLexerKeywords(AbcLexer lexer) {
        // Add the base keywords
        super.initLexerKeywords(lexer);
        
        lexer.addPointcutKeyword("arrayget", new LexerAction_c(new Integer(abcexer2.parse.sym.PC_ARRAYGET)));
        
    }
    
    
    
    public void addBasicClassesToSoot()
    {
        super.addBasicClassesToSoot();

        Scene.v().addBasicClass("abcexer2.runtime.reflect.Abcexer2Factory",
                                SootClass.SIGNATURES);
    }

    public String runtimeSJPFactoryClass() {
        return "abcexer2.runtime.reflect.Abcexer2Factory";
    }

    /**
	 * Create a (compile-time) static join point information object that
         * generates code to initialize static join point fields with
         * reflective information about a join point.
	 */
    public SJPInfo createSJPInfo(String kind, String signatureTypeClass,
            String signatureType, String signature, Host host) {
        return new ExtendedSJPInfo(kind, signatureTypeClass, signatureType,
                signature, host);
    }
}
