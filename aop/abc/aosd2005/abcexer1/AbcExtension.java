/*
 * Created on 08-Feb-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package abcexer1;

import java.util.Collection;

import abc.aspectj.parse.AbcLexer;
import abc.aspectj.parse.LexerAction_c;

/**
 * @author sascha
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AbcExtension extends abc.main.AbcExtension {
	
	public abc.aspectj.ExtensionInfo makeExtensionInfo(Collection jar_classes,
			Collection aspect_sources) {
		return new abcexer1.ExtensionInfo(jar_classes, aspect_sources);
	}
	
	   /* (non-Javadoc)
     * @see abc.main.AbcExtension#initLexerKeywords(abc.aspectj.parse.AbcLexer)
     */
    public void initLexerKeywords(AbcLexer lexer) {
                // Add the base keywords
        super.initLexerKeywords(lexer);

       
        // keyword for the "throw" pointcut extension
        lexer.addAspectJKeyword("surround", new LexerAction_c(new Integer(abcexer1.parse.sym.SURROUND)));
    }
}
