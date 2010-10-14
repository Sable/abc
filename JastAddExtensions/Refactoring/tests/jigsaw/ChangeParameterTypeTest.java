package tests.jigsaw;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class ChangeParameterTypeTest extends AbstractRealProgramTest {

	@Override
	protected void performChanges(Program prog) {
		Collection<MethodDecl> meths = prog.sourceMethods();
		System.out.println(meths.size()+" source methods");
		for(MethodDecl md : meths) {
			for(int i=0;i<md.getNumParameter();++i) {
				TypeDecl tp = md.getParameter(i).type();
				HashSet<TypeDecl> substitutiontypes = new HashSet<TypeDecl>();
				substitutiontypes.addAll(tp.supertypestransitive());
				//substitutiontypes.addAll(tp.childtypestransitive());
				for(TypeDecl stp : substitutiontypes) {
					System.out.println("refactoring parameter #" + i + " of method " + md.hostType().typeName() + "." + md.signature() + " to " + stp.fullName() + "... ");
					newRun();
					try {						
						Program.startRecordingASTChangesAndFlush();
						md.getParameter(i).changeType(stp);
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
		}	
	}

}
