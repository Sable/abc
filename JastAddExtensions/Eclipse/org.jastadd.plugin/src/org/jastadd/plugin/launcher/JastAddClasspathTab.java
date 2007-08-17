package org.jastadd.plugin.launcher;

import java.util.ArrayList;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jdt.internal.debug.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

public class JastAddClasspathTab extends AbstractLaunchConfigurationTab {

	// -- Widget stuff --
	private List content;
	
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		comp.setLayout(topLayout);		
		GridData gd;
		
		Label label = new Label(comp, SWT.NONE);
		label.setText(LauncherMessages.JavaClasspathTab_0); 
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		
		content = new List(parent, SWT.SINGLE);
		ListViewer viewer = new ListViewer(content);
		viewer.setLabelProvider(new JastAddLabelProvider());
		viewer.getControl().setFont(font);
		
		Composite pathButtonComp = new Composite(comp, SWT.NONE);
		GridLayout pathButtonLayout = new GridLayout();
		pathButtonLayout.marginHeight = 0;
		pathButtonLayout.marginWidth = 0;
		pathButtonComp.setLayout(pathButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		pathButtonComp.setLayoutData(gd);
		pathButtonComp.setFont(font);
	
		Button moveUpButton = createPushButton(pathButtonComp,ActionMessages.MoveUpAction_Move_U_p_1, null);
		Button moveDownButton = createPushButton(pathButtonComp, ActionMessages.MoveDownAction_M_ove_Down_1, null);
		Button removeButton = createPushButton(pathButtonComp, ActionMessages.RemoveAction__Remove_1, null);
		Button addProjectButton = createPushButton(pathButtonComp, ActionMessages.AddProjectAction_Add_Project_1, null);
		Button addJarButton = createPushButton(pathButtonComp, ActionMessages.AddJarAction_Add__JARs_1, null);
		Button addExternalJarButton = createPushButton(pathButtonComp, ActionMessages.AddExternalJar_Add_E_xternal_JARs_1, null);
	}

	public String getName() {
		return LauncherMessages.JavaClasspathTab_Cla_ss_path_3; 
	}

	public static Image getClasspathImage() {
		return JavaDebugImages.get(JavaDebugImages.IMG_OBJS_CLASSPATH);
	}
	
	private class JastAddLabelProvider implements ILabelProvider {

		public Image getImage(Object element) {
			return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_LIBRARY);
		}

		public String getText(Object element) {
			if (element instanceof String) {
				return (String)element;
			}
			return null;
		}

		public void addListener(ILabelProviderListener listener) {
		}
		public void dispose() {
		}
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		public void removeListener(ILabelProviderListener listener) {
		}		
	}

	
	
	// -- Tab life-cycle stuff --
	
	public void initializeFrom(ILaunchConfiguration configuration) {
		content.add("The default classpath");
		content.add("Some other classpath");
		//java.util.List classPathList = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, null);
		
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

}
