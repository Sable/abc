package abc.aspectj.ast;

import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import java.util.List;

public interface AdviceSpec extends Node
{
    List formals();
    TypeNode returnType();
    Formal returnVal();
    String kind();

    public void setReturnType(TypeNode rt);
    public void setReturnVal(Formal rv);

    abc.weaving.aspectinfo.AdviceSpec makeAIAdviceSpec();
}
