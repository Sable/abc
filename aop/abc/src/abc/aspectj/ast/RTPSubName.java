package abc.aspectj.ast;

/**
 * A reference type pattern that matches all subclasses (..)+
 * @author Oege de Moor
 * @author Aske Simon Christensen
 *
 */
public interface RTPSubName extends RefTypePattern
{

    public NamePattern getNamePattern();

}
