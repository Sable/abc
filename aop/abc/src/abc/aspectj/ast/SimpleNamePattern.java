package abc.aspectj.ast;


import polyglot.ast.Node;

public interface SimpleNamePattern extends NamePattern
{
    public String getPatternString();
    public java.util.regex.Pattern getPattern();

}
