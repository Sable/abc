package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import AST.ASTNode;
import AST.BytecodeParser;
import AST.BytecodeReader;
import AST.CompilationUnit;
import AST.FileRange;
import AST.Frontend;
import AST.JavaParser;
import AST.Program;

public class TestHelper {
	
	public static Program compile(String... files) {
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
		if(f.process(files, br, jp))
			return f.getProgram();
		return null;
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

    static boolean covers(ASTNode r, FileRange rng) {
        int start = r.getStart();
        int end = r.getEnd();
        return covers(ASTNode.getLine(start), ASTNode.getColumn(start),
                ASTNode.getLine(end), ASTNode.getColumn(end),
                rng.sl, rng.sc, rng.el, rng.ec);
    }

    static boolean covers(int sl1, int sc1, int el1, int ec1, int sl2, int sc2, int el2, int ec2) {
        if(sl1 > sl2 || sl1 == sl2 && sc1 > sc2) return false;
        if(el1 < el2 || el1 == el2 && ec1 < ec2) return false;
        return true;
    }
	
    public static char[] wholeFile(String name) throws FileNotFoundException, IOException {
		File rf = new File(name);
		FileReader rfr = new FileReader(rf);
		long l = rf.length();
		char[] buf = new char[(int)l];
		rfr.read(buf);
		return buf;
	}


}
