package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import AST.ASTNode;
import AST.BytecodeParser;
import AST.BytecodeReader;
import AST.CompilationUnit;
import AST.FileRange;
import AST.Frontend;
import AST.JavaParser;
import AST.LocalDeclaration;
import AST.Opt;
import AST.Program;
import AST.Stmt;
import AST.Variable;

public class TestHelper {
	
	public static Program compileWithCP(String cp, String file) {
		Frontend f = new Frontend() { 
			protected void processErrors(Collection errors, CompilationUnit unit) { }
			protected void processWarnings(Collection errors, CompilationUnit unit) { }
		};
		BytecodeReader br = new BytecodeParser();
		JavaParser jp = new JavaParser() {
            public CompilationUnit parse(java.io.InputStream is, String fileName) 
            		throws java.io.IOException, beaver.Parser.Exception {
                return new parser.JavaParser().parse(is, fileName);
            }
		};
		if(f.process(new String[]{"-classpath", cp, file}, br, jp))
			return f.getProgram();
		return null;
	}
	
	public static Program compile(String... files) {
		List<String> sources = new LinkedList<String>();
		List<String> jars = new LinkedList<String>();
		for(String n : files)
			if(n.endsWith(".jar"))
				jars.add(n);
			else
				sources.add(n);
		Frontend f = new Frontend() { 
			protected void processErrors(Collection errors, CompilationUnit unit) { }
			protected void processWarnings(Collection errors, CompilationUnit unit) { }
		};
		BytecodeReader br = new BytecodeParser();
		JavaParser jp = new JavaParser() {
            public CompilationUnit parse(java.io.InputStream is, String fileName) 
            		throws java.io.IOException, beaver.Parser.Exception {
                return new parser.JavaParser().parse(is, fileName);
            }
		};
		if(f.process(createArglist(sources, jars), br, jp))
			return f.getProgram();
		return null;
	}
	
	static String[] createArglist(List<String> sources, List<String> jars) {
		if(jars.size() == 0)
			return sources.toArray(new String[]{});
		StringBuffer classpath = new StringBuffer();
		for(String j : jars) {
			classpath.append(j);
			classpath.append(':');
		}
		classpath.append(".");
		sources.add(0, "-classpath");
		sources.add(1, classpath.toString());
		return sources.toArray(new String[]{});
	}

    static ASTNode findSmallestCoveringNode(ASTNode r, FileRange rng) {
        if(r instanceof CompilationUnit && 
                (((CompilationUnit)r).relativeName() == null ||
                        !((CompilationUnit)r).relativeName().equals(rng.filename)))
            return null;
        for(int i=0;i<r.getNumChild();++i) {
        	ASTNode child = r.getChild(i);
            ASTNode tmp = findSmallestCoveringNode(child, rng);
            if(tmp != null)
                return tmp;
        }
        if(!covers(r, rng))
            return null;
        return r;
    }

    public static boolean covers(ASTNode r, FileRange rng) {
        int start = r.getStart();
        int end = r.getEnd();
        return covers(ASTNode.getLine(start), ASTNode.getColumn(start),
                ASTNode.getLine(end), ASTNode.getColumn(end),
                rng.sl, rng.sc, rng.el, rng.ec);
    }

    public static boolean covers(int sl1, int sc1, int el1, int ec1, int sl2, int sc2, int el2, int ec2) {
        if(sl1 > sl2 || sl1 == sl2 && sc1 > sc2) return false;
        if(el1 < el2 || el1 == el2 && ec1 < ec2) return false;
        return true;
    }
    
    public static boolean rightBefore(ASTNode n, FileRange rng) {
    	int end = n.getEnd();
    	return ASTNode.getLine(end) == rng.sl && ASTNode.getColumn(end) + 1 == rng.sc;
    }
	
    public static boolean rightAfter(FileRange rng, ASTNode n) {
    	int start = n.getStart();
    	return rng.el == ASTNode.getLine(start) && rng.ec + 1 == ASTNode.getColumn(start);
    }
	
    public static char[] wholeFile(String name) throws FileNotFoundException, IOException {
		File rf = new File(name);
		FileReader rfr = new FileReader(rf);
		long l = rf.length();
		char[] buf = new char[(int)l];
		rfr.read(buf);
		rfr.close();
		return buf;
	}

	public static LocalDeclaration findLocalVariable(ASTNode n, String name) {
		if(n == null) return null;
		if(n instanceof LocalDeclaration &&
				((LocalDeclaration)n).getID().equals(name))
			return (LocalDeclaration)n;
		for(int i=0;i<n.getNumChild();++i) {
			LocalDeclaration v = findLocalVariable(n.getChild(i), name);
			if(v != null) return v;
		}
		return null;
	}

	public static Variable findVariable(ASTNode n, String name) {
		if(n == null) return null;
		if(n instanceof Variable && ((Variable)n).name().equals(name))
			return (Variable)n;
		for(int i=0;i<n.getNumChild();++i) {
			Variable v = findVariable(n.getChild(i), name);
			if(v != null) return v;
		}
		return null;
	}
	
	public static Stmt findStmtFollowingComment(CompilationUnit cu, String comment) {
		if (cu == null) return null;
		FileRange fr = cu.findComment(comment);
		if (fr == null) return null;
		return findFollowingStmt(cu,fr.el);
	}
	
	public static Stmt findStmtPrecedingComment(CompilationUnit cu, String comment) {
		if (cu == null) return null;
		FileRange fr = cu.findComment(comment);
		if (fr == null) return null;
		return findPrecedingStmt(cu, fr.el);
	}
	
	public static Stmt findFollowingStmt(ASTNode n, int line) {
		if(n == null) return null;
		if(n instanceof Stmt) {
			int l = ASTNode.getLine(n.getStart());
			if(l == line || l == line+1)
				return (Stmt)n;
		}
		for(int i=0;i<n.getNumChild();++i) {
			Stmt s = findFollowingStmt(n.getChild(i), line);
			if(s != null) return s;
		}
		return null;
	}
	
	public static Stmt findPrecedingStmt(ASTNode n, int line) {
		if(n == null) return null;
		if(n instanceof Stmt) {
			int l = ASTNode.getLine(n.getEnd());
			if(l == line || l == line-1)
				return (Stmt)n;
		}
		for(int i=0;i<n.getNumChild();++i) {
			Stmt s = findPrecedingStmt(n.getChild(i), line);
			if(s != null) return s;
		}
		return null;
	}
	
	public static ASTNode findFirstNodeInside(ASTNode n, FileRange rng) {
		if(n == null || rng == null) return null;
		int start = n.getStart();
		int end = n.getEnd();
		if(!(n instanceof AST.List) && !(n instanceof Opt) &&
				covers(rng.sl, rng.sc, rng.el, rng.ec,
						ASTNode.getLine(start), ASTNode.getColumn(start),
						ASTNode.getLine(end), ASTNode.getColumn(end)))
			return n;
		for(int i=0;i<n.getNumChild();++i) {
			ASTNode s = findFirstNodeInside(n.getChild(i), rng);
			if(s != null) return s;
		}
		return null;
	}
	
	public static ASTNode findLastNodeInside(ASTNode n, FileRange rng) {
		if(n == null || rng == null) return null;
		int start = n.getStart();
		int end = n.getEnd();
		if(!(n instanceof AST.List) && !(n instanceof Opt) &&
				covers(rng.sl, rng.sc, rng.el, rng.ec,
						ASTNode.getLine(start), ASTNode.getColumn(start),
						ASTNode.getLine(end), ASTNode.getColumn(end)))
			return n;
		for(int i=n.getNumChild()-1;i>=0;i--) {
			ASTNode s = findLastNodeInside(n.getChild(i), rng);
			if(s != null) return s;
		}
		return null;
	}

}
