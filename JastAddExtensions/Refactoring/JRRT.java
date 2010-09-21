import java.util.Collection;
import java.util.Iterator;

import tests.CompileHelper;
import AST.MonitorAction;
import AST.Program;
import AST.TypeDecl;


public class JRRT {
	public static void main(String[] args) {
		long start, end;
		if(args[0].equals("RenameToplevelType")) {
			String pkg = args[1];
			String tp = args[2];
			String n = args[3];
			Program p = compile(args, 4);
			TypeDecl td = p.findType(pkg, tp);
			assert tp != null;
			start = System.currentTimeMillis();
			td.rename(n);
			end = System.currentTimeMillis();
			System.out.println(end-start);
		} else {
			System.out.println("unknown refactoring");
			return;
		}
	}
	
	public static Program compile(String[] strs, int offset) {
		String[] args = new String[strs.length-offset];
		System.arraycopy(strs, offset, args, 0, args.length);
		long start = System.currentTimeMillis();
		Program p = CompileHelper.process(args);
		if(p == null) {
			System.err.println("didn't compile");
			System.exit(-1);
		}
		long end = System.currentTimeMillis();
		System.out.println("time to compile: "+(end-start)+"ms");
		return p;
	}
}
