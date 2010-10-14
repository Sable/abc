package tests.jigsaw;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import AST.ClassDecl;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class ExtractInterfaceTest extends AbstractRealProgramTest {

	/*
	 * Run1:
	 * 
	 * Extract from every source class declaration
	 * 	- an empty interface
	 *  - an interface containing all public and non inherited methods of that class
	 *  - all interfaces resulting from deletion of a single method from the set of all public and non inherited methods of that class
	 * using a previously unused identifier and package name for the new interface
	 * 
	 * 
	 * Run2:
	 * 
	 * Extract from 
	 *   - the 10 most frequently referenced classes
	 * an interface containing all public and non inherited 
	 * methods of this class into interfaces
	 *  - with same names as all types already present in the program
	 * using a previously unused package name for the new interface
	 */
	
	@Override
	protected void performChanges(Program prog) {
		//Run1
		final String anUnlikelyInterfaceName = "RTT_NEW_INTERFACE";
		final String anUnlikelyPackageName = "RTT_NEW_PACKAGE";
		
		for(ClassDecl classDecl : prog.sourceClassDecls()) {
			for(Collection<MethodDecl> methodSet : computeMethodSets(classDecl)) {
				
				System.out.println();
				System.out.print("extracting interface "+anUnlikelyPackageName+"."+anUnlikelyInterfaceName+" with method set {");
				for(MethodDecl method : methodSet)
					System.out.print(method.getName() + ", ");
				System.out.print("} from " + classDecl.packageName() + "." + classDecl.getName());
				newRun();
				try {						
					Program.startRecordingASTChangesAndFlush();
					classDecl.doExtractInterface(anUnlikelyPackageName, anUnlikelyInterfaceName, methodSet);
					runFinished();
					success(); 
					LinkedList errors = new LinkedList();
					prog.errorCheck(errors);
					if(!errors.isEmpty()){
						error();
						System.out.print("\n Refactoring introduced errors: " + errors);
					}
				} catch(RefactoringException rfe) {
					runFinished();
					System.out.println("  failed (" + rfe.getMessage() + "); ");
				} catch(Error e) {
					e.printStackTrace();
					throw e;
				} finally {
					runFinished();
					Program.undoAll();
					prog.flushCaches();
					checkUndo();
				}
				
			}
		}
		
		//Run2
		for(ClassDecl classDecl : mostReferencedClassDecls(prog,10)) {
			for (ClassDecl classDecl2 : prog.sourceClassDecls()) {
				System.out.println("extracting interface "
						+ anUnlikelyPackageName + "." + classDecl2.getName() + " containing all public non inherited methods from "
						+ classDecl.getName());
				newRun();
				try {
					Program.startRecordingASTChangesAndFlush();
					classDecl.doExtractInterface(anUnlikelyPackageName, classDecl2.getName(), allPublicNonInheritedMethods(classDecl));
					runFinished();
					success();
					LinkedList errors = new LinkedList();
					prog.errorCheck(errors);
					if (!errors.isEmpty()) {
						error();
						System.out.print("\n Refactoring introduced errors: "
								+ errors);
					}
				} catch (RefactoringException rfe) {
					runFinished();
					System.out.println("  failed (" + rfe.getMessage() + "); ");
				} catch (Error e) {
					e.printStackTrace();
					throw e;
				} finally {
					runFinished();
					Program.undoAll();
					prog.flushCaches();
					checkUndo();
				}
			}
		}
		
		for(ClassDecl classDecl : mostReferencedClassDecls(prog, 5)){
			System.out.println(classDecl.getName());
		}
	}

	private Collection<Collection<MethodDecl>> computeMethodSets(TypeDecl typeDecl) {
		Collection<Collection<MethodDecl>> res = new LinkedList<Collection<MethodDecl>>();
		
		Collection<MethodDecl> publicNonInheritedMethods = allPublicNonInheritedMethods(typeDecl);
		
		// empty methods set
		res.add(new LinkedList<MethodDecl>());
		
		// full methods set
		res.add(flatCopy(publicNonInheritedMethods));
		
		// n-1 methods set
		for (MethodDecl methodDecl : publicNonInheritedMethods) {
			LinkedList<MethodDecl> methodList = flatCopy(publicNonInheritedMethods);
			methodList.remove(methodDecl);
			res.add(methodList);
		}
		return res;
	}
	
	private Collection<MethodDecl> allPublicNonInheritedMethods(TypeDecl typeDecl) {
		Collection<MethodDecl> res = new LinkedList<MethodDecl>();
		Iterator<MethodDecl> methodIterator = typeDecl.methodsIterator();
		while(methodIterator.hasNext()){
			MethodDecl method = methodIterator.next();
			if(method.isPublic() && method.getParent(2) == typeDecl)
				res.add(method);
		}
		return res;
	}
	
	private Collection<String[]> computeInterfaceNameCandidates(Program prog) {
		LinkedList<String[]> res = new LinkedList<String[]>();
		for(String packageName : prog.sourcePackageDecls()){
			for(TypeDecl type : prog.sourceClassDecls()) {
				String[] candidate = {packageName,type.getName()};
				res.add(candidate);
			}
		}
		return res;
	}

	private <T> LinkedList<T> flatCopy(Collection<T> toCopy){
		LinkedList<T> res = new LinkedList<T>();
		res.addAll(toCopy);
		return res;
	}
	
	private Collection<ClassDecl> mostReferencedClassDecls(Program prog, int resultLengh) {
		Collection<ClassDecl> res = new LinkedList<ClassDecl>();
		
		class SortableClassDecl implements Comparable<SortableClassDecl> {
			ClassDecl classDecl;
			Integer numberOfReferences;
			
			SortableClassDecl(ClassDecl classDecl){
				this.classDecl = classDecl;
				this.numberOfReferences = classDecl.uses().size();
			}
			@Override
			public int compareTo(SortableClassDecl other) {
				return numberOfReferences.compareTo(other.numberOfReferences);
			}
		}
		
		SortableClassDecl[] sortableClassDecls = new SortableClassDecl[prog.sourceClassDecls().size()];
		int j = 0;
		for (ClassDecl classDecl : prog.sourceClassDecls()) {
			sortableClassDecls[j++] = new SortableClassDecl(classDecl); 
		}                                                  
		Arrays.sort(sortableClassDecls);
		resultLengh = Math.min(resultLengh, sortableClassDecls.length);
		
		int pos = sortableClassDecls.length-1;
		for(int i = 0; i<resultLengh; i++){
			res.add(sortableClassDecls[pos--].classDecl);
		}
		return res;
	}
}
