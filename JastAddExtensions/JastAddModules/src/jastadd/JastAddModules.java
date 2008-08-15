//STANDING NOTE: Make VERY sure that all JAModule passes DO NOT 
//TOUCH any AST nodes below CompilationUnit to avoid the REWRITES

package jastadd;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.io.File;

import AST.BytecodeParser;
import AST.BytecodeReader;
import AST.CompilationUnit;
import AST.JavaParser;
import AST.ModuleCompilationUnit;
import AST.Options;

//primitive extension archi, change to something polyglot-like if needed
public class JastAddModules extends JastAdd {

	public static final String INSTANCE_MODULES_OPTION = "-instance-module";
	public static final String DEBUG_OPTION = "-debug";
	public static final String JASTADD_FRAMEWORK = "-jastaddframework";

	public static void main(String args[]) {
		if (!new JastAddModules().compile(args)) {
			System.exit(1);
		}
	}

	// needed to change this, since module CU insertion has to be done before
	// the initial errorcheck due to module qualified names (e.g. m1.Type)
	@Override
	public boolean process(String[] args, BytecodeReader reader,
			JavaParser parser) {
		program.initBytecodeReader(reader);
		program.initJavaParser(parser);

		initOptions();
		processArgs(args);
		
		if (program.options().hasOption(DEBUG_OPTION)) {
			System.out.println("Arguments: ");
			for (int i = 0; i < args.length; i++) {
				System.out.print(args[i]);
				System.out.print("\n");
			}
			System.out.println("End arguments");
		}

		Collection files = program.options().files();

		if (program.options().hasOption("-version")) {
			printVersion();
			return false;
		}
		if (program.options().hasOption("-help") || files.isEmpty()) {
			printUsage();
			return false;
		}

		try {
			for (Iterator iter = files.iterator(); iter.hasNext();) {
				String name = (String) iter.next();
				if (!new File(name).exists())
					System.out.println("WARNING: file \"" + name
							+ "\" does not exist");
				program.addSourceFile(name);
			}

			//MODULE PROCESSING STARTS HERE
			//NOTE TO MAINTAINERS:
			//Try very hard not to insert anything between the start and the end of the 
			//module passes. If you do:
			//  You MUST avoid going down into normal CompilationUnits as that would
			//    trigger rewrites that use lookupType (which in turn relies on the 
			//    ModuleCompilationUnits being properly placed and configured above
			//    their member CUs.
			//  NEVER CALL flushCaches. See rewrites above.
			//  Don't call flushCache unless you Really Know What You're Doing (TM)
			//  Try to always use the compilationUnitIterator() instead of doing
			//    a standard AST traversal. Most passes should not go into uninstantiated
			//    modules.
			boolean result = true;
			Collection errors = new LinkedList();
			Collection warnings = new LinkedList();
			program.initErrHandling(errors, warnings);
			try {
				//collect moduleDecls from package-info.java files
				program.collectPackageInfo();
				program.insertPackageInfoModuleDecl();
				
				//create synthetic MCUs from overrides
				program.insertOverrideMCUs();
				
				// first error pass (does not depend on the MCUs being above the CUs
				program.checkModuleErrorsPass1();
				
				//debug
				StringBuffer msg = null;
				printCUASTBeforeInsert();

				// insert the ModuleCompilationUnits above the CUs that are a
				// member
				result = program.insertModuleCUs();
				if (!result) {
					return false;
				}
				
				//second error pass (depends on MCUs being above their member CUs
				program.checkModuleErrorsPass2();
				
				//check if a submodule reduces the signature of a super module
				//NOTE: No need to checking module signatures, as merge now preserves signatures
				//program.checkModuleSignatures();
				
				//super modules
				result = program.collectSuperModules();
				if (!result) {
					return false;
				}
				
				//add implicit jastadd$framework imports
				result = program.inserJAFrameworkModuleImport();
				if (!result) {
					return false;
				}
				//DEBUG
				printCUASTAfterInsert();

				// generate the ModuleCompilationUnits created by import
				// own/merges
				result = program.generateImportOwn();
				if (!result) {
					return false;
				}
				// flush the program cache to take the newly generated modules
				// into consideration
				// for JAModuleClassPath.Program.modulePackages()
				program.flushCache();

				//collect local module packages
				program.collectLocalModulePackages();
				
				//debug
				printDebugInfoAfterGenerateImportOwn();
				
				//last error check, checks the instances created
				program.checkModuleErrorsPass3();

			} catch (UnrecoverableSemanticError e) {
				System.out.print("Unrecoverable semantic error(s) found.\n");
			}

			program.collectModuleErrors(errors);
			for (Iterator i = errors.iterator(); i.hasNext();) {
				System.out.println(i.next());
			}
			//warnings will be collected by the errorcheck below
			if (errors.size() > 0) {
				return false;
			}
			
			program.setModuleProcessingComplete(true);
			
			//MODULE PROCESSING ENDS HERE

			//debug
			printCollectTypesAndIterator();

			// Error check
			for (Iterator iter = program.compilationUnitIterator(); iter
					.hasNext();) {
				CompilationUnit unit = (CompilationUnit) iter.next();
				if (unit.fromSource()) {
					errors = unit.parseErrors();
					// warnings = new LinkedList();
					// compute static semantic errors when there are no parse
					// errors or
					// the recover from parse errors option is specified
					if (errors.isEmpty()
							|| program.options().hasOption("-recover"))
						unit.errorCheck(errors, warnings);
					if (!errors.isEmpty()) {
						processErrors(errors, unit);
						return false;
					} else {
						//move to end
						//processWarnings(warnings, unit);
						processNoErrors(unit);
					}
				}
			}
			//cu is unused
			processWarnings(warnings, null);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return true;
	}

	private void printCollectTypesAndIterator() {
		if (program.options().hasOption(DEBUG_OPTION)) {

			System.out
					.println("----------MCU collectTypes after import own----------\n");
			System.out.println(program.toStringModuleMemberTypes());

			System.out.print("----------CU iterator----------\n");
			System.out.print(program.toStringCompilationUnitIterator()
					+ "\n");
		}
	}

	private void printDebugInfoAfterGenerateImportOwn() {
		StringBuffer msg;
		if (program.options().hasOption(DEBUG_OPTION)) {
			System.out
					.print("-------------Instance ModuleCompilationUnit------------\n");
			System.out.print(program.getInstanceModuleCU() + "\n");
			System.out
					.print("-----------End Instance ModuleCompilationUnit----------\n");
			msg = new StringBuffer(
					"----------CU AST after generateImportOwn----------\n");
			System.out.print(program.toStringJAModuleCUAST(0, msg)
					+ "\n");
			msg = new StringBuffer(
					"----------Module CU imports after import own----------\n");
			System.out.print(msg);
			System.out.print(program.toStringJAModuleCUImports());

		}
	}

	private void printCUASTAfterInsert() {
		StringBuffer msg;
		if (program.options().hasOption(DEBUG_OPTION)) {
			msg = new StringBuffer(
					"----------CU AST after insert----------\n");
			System.out.print(program.toStringJAModuleCUAST(0, msg));
			msg = new StringBuffer(
					"----------Module CU imports before import own----------\n");
			System.out.print(msg);
			System.out.print(program.toStringJAModuleCUImports());
		}
	}

	private void printCUASTBeforeInsert() {
		StringBuffer msg;
		if (program.options().hasOption(DEBUG_OPTION)) {
			msg = new StringBuffer(
					"----------Module contents----------\n");
			System.out.print(program.toStringJAModules(msg));
			msg = new StringBuffer(
					"----------CU AST before insert----------\n");
			System.out.print(program.toStringJAModuleCUAST(0, msg));
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

		program.generateIntertypeDecls();
		program.transformation();
		
		program.setFrontEndProcessingComplete(true);
		
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
		Options options = program.options();
		// specifies the module that is going to be used to instantiate the
		// generated package names
		options.addKeyValueOption(INSTANCE_MODULES_OPTION);
		options.addKeyOption(DEBUG_OPTION);
		options.addKeyOption(JASTADD_FRAMEWORK);
	}

}
