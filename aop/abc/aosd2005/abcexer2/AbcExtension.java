/*
 * Created on 09-Feb-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package abcexer2;

import java.util.Collection;
import java.util.List;

import abc.aspectj.parse.AbcLexer;
import abc.aspectj.parse.LexerAction_c;
import abcexer2.weaving.matching.ArrayGetShadowMatch;

/**
 * @author sascha
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
}
