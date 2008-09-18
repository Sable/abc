package AST;

import java.util.Stack;

public class Main {
    public static final boolean DEBUG = false;
	
    private static Stack<MemoLine> memostack = new Stack<MemoLine>();
    private static MemoLine curline = null;
    public static int last_change = 0;
	
    public static void pushMemoLine(MemoLine line) {
	memostack.push(curline=line);
    }
    public static void popMemoLine() {
	memostack.pop();
	curline = memostack.isEmpty()? null : memostack.peek();
    }
    public static void registerDependency(Dependency dep, Object val) {
	if (curline != null) {
	    if(dep.getCacheRoot() != curline.getCacheRoot() && 
	       !dep.node.fromLibrary())
		curline.add(dep, val);
	}
    }

}
