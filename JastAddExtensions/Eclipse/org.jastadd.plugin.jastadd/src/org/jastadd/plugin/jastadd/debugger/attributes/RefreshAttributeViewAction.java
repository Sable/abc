package org.jastadd.plugin.jastadd.debugger.attributes;

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;

public class RefreshAttributeViewAction extends Action {

	private AttributeView view;
	
	public RefreshAttributeViewAction(AttributeView view) {
		this.view = view;
		//super(ActionMessages.ShowTypesAction_Show__Type_Names_1, IAction.AS_CHECK_BOX); 
		setToolTipText("Refresh the view."); 
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_TYPE_NAMES));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TYPE_NAMES));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TYPE_NAMES));
	}
		//setId(DebugUIPlugin.getUniqueIdentifier() + ".AttributeV");
	
	@Override
	public void run() {
		//TODO fixme view.setInput((IJavaVariable) view.getViewer().getInput());
	}
	
}
