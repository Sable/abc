package arc.aspectj.ast;

import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import java.util.List;

public interface AdviceSpec extends Node
{
    List formals();
    TypeNode returnType();
    Formal returnVal();
}
