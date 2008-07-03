package jastadd;

import java.util.Iterator;

import AST.BytecodeParser;
import AST.CompilationUnit;
import AST.JavaParser;
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
		program.printJAModules();
		if (!result)
			return false;
		jastAdd.generate();
		return true;
	}

	public void generate() {
		program.generateIntertypeDecls();
		program.transformation();
		for (Iterator iter = program.compilationUnitIterator(); iter.hasNext();) {
			CompilationUnit cu = (CompilationUnit) iter.next();
			if (cu.fromSource()) {
				for (int i = 0; i < cu.getNumTypeDecl(); i++) {
					cu.getTypeDecl(i).generateClassfile();
				}
			}
		}
		// program.generateClassfile();
	}

	protected void initOptions() {
		super.initOptions();
	}
}
