/*
 * Created on 08-Feb-2005
 *
 */
package abcexer1;

import java.util.Collection;

import abc.aspectj.parse.AbcLexer;
import abc.aspectj.parse.LexerAction_c;

/**
 * @author Sascha Kuzins
 *
 */
public class AbcExtension extends abc.main.AbcExtension {
	
	public abc.aspectj.ExtensionInfo makeExtensionInfo(Collection jar_classes,
			Collection aspect_sources) {
		return new abcexer1.ExtensionInfo(jar_classes, aspect_sources);
	}
	
    public void initLexerKeywords(AbcLexer lexer) {
        // Add the base keywords
        super.initLexerKeywords(lexer);
        
        lexer.addAspectJContextKeyword("surround", new LexerAction_c(new Integer(abcexer1.parse.sym.SURROUND),
        		new Integer(lexer.pointcut_state())));
    }
}
