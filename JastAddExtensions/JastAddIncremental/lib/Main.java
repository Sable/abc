package AST;

import java.util.Stack;

public class Main {
	public static final boolean DEBUG = false;
	
	private static Stack<MemoLine> memostack = new Stack<MemoLine>();
	private static MemoLine curline;
	static { memostack.push(curline=new MemoLine()); }
	
	public static void pushMemoLine(MemoLine line) {
		memostack.push(curline=line);
	}
	public static void popMemoLine() {
		memostack.pop();
		curline = memostack.peek();
	}
	public static void registerDependency(Dependency dep, Object val) {
		curline.add(dep, val);
	}

}
