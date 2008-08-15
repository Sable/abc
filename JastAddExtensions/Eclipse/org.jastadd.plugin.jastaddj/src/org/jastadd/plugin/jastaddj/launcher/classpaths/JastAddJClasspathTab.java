package org.jastadd.plugin.jastaddj.launcher.classpaths;

import java.util.ArrayList;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jdt.internal.debug.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


/** This class is currently not used -- Remove? */
public class JastAddJClasspathTab extends AbstractLaunchConfigurationTab {

	// -- Widget stuff --
	private ClasspathContentProvider content;
	
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
		
		content = new ClasspathContentProvider();
		TreeViewer viewer = new TreeViewer(comp);
		viewer.setLabelProvider(new JastAddLabelProvider());
		viewer.setContentProvider(content);
		viewer.setInput("root");
		viewer.getControl().setFont(font);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		gd.heightHint = viewer.getTree().getItemHeight();
		viewer.getTree().setLayoutData(gd);
		
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
	
	private class ClasspathContentProvider implements ITreeContentProvider {

		private ArrayList list;
		private TreeViewer treeViewer;
		
		public ClasspathContentProvider() {
			list = new ArrayList();
			list.add("Default classpath");
			list.add("Another classpath");
		}
		
		public void add(String path) {
		    list.add(path);
		    /*
			treeViewer.add(null, path);
			treeViewer.setExpandedState(null, true);
			treeViewer.reveal(path);
			
			treeViewer.refresh();
			*/
		}
		
		public Object[] getChildren(Object parentElement) {
			return list.toArray();
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return false;
		}

		public Object[] getElements(Object inputElement) {
			return list.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			treeViewer = (TreeViewer) viewer;
		}		
	}

	
	
	// -- Tab life-cycle stuff --
	
	public void initializeFrom(ILaunchConfiguration configuration) {
		//java.util.List classPathList = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, null);
		
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

}
