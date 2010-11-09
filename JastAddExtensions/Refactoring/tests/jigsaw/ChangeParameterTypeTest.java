package tests.jigsaw;

import java.util.LinkedList;

import AST.MethodDecl;
import AST.Problem;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class ChangeParameterTypeTest extends AbstractRealProgramTest {

	@Override
	protected void performChanges(final Log log, final Program prog) throws Exception {
		final String orig = CHECK_UNDO ? prog.toString() : null;
		for(final MethodDecl md : prog.sourceMethods()) {
			for(int i=0;i<md.getNumParameter();++i) {
				final int i_ = i;
				TypeDecl tp = md.getParameter(i).type();
				for(final TypeDecl stp : tp.supertypes()) {
					final LogEntry entry = new LogEntry(name());
					prog.setLogEntry(entry);
					entry.addParameter("method", md.fullName());
					entry.addParameter("parameter #", i+"");
					entry.addParameter("substitution type", stp.fullName());	
					Thread job = new Thread() {
						@Override
						public void run() {
							entry.startsNow();
							try{
								Program.startRecordingASTChangesAndFlush();
								md.getParameter(i_).changeType(stp);
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
					log.add(entry);
					Program.undoAll();
					prog.flushCaches();
					if(CHECK_UNDO)
						assertEquals("Undo did not succeed",orig, prog.toString());
				}
			}
		}	
	}

	@Override
	protected String name() {
		return "change parameter type";
	}
}
