package abc.aspectj.visit;

import polyglot.ast.Node;

public interface CflowDepth {
    public Node recordCflowDepth(int depth);
    public int getCflowDepth(); // for debugging
}
