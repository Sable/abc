/*
 * Created on Sep 15, 2004
 */
package abc.aspectj.parse;

/**
 * Defines the handling of "per-pointcut" keywords.
 * 
 * @author pavel
 */
public class PerClauseLexerAction_c extends LexerAction_c {
    public PerClauseLexerAction_c(Integer t) {
        super(t);
    }
    public PerClauseLexerAction_c(Integer t, Integer s) {
        super(t, s);
    }

    public int getToken(AbcLexer lexer) {
        lexer.setInPerPointcut(true);
        return super.getToken(lexer);
    }
}
