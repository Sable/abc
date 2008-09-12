package AST;

import java.util.Stack;

public class Main {
    public static final boolean DEBUG = false;
	
	private static Stack<MemoLine> memostack = new Stack<MemoLine>();
	private static MemoLine curline = null;
	
	public static void pushMemoLine(MemoLine line) {
		memostack.push(curline=line);
	}
	public static void popMemoLine() {
		memostack.pop();
		curline = memostack.isEmpty()? null : memostack.peek();
	}
	public static void registerDependency(Dependency dep, Object val) {
		if (curline != null) {
			if(dep.node.getMemoLine() != curline) {
			    /*if(curline.busy())
			      throw new RuntimeException("adding dependency to busy memo line");*/
				curline.add(dep, val);
			}
		}
	}

}
