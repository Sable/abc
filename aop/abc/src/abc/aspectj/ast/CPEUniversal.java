package abc.aspectj.ast;

import java.util.List;

public interface CPEUniversal extends ClassnamePatternExpr
{
    public void addExclude(ClassnamePatternExpr pat);
    public void setExcludes(List excludes);
}
