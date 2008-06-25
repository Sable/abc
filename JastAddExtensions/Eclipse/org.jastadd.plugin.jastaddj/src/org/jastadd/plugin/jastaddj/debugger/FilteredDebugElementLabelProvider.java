package org.jastadd.plugin.jastaddj.debugger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.model.elements.DebugElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;
import org.eclipse.jface.viewers.TreePath;

/**
 * Deals with renaming stack frame entries.
 * Also ensures we don't spend time stepping through invalid stack frames.
 * In all other respects works like DebugElementLabelProvider 
 * @author luke
 *
 */
public class FilteredDebugElementLabelProvider extends
		DebugElementLabelProvider {
	
	protected String getLabel(TreePath elementPath, IPresentationContext presentationContext, String columnId) throws CoreException {
		Object element = elementPath.getLastSegment();
		if (element instanceof IJavaStackFrame) {
			IJavaStackFrame stackFrame = (IJavaStackFrame) element;
			String currentName = stackFrame.getName();
			JDIModelPresentation presentation = new JDIModelPresentation();
			
			// We consider those stackframes beginning with impl$body to be
			// renameable.
			if (currentName.startsWith("impl$body$")) {
				stackFrame = new RenameDelegateMethodStackFrame(stackFrame);
			}

			return presentation.getText(stackFrame);
		}
		return super.getLabel(elementPath, presentationContext, columnId);
	}

}
