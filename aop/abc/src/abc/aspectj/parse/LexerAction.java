/*
 * Created on Sep 15, 2004
 */
package abc.aspectj.parse;

/**
 * Classes implement this interface to indicate they can perform the actions
 * required by the lexer when a particular keyword is scanned.
 * 
 * @author pavel
 */
public interface LexerAction {
    /**
     * This function is called when the keyword that is associated with this action
     * is encountered in the lexer. In order to be useful, it should probably make
     * use of some of Lexer_c's public members to produce the necessary side-effects.
     * 
     * @param lexer The lexer object that the action should apply to. It is used for
     *          the calls that produce side-effects. 
     *  
     * @return The token that should be passed on to the parser, as defined in
     *      aspectj.ppg or similar files from extensions to abc.
     */
    public int getToken(AbcLexer lexer);
}
