package org.jastadd.plugin.jastaddj.launcher;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IOutlineNode;
import org.jastadd.plugin.jastaddj.AST.IClassDecl;
import org.jastadd.plugin.jastaddj.AST.IProgram;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.util.BuildUtil;

import org.jastadd.plugin.Activator;

public class JastAddJMainTab extends AbstractLaunchConfigurationTab {

	private Text projText;
	private Text mainClassText;
	private Button projButton;
	private Button mainClassButton;
	
	//private JastAddJModel model;
	
	
	public JastAddJMainTab() {
		super();
		//this.model = model;
	}

	public void createControl(Composite parent) {
  		Font font = parent.getFont();
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		
		GridLayout topLayout = new GridLayout();
		topLayout.verticalSpacing = 0;
		comp.setLayout(topLayout);
		comp.setFont(font);
		
		createProjectPart(comp);
		createVerticalSpacer(comp, 1);
		createMainClassPart(comp);      		
	}
	
	private void createMainClassPart(Composite comp) {
	    Font font = comp.getFont();
	    
		Group mainGroup = new Group(comp, SWT.NONE);
		mainGroup.setText(LauncherMessages.JavaMainTab_Main_cla_ss__4);
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		mainGroup.setLayoutData(gd);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		mainGroup.setLayout(layout);
		mainGroup.setFont(font);
		
		mainClassText = new Text(mainGroup, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		mainClassText.setLayoutData(gd);
		mainClassText.setFont(font);
		mainClassText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		mainClassButton = createPushButton(mainGroup, LauncherMessages.AbstractJavaMainTab_2, null);
		mainClassButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				handleMainClassButtonSelected();
			}
		});
	}

	private void createProjectPart(Composite comp) {
		Font font= comp.getFont();
		Group group = new Group(comp, SWT.NONE);
		group.setText(LauncherMessages.AbstractJavaMainTab_0); 
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setFont(font);
		
		projText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		projText.setLayoutData(gd);
		projText.setFont(font);
		projText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (getProject() == null) {
					mainClassText.setText("");
				}
				updateLaunchConfigurationDialog();
			}
		});
		projButton = createPushButton(group, LauncherMessages.AbstractJavaMainTab_1, null); 
		projButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				handleProjectButtonSelected();
			}
		});
	}
	
	public Image getImage() {
		return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
	}
	
	public String getName() {
		return LauncherMessages.JavaMainTab__Main_19;
	}
	
	
	
	// -- Tab Life-cycle stuff --
	
	public void initializeFrom(ILaunchConfiguration configuration) {
		
		// Fill in the project field
		String projectName = projText.getText().trim();
		if (projectName.length() == 0) {
          try {
			projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");	
		  } 
		  catch (CoreException ce) { }	
        }
		projText.setText(projectName);
		
		// Fill in the main class field
		String mainTypeName = "";
		try {
			mainTypeName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "");
		}//end try 
		catch (CoreException ce) { }	
		mainClassText.setText(mainTypeName);
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projText.getText().trim());
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainClassText.getText().trim());
		configuration.setMappedResources(new IResource[]{ getProject() });
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		System.out.println("JastAddMainTab.setDefault...");
	}

	
	
	// -- Action stuff --
	
	private void handleProjectButtonSelected() {	
		// For now use Javas default label provider
	    ILabelProvider labelProvider= new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
		
	    // Create a dialog
	    ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
	    dialog.setTitle(LauncherMessages.AbstractJavaMainTab_4); 
		dialog.setMessage(LauncherMessages.AbstractJavaMainTab_3);
		
		// Fill dialog with values - currenly all members of the workspace 
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		try {
			dialog.setElements(root.members());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		// Mark the current selection if possible
		IProject project = getProject();
		if (project != null) {
			dialog.setInitialSelections(new Object[] { project });
		}
		
		// If ok get the selected project
		if (dialog.open() == Window.OK) {			
			project = (IProject) dialog.getFirstResult();
		}			
		
		// In case no project was selected do nothing
		if (project == null) {
			return;
		}
		// .. otherwise fill in project name, and remove main class, if changed
		String oldContent = projText.getText();
		String newContent = project.getName();
		if (!oldContent.equals(newContent)) {
		  projText.setText(project.getName());
		  mainClassText.setText("");
		}
	}
	
	protected IProject getProject() {
		String projectName = projText.getText();
		if(projectName == null || projectName.equals(""))
			return null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		return project.exists() ? project : null;
	}
	
	private void handleMainClassButtonSelected() {
		// Only search if a project has been selected 
		IProject project = getProject();
		if (project != null) {
			ILabelProvider labelProvider= new MainTypeLabelProvider();
			ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		    dialog.setTitle(LauncherMessages.JavaMainTab_Choose_Main_Type_11); 
			dialog.setMessage(LauncherMessages.JavaMainTab_Choose_a_main__type_to_launch__12);
			
			//JastAddJBuildConfiguration buildConfiguration = BuildUtil.getBuildConfiguration(project);
			//if (buildConfiguration != null) {
			
			IASTNode projectAST = Activator.getASTRegistry().lookupAST(null, project);
			if (projectAST != null && projectAST instanceof IProgram) {
				IProgram program = (IProgram)projectAST;
				IOutlineNode[] mainTypes = program.mainTypes();
				if (mainTypes.length == 0) {
					// Show message: No main types in this project
					System.out.println("No main types in this project");
					return;
				} 
				dialog.setElements(mainTypes);
			} else {
				dialog.setElements(new IOutlineNode[0]);
			}
			
			if (dialog.open() == Window.CANCEL) {
				return;
			}
			Object[] results = dialog.getResult();	
		    IClassDecl type = (IClassDecl)results[0];
	     	if (type != null) {
	     		String typeText = type.getID();
	     		String packageText = type.packageName();
	     		mainClassText.setText((packageText.length() > 0 ? packageText + "." : "") + typeText);
		    }
		} else {
			// Show message: No project selected
			System.out.println("No valid project selected");
		}
	}
	
	
	private class MainTypeLabelProvider implements ILabelProvider {

		public Image getImage(Object element) {
			if (element instanceof IOutlineNode) {
				IOutlineNode decl = (IOutlineNode)element;
				return decl.contentOutlineImage();
			}
			return null;
		}

		public String getText(Object element) {
			if (element instanceof IClassDecl) {
				IClassDecl decl = (IClassDecl)element;
				String text = decl.packageName();
				if (text.length() > 0)
					text += ".";
				text += decl.getID();
				return text;
			}
			return null;
		}

		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
		}
		public void dispose() {
			// TODO Auto-generated method stub
		}
		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}
		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
		}
	}
}
