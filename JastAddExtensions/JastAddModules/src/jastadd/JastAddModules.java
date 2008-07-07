package jastadd;

import java.util.Iterator;

import AST.BytecodeParser;
import AST.CompilationUnit;
import AST.JavaParser;
import AST.ModuleCompilationUnit;
import AST.Options;

//primitive extension archi, change to something polyglot-like if needed
public class JastAddModules extends JastAdd  {
	public static void main(String args[]) {
		if (!new JastAddModules().compile(args)) {
			System.exit(1);
		}
	}
	
	public boolean compile(String[] args) {
		JastAdd jastAdd = this;
		boolean result = jastAdd.process(args, new BytecodeParser(),
				new JavaParser() {
					public CompilationUnit parse(java.io.InputStream is,
							String fileName) throws java.io.IOException,
							beaver.Parser.Exception {
						return new parser.JavaParser().parse(is, fileName);
					}
				});
		if (!result)
			return false;
		jastAdd.generate();
		return true;
	}

	public void generate() {
		System.out.println("----------Module contents----------");
		program.printJAModules();
		System.out.println("----------Module AST before insert----------");
		program.printJAModuleCUAST(0);
		program.insertModuleCUs();
		System.out.println("----------Module AST after insert----------");
		program.printJAModuleCUAST(0);
		program.dumpTree();
		
		
		program.generateIntertypeDecls();
		program.transformation();
		for (Iterator iter = program.compilationUnitIterator(); iter.hasNext();) {
			CompilationUnit cu = (CompilationUnit) iter.next();
			if (cu.fromSource()) {
				for (int i = 0; i < cu.getNumTypeDecl(); i++) {
					cu.getTypeDecl(i).generateClassfile();
				}
			}
			//bad hack, fix by refining compilationUnitIterator() in ClassPath.jrag
			//does not work completely, innertypes for ASTNode not generated properly
			if (cu instanceof ModuleCompilationUnit) {
				for (CompilationUnit childCU : ((ModuleCompilationUnit)cu).getCompilationUnitList()) {
					if (childCU.fromSource()) {
						for (int i = 0; i < childCU.getNumTypeDecl(); i++) {
							childCU.getTypeDecl(i).generateClassfile();
						}
					}
				}
			}
		}
		// program.generateClassfile();
	}

	protected void initOptions() {
		super.initOptions();
	}
}
