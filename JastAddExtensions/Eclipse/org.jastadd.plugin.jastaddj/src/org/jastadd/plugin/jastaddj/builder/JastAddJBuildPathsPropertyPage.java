package org.jastadd.plugin.jastaddj.builder;

import java.util.ArrayList;

import org.eclipse.debug.internal.ui.PixelConverter;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.PropertyPage;

public class JastAddJBuildPathsPropertyPage extends PropertyPage {

	private ClasspathContentProvider content;

	/*
	private List getDefaultClassPath(IProject project) {
		List list= new ArrayList();
		IResource srcFolder;
		IPreferenceStore store= PreferenceConstants.getPreferenceStore();
		String sourceFolderName= store.getString(PreferenceConstants.SRCBIN_SRCNAME);
		if (store.getBoolean(PreferenceConstants.SRCBIN_FOLDERS_IN_NEWPROJ) && sourceFolderName.length() > 0) {
			srcFolder= project.getFolder(sourceFolderName);
		} else {
			srcFolder= project;
		}

		//list.add(new CPListElement(jproj, IClasspathEntry.CPE_SOURCE, srcFolder.getFullPath(), srcFolder));

		IClasspathEntry[] jreEntries= PreferenceConstants.getDefaultJRELibrary();
		list.addAll(getExistingEntries(jreEntries, project));
		return list;
	}
	
	private ArrayList getExistingEntries(IClasspathEntry[] classpathEntries, IProject project) {
		ArrayList newClassPath= new ArrayList();
		for (int i= 0; i < classpathEntries.length; i++) {
			IClasspathEntry curr= classpathEntries[i];
			newClassPath.add(CPListElement.createFromExisting(curr, project));
		}
		return newClassPath;
	}
	*/
	
	@Override
	protected Control createContents(Composite parent) {
		
		noDefaultAndApplyButton();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		composite.setLayout(layout);

		TabFolder folder = new TabFolder(composite, SWT.NONE);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		folder.setFont(composite.getFont());

		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(NewWizardMessages.BuildPathsBlock_tab_libraries);
		item.setImage(JavaPluginImages.get(JavaPluginImages.IMG_OBJS_LIBRARY));
		item.setControl(createLibraryControl(folder));
	
		Dialog.applyDialogFont(composite);
		setValid(true);
		return composite;
	}

	private Control createLibraryControl(Composite parent) {
		
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setFont(parent.getFont());	
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		comp.setLayout(topLayout);
		
		GridData gd;
		
		Label label = new Label(comp, SWT.NONE);
		label.setText(NewWizardMessages.LibrariesWorkbookPage_libraries_label); 
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		content = new ClasspathContentProvider();
		TreeViewer viewer = new TreeViewer(comp);
		viewer.setLabelProvider(new JastAddLabelProvider());
		viewer.setContentProvider(content);
		viewer.setInput("root"); 
		viewer.getControl().setFont(parent.getFont());
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
		pathButtonComp.setFont(parent.getFont());
	
		Button addJarButton = createPushButton(pathButtonComp, NewWizardMessages.LibrariesWorkbookPage_libraries_addjar_button, null);
		addJarButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				handleAddJarButtonSelected();
			}
		});
		
		Button addExtJarButton = createPushButton(pathButtonComp, NewWizardMessages.LibrariesWorkbookPage_libraries_addextjar_button, null);
		addExtJarButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				handleAddExtJarButtonSelected();
			}
		});
		
		Button addVarButton = createPushButton(pathButtonComp, NewWizardMessages.LibrariesWorkbookPage_libraries_addvariable_button, null);
		addVarButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				handleAddVarButtonSelected();
			}
		});
		addVarButton.setEnabled(false);

		Button addLibButton = createPushButton(pathButtonComp, NewWizardMessages.LibrariesWorkbookPage_libraries_addlibrary_button, null);
		addLibButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				handleAddLibButtonSelected();
			}
		});
		addLibButton.setEnabled(false);

		Button addClassFolderButton = createPushButton(pathButtonComp, NewWizardMessages.LibrariesWorkbookPage_libraries_addclassfolder_button, null);
		addClassFolderButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				handleAddClassFolderButtonSelected();
			}
		});
		addClassFolderButton.setEnabled(false);
		
		Label separator = new Label(pathButtonComp, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setFont(parent.getFont());
		separator.setVisible(false);
		gd = new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.verticalAlignment= GridData.BEGINNING;
		gd.verticalIndent= 4;
		separator.setLayoutData(gd);
		
		Button editButton = createPushButton(pathButtonComp, NewWizardMessages.LibrariesWorkbookPage_libraries_edit_button, null);
		editButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				handleEditButtonSelected();
			}
		});
		editButton.setEnabled(false);

		Button removeButton = createPushButton(pathButtonComp, NewWizardMessages.LibrariesWorkbookPage_libraries_remove_button, null);
		removeButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				handleRemoveButtonSelected();
			}
		});
		removeButton.setEnabled(false);
		
		separator = new Label(pathButtonComp, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setFont(parent.getFont());
		separator.setVisible(false);
		gd = new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.verticalAlignment= GridData.BEGINNING;
		gd.verticalIndent= 4;
		separator.setLayoutData(gd);
		
		Button libReplaceButton = createPushButton(pathButtonComp, NewWizardMessages.LibrariesWorkbookPage_libraries_replace_button, null);
		libReplaceButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				handleLibReplaceButtonSelected();
			}
		});
		libReplaceButton.setEnabled(false);
		
		return comp;
	}
	
	public Button createPushButton(Composite parent, String label, Image image) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		if (image != null) {
			button.setImage(image);
		}
		if (label != null) {
			button.setText(label);
		}
		GridData gd = new GridData();
		button.setLayoutData(gd);	
		
		Assert.isNotNull(button);
		Object gd2 = button.getLayoutData();
		if (gd instanceof GridData) {
			
			((GridData)gd2).widthHint= getButtonWidthHint(button);	
			((GridData)gd2).horizontalAlignment = GridData.FILL;	 
		}
		return button;	
	}	
	
	public int getButtonWidthHint(Button button) {
		button.setFont(JFaceResources.getDialogFont());
		PixelConverter converter= new PixelConverter(button);
		int widthHint= converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}

	
	// ---- Handle actions ----
	
	protected void handleLibReplaceButtonSelected() {
		// TODO Auto-generated method stub
	}

	protected void handleRemoveButtonSelected() {
		// TODO Auto-generated method stub
	}

	protected void handleEditButtonSelected() {
		// TODO Auto-generated method stub
	}

	protected void handleAddClassFolderButtonSelected() {
		// TODO Auto-generated method stub
	}

	protected void handleAddLibButtonSelected() {
		// TODO Auto-generated method stub
	}

	protected void handleAddVarButtonSelected() {
		// TODO Auto-generated method stub
	}

	protected void handleAddExtJarButtonSelected() {
		// TODO Auto-generated method stub
	}

	protected void handleAddJarButtonSelected() {
		// TODO Auto-generated method stub
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
	
	private class ClasspathContentProvider implements ITreeContentProvider {

		private ArrayList list;
		private TreeViewer treeViewer;
		
		public ClasspathContentProvider() {
			list = new ArrayList();
			list.add("Default classpath");
			list.add("Another classpath");
		}
		
		public void dispose() {
		}

		public void add(String s) {
			list.add(s);
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			treeViewer = (TreeViewer) viewer;
		}

		public Object[] getChildren(Object parentElement) {
			return null;
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
	}
}
