package tests.jigsaw;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import tests.jigsaw.AbstractRealProgramTest;

import AST.ClassDecl;
import AST.MethodDecl;
import AST.Problem;
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
	protected void performChanges(Program prog, Log log) throws IOException {
		//Run1
		final String freshInterface = "RTT_NEW_INTERFACE";
		final String freshPackage = "RTT_NEW_PACKAGE";
		for(ClassDecl classDecl : prog.sourceClassDecls()) {
			for(Collection<MethodDecl> methodSet : computeMethodSets(classDecl)) {
				log.add(performChanges(prog, classDecl, freshPackage, freshInterface, methodSet));				
			}
		}
		//Run2
		for(ClassDecl classDecl : mostReferencedClassDecls(prog,10)) {
			for (ClassDecl classDecl2 : prog.sourceClassDecls()) {
				log.add(performChanges(prog, classDecl, freshPackage, classDecl2.name(), allPublicNonInheritedMethods(classDecl)));
			}
		}
		
		for(ClassDecl classDecl : mostReferencedClassDecls(prog, 5)){
			System.out.println(classDecl.name());
		}
	}
	
	private LogEntry performChanges(final Program prog, final ClassDecl clazz, final String pkg, final String name, final Collection<MethodDecl> methods) {
		final LogEntry entry = new LogEntry(name());
		entry.addParameter("class", clazz.fullName());
		entry.addParameter("interface package", pkg);
		entry.addParameter("interface name", name);
		StringBuilder methodsList = new StringBuilder();
		for(MethodDecl method : methods)
			methodsList.append(method.name()).append(", ");
		entry.addParameter("methods", methodsList.toString());
		
		final String orig = CHECK_UNDO ? prog.toString() : null;
		
		Thread job = new Thread() {
			@Override
			public void run() {
				entry.startsNow();
				try{
					Program.startRecordingASTChangesAndFlush();
					clazz.doExtractInterface(pkg, name, methods);
					entry.finished();
					LinkedList<Problem> errors = new LinkedList<Problem>();
					prog.errorCheck(errors);
					entry.logErrors(errors);
				} catch(RefactoringException rfe){
					entry.finished(rfe);
				} catch(ThreadDeath td) {
					// might occur in case of timeout
				} catch(Throwable t) {
					entry.finished(t);
				}				
			}
		};
		job.start();
		try {
			job.join(TIMEOUT);
			if(job.isAlive()) {
				entry.logTimeout();
				job.stop();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Program.undoAll();
		prog.flushCaches();
		if(CHECK_UNDO)
			assertEquals("Undo did not succeed",orig, prog.toString());
		return entry;
	}

	@Override
	protected String name() {
		return "extract interface";
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
