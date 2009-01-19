package org.jastadd.plugin.jastaddj.debugger;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.jastadd.plugin.jastaddj.Activator;

/**
 * Ensures we don't stop in a frame which is invalid.
 * @author luke
 *
 */
public class JastAddJDebug implements IDebugEventSetListener {

	public void handleDebugEvents(DebugEvent[] events) {
		for (DebugEvent event : events) {
			if (event.getKind() == DebugEvent.SUSPEND) {
				if (event.getSource() instanceof IJavaThread) {
					IJavaThread thread = (IJavaThread) event.getSource();
					try {
						IStackFrame stackFrame = thread.getTopStackFrame();
						// If we're in an invalid stack frame (one which we don't wish to be seen
						// by the user), we simply return to the preceding frame.
						if (!InterceptFactory.conds.isValid(stackFrame)) {
							stackFrame.getThread().stepReturn();
						}
					} catch (DebugException e) {
						ILog log = Platform.getLog(Activator.getInstance().getBundle());
						log.log(new Status(IStatus.ERROR, Activator.JASTADDJ_PLUGIN_ID, e.getLocalizedMessage(), e));
					}
					
				}
			}
		}
	}

}
