package tests.jigsaw;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import AST.ClassDecl;
import AST.MethodDecl;
import AST.Problem;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class ExtractInterfaceTest extends AbstractRealProgramTest {

	@Override
	protected void performChanges(Log log, Program prog) throws Exception {
		final String freshInterface = "RTT_NEW_INTERFACE";
		final String freshPackage = "RTT_NEW_PACKAGE";
		for(ClassDecl classDecl : prog.sourceClassDecls()) {
			if(!classDecl.isAnonymous())
				log.add(performChanges(prog, classDecl, freshPackage, freshInterface, allPublicNonStaticNonInheritedMethods(classDecl)));				
		}
	}
	
	private LogEntry performChanges(final Program prog, final ClassDecl clazz, final String pkg, final String name, final Collection<MethodDecl> methods) {
		final LogEntry entry = new LogEntry(name());
		prog.setLogEntry(entry);
		entry.addParameter("class", clazz.fullName());
		entry.addParameter("interface package", pkg);
		entry.addParameter("interface name", name);
		StringBuilder methodsList = new StringBuilder();
		for(MethodDecl method : methods)
			methodsList.append(method.fullName()).append(", ");
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
					prog.clearErrors();
				} catch(RefactoringException rfe){
					entry.finished(rfe);
				} catch(ThreadDeath td) {
					// might occur in case of timeout
				} catch(Throwable t) {
					t.printStackTrace();
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
		return "extract interface pack methods";
	}
	
	private Collection<MethodDecl> allPublicNonStaticNonInheritedMethods(TypeDecl typeDecl) {
		Collection<MethodDecl> res = new LinkedList<MethodDecl>();
		Iterator<MethodDecl> methodIterator = typeDecl.methodsIterator();
		while(methodIterator.hasNext()){
			MethodDecl method = methodIterator.next();
			if(!method.isStatic() && method.isPublic() && method.getParent(2) == typeDecl)
				res.add(method);
		}
		return res;
	}
}
