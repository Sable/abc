/*
 * Created on Sep 15, 2004
 */
package abc.aspectj.parse;

/**
 * Default implementation of the LexerAction interface.
 * 
 * This class can be constructed with one or two Integer parameters. The
 * one-argument form calls the two-argument form with null for the second
 * parameter. The first argument is the parser token that should be
 * returned, the second is the state that the lexer should switch to
 * when the corresponding keyword is encountered - no switch is performed
 * if this argument is null. 
 * 
 * @author pavel
 */
public class LexerAction_c implements LexerAction {
    public Integer token;
    public Integer nextState;

    public LexerAction_c(Integer t) {
        this(t, null);
    }
    
    public LexerAction_c(Integer t, Integer s) {
        token = t;
        nextState = s;
    }
    
    public int getToken(AbcLexer lexer) {
        if(nextState != null) {
            lexer.enterLexerState(nextState.intValue());
        }
        return token.intValue();
    }

}
