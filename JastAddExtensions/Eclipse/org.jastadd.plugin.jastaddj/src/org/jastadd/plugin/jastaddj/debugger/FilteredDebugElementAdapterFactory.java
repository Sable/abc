package org.jastadd.plugin.jastaddj.debugger;

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.model.elements.DebugElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.views.launch.DebugElementAdapterFactory;

public class FilteredDebugElementAdapterFactory extends DebugElementAdapterFactory {
	
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType) {
	    if (adapterType.equals(IElementLabelProvider.class) && (adaptableObject instanceof IStackFrame)) {
	    	return new FilteredDebugElementLabelProvider();
	    }
	    return super.getAdapter(adaptableObject, adapterType);
    }
    
	public Class[] getAdapterList() {
		return new Class[]{IElementLabelProvider.class};
	}
}
