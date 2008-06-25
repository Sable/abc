package org.jastadd.plugin.jastaddj.debugger;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.ui.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.ui.monitors.JavaThreadContentProvider;
import org.eclipse.jdt.internal.debug.ui.monitors.NoMonitorInformationElement;

/**
 * Taken from JavaThreadContentProvider, mostly. We override certain methods to do stackframe filtering.
 * 
 * Currently less efficient than the original, because we implement getChildCount and hasChildren using the getChildren
 * method, to ensure we consider the removed stack frames.
 * @author luke
 *
 */
public class FilteredJavaThreadContentProvider extends JavaThreadContentProvider {

	StackFrameRemoveCondition cond;
	
	public FilteredJavaThreadContentProvider(StackFrameRemoveCondition cond) {
		this.cond = cond;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getChildCount(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	protected int getChildCount(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		IJavaThread thread = (IJavaThread)element;
		if (!thread.isSuspended()) {
			return 0;
		}
		int childCount = thread.getFrameCount();
		if (isDisplayMonitors()) {
			if (((IJavaDebugTarget) thread.getDebugTarget()).supportsMonitorInformation()) {
				childCount+= thread.getOwnedMonitors().length;
				if (thread.getContendedMonitor() != null) {
					childCount++;
				}
			} else {
				// unavailable notice
				childCount++;
			}
		}
		//return childCount;
		return getChildren(thread).length;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getChildren(java.lang.Object, int, int, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected Object[] getChildren(Object parent, int index, int length, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		IJavaThread thread = (IJavaThread)parent;
		if (!thread.isSuspended()) {
			return EMPTY;
		}
		return getElements(getChildren(thread), index, length);
	}
	
	protected Object[] getChildren(IJavaThread thread) {
		try {
			IStackFrame[] frames = thread.getStackFrames();
			
			ArrayList<IStackFrame> validStackFrames = new ArrayList<IStackFrame>();

			// This next section is code that has been added
			if (frames.length > 0) {
				// We always consider the current stackframe as valid
				validStackFrames.add(frames[0]);
				
				for (int i = 1; i < frames.length; i++) {
					// We check it's a valid frame against the pre-built condition
					if (cond.isValid(frames[i])) {
						validStackFrames.add(frames[i]);
					}
				}	
			
				frames = validStackFrames.toArray(new IStackFrame[0]);
			}
			// This next section is code that has been added
			
			if (!isDisplayMonitors()) {
				return frames;
			}

			Object[] children;
			int length = frames.length;
			if (((IJavaDebugTarget) thread.getDebugTarget()).supportsMonitorInformation()) {
				IDebugElement[] ownedMonitors = JavaDebugUtils.getOwnedMonitors(thread);
				IDebugElement contendedMonitor = JavaDebugUtils.getContendedMonitor(thread);

				if (ownedMonitors != null) {
					length += ownedMonitors.length;
				}
				if (contendedMonitor != null) {
					length++;
				}
				children = new Object[length];
				if (ownedMonitors != null && ownedMonitors.length > 0) {
					System.arraycopy(ownedMonitors, 0, children, 0, ownedMonitors.length);
				}
				if (contendedMonitor != null) {
					// Insert the contended monitor after the owned monitors
					children[ownedMonitors.length] = contendedMonitor;
				}
			} else {
				children = new Object[length + 1];
				children[0] = new NoMonitorInformationElement(thread.getDebugTarget());
			}
			int offset = children.length - frames.length;
			System.arraycopy(frames, 0, children, offset, frames.length);
			return children;
		} catch (DebugException e) {
			return EMPTY;
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#hasChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected boolean hasChildren(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		return getChildren((IJavaThread)element).length != 0;
	}
}
