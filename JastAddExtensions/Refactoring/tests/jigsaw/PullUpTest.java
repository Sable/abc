package tests.jigsaw;

import java.util.LinkedList;

import AST.MethodDecl;
import AST.Problem;
import AST.Program;
import AST.RefactoringException;

public class PullUpTest extends AbstractRealProgramTest {
	@Override
	protected void performChanges( final Log log) throws Exception {
		final Program prog = getProgram();
		final String orig = CHECK_UNDO ? prog.toString() : null;
		for(final MethodDecl method : prog.sourceMethods()){
			final LogEntry entry = new LogEntry(name());
			prog.setLogEntry(entry);
			Thread job = new Thread() {
				@Override
				public void run() {
					entry.addParameter("moved method", method.fullName());
					entry.startsNow();
					try{
						Program.startRecordingASTChangesAndFlush();
						method.doPullUp();
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
			log.add(entry);
			Program.undoAll();
			prog.flushCaches();
			if(CHECK_UNDO)
				assertEquals("Undo did not succeed",orig, prog.toString());
		}
	}
	@Override
	protected String name() {
		return "pull up method";
	}
}
