package abc.aspectj.ast;


import polyglot.ast.Node;

public interface DotNamePattern extends NamePattern
{

    public NamePattern getInit();
    public SimpleNamePattern getLast();
   
}
