package tests.jigsaw;

import java.io.IOException;
import java.util.LinkedList;

import tests.jigsaw.AbstractRealProgramTest;

import AST.Modifier;
import AST.Problem;
import AST.Program;
import AST.RefactoringException;
import AST.Visible;

public class ChangeAccessibilityTest extends AbstractRealProgramTest {
	
	@Override
	protected void performChanges(Program prog, Log log) throws IOException {
		for(Visible visible : prog.sourceVisibles()) {
			if(!visible.isPrivate())
				log.add(run(prog, visible, Modifier.VIS_PRIVATE));
			if(visible.isPrivate() || visible.isProtected() || visible.isPublic())
				log.add(run(prog, visible, Modifier.VIS_PACKAGE));
			if(!visible.isProtected())
				log.add(run(prog, visible, Modifier.VIS_PROTECTED));
			if(!visible.isPublic())
				log.add(run(prog, visible, Modifier.VIS_PUBLIC));
		}
	}
	
	private LogEntry run(final Program prog, final Visible visible, final int accessModifier) {
		final LogEntry entry = new LogEntry(name());
		
		entry.addParameter("declaration", visible.name());
		entry.addParameter("accessibility", Modifier.visibilityToString(accessModifier));
		
		final String orig = CHECK_UNDO ? prog.toString() : null;

		Thread job = new Thread() {
			@Override
			public void run() {
				entry.startsNow();
				try{
					Program.startRecordingASTChangesAndFlush();
					visible.changeAccessibility(accessModifier);
					entry.finished();
					LinkedList<Problem> errors = new LinkedList<Problem>();
					prog.errorCheck(errors);
					entry.logErrors(errors);
				} catch(RefactoringException rfe){
					entry.finished(rfe);
				} catch(ThreadDeath td) {
					// dont mind these, will be caused by stop() in case of timeout
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
		return "change accessibility";
	}
}
