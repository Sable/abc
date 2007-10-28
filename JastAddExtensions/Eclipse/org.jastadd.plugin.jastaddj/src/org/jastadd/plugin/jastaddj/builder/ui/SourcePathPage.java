package org.jastadd.plugin.jastaddj.builder.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.Pattern;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.SourcePathEntry;

class SourcePathPage {
	private JastAddJBuildConfigurationPropertyPage mainPage;
	
	SourcePathPage(JastAddJBuildConfigurationPropertyPage mainPage) {
		this.mainPage = mainPage;
	}
	
	Control getControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(1, false));

		Label sourceTreeLabel = new Label(composite, SWT.LEFT);
		sourceTreeLabel.setText("Source pat&h:");

		Composite sourceComposite = new Composite(composite, SWT.NONE);
		sourceComposite.setFont(parent.getFont());
		GridLayout sourceCompositeLayout = new GridLayout(2, false);
		sourceCompositeLayout.marginWidth = 0;
		sourceCompositeLayout.marginHeight = 0;
		sourceComposite.setLayout(sourceCompositeLayout);

		UIUtil.stretchControl(sourceComposite);

		Tree sourceTree = new Tree(sourceComposite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL);
		sourceTree.setFont(parent.getFont());
		UIUtil.stretchControl(sourceTree);
		final TreeViewer sourceViewer = new TreeViewer(sourceTree);
		sourceViewer.setContentProvider(new SourceContentProvider());
		sourceViewer.setLabelProvider(new SourceLabelProvider());
		sourceViewer.setInput(mainPage.buildConfiguration);

		Control buttonControl = new AddEditRemoveField(sourceViewer, new String[] {"&Add", "&Edit", "&Remove"}) {
			protected boolean hasSelection() {
				return getSelectedSourceEntry(sourceViewer) != null;
			}
			protected void addCommand() {
				sourceEntryAddCommand(sourceViewer);
			}
			protected void editCommand() {
				sourceEntryEditCommand(sourceViewer, getSelectedSourceEntry(sourceViewer));
							
			}
			protected void removeCommand() {
				sourceEntryRemoveCommand(sourceViewer, getSelectedSourceEntry(sourceViewer));
			}			
		}.getControl(sourceComposite);
		
		GridData buttonControlGridData = new GridData();
		buttonControlGridData.verticalAlignment = SWT.TOP;
		buttonControlGridData.verticalSpan = 2;
		buttonControl.setLayoutData(buttonControlGridData);

		Label outputFolderLabel = new Label(composite, SWT.LEFT);
		outputFolderLabel.setText("&Output folder:");

		final Text outputFolderControl = new Text(composite, SWT.BORDER);
		outputFolderControl.setFont(parent.getFont());
		if (mainPage.buildConfiguration.outputPath != null)
			outputFolderControl.setText(mainPage.buildConfiguration.outputPath);
		GridData buildConfigurationGridData = new GridData();
		buildConfigurationGridData.horizontalAlignment = SWT.FILL;
		outputFolderControl.setLayoutData(buildConfigurationGridData);
		outputFolderControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = outputFolderControl.getText();
				if (text.length() > 0)
					mainPage.buildConfiguration.outputPath = text;
				else
					mainPage.buildConfiguration.outputPath = null;
				mainPage.hasChanges = true;
			}
		});

		return composite;
	}

	protected class ListWrapper {
		JastAddJBuildConfiguration.SourcePathEntry sourceEntry;

		ListWrapper(JastAddJBuildConfiguration.SourcePathEntry sourceEntry) {
			this.sourceEntry = sourceEntry;
		}
	}

	protected class IncludeWrapper extends ListWrapper {
		IncludeWrapper(JastAddJBuildConfiguration.SourcePathEntry sourceEntry) {
			super(sourceEntry);
		}
	}

	protected class ExcludeWrapper extends ListWrapper {
		ExcludeWrapper(JastAddJBuildConfiguration.SourcePathEntry sourceEntry) {
			super(sourceEntry);
		}
	}
	
	protected Map<JastAddJBuildConfiguration.SourcePathEntry, IncludeWrapper> includeWrapperMap = new HashMap<JastAddJBuildConfiguration.SourcePathEntry, IncludeWrapper>();
	protected Map<JastAddJBuildConfiguration.SourcePathEntry, ExcludeWrapper> excludeWrapperMap = new HashMap<JastAddJBuildConfiguration.SourcePathEntry, ExcludeWrapper>();

	private final class SourceContentProvider implements ITreeContentProvider {	
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof JastAddJBuildConfiguration) {
				return ((JastAddJBuildConfiguration) parentElement).sourcePathList
						.toArray();
			} else if (parentElement instanceof JastAddJBuildConfiguration.SourcePathEntry) {
				JastAddJBuildConfiguration.SourcePathEntry sourceEntry = (JastAddJBuildConfiguration.SourcePathEntry) parentElement;
				
				IncludeWrapper includeWrapper = includeWrapperMap.get(sourceEntry);
				if (includeWrapper == null) {
					includeWrapper = new IncludeWrapper(sourceEntry);
					includeWrapperMap.put(sourceEntry, includeWrapper);
				}
				
				ExcludeWrapper excludeWrapper = excludeWrapperMap.get(sourceEntry);
				if (excludeWrapper == null) {
					excludeWrapper = new ExcludeWrapper(sourceEntry);
					excludeWrapperMap.put(sourceEntry, excludeWrapper);
				}				
				
				return new Object[] {
						includeWrapper,
						excludeWrapper };
			} else
				return new Object[0];
		}

		public Object getParent(Object element) {
			if (element instanceof ListWrapper)
				return ((ListWrapper) element).sourceEntry;
			else
				return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}
	}
	
	private final class SourceLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element instanceof JastAddJBuildConfiguration.SourcePathEntry) {
				JastAddJBuildConfiguration.SourcePathEntry sourceEntry = (JastAddJBuildConfiguration.SourcePathEntry) element;
				return /*(sourceEntry.isRelative ? "" : "> ") +*/ sourceEntry.sourcePath;
			} else if (element instanceof ListWrapper) {
				ListWrapper wrapper = (ListWrapper) element;
				StringBuffer buffer = new StringBuffer();
				if (wrapper instanceof IncludeWrapper)
					buffer.append("Includes: ");
				else if (wrapper instanceof ExcludeWrapper)
					buffer.append("Excludes: ");
				else
					Assert.isTrue(false);

				List<JastAddJBuildConfiguration.Pattern> list = (wrapper instanceof IncludeWrapper) ? wrapper.sourceEntry.includeList : wrapper.sourceEntry.excludeList;
				for (Iterator<JastAddJBuildConfiguration.Pattern> i = list.iterator(); i.hasNext();) {
					buffer.append(i.next().value);
					if (i.hasNext())
						buffer.append(",");
				}
				return buffer.toString();
			}
			return null;
		}
	}

	public JastAddJBuildConfiguration.SourcePathEntry getSelectedSourceEntry(
			TreeViewer viewer) {
		ITreeSelection selection = (ITreeSelection) viewer.getSelection();
		if (selection == null)
			return null;
		Object target = selection.getFirstElement();
		if (target == null)
			return null;
		if (target instanceof JastAddJBuildConfiguration.SourcePathEntry)
			return (JastAddJBuildConfiguration.SourcePathEntry) target;
		else if (target instanceof ListWrapper)
			return ((ListWrapper) target).sourceEntry;
		else
			return null;
	}

	protected void sourceEntryAddCommand(TreeViewer viewer) {
		EditSourcePathEntryDialog dialog = new EditSourcePathEntryDialog(mainPage.getShell(), null,
				true);
		if (dialog.open() == IDialogConstants.OK_ID) {
			SourcePathEntry sourcePathEntry = new SourcePathEntry();
			dialog.update(sourcePathEntry);
			mainPage.buildConfiguration.sourcePathList.add(sourcePathEntry);
			viewer.refresh();
			mainPage.hasChanges = true;
		}	
	}

	protected void sourceEntryEditCommand(TreeViewer viewer,
			JastAddJBuildConfiguration.SourcePathEntry sourceEntry) {
		JastAddJBuildConfiguration.SourcePathEntry sourcePathEntry = getSelectedSourceEntry(viewer);
		EditSourcePathEntryDialog dialog = new EditSourcePathEntryDialog(mainPage.getShell(), 
				sourcePathEntry, false);
		if (dialog.open() == IDialogConstants.OK_ID) {
			dialog.update(sourcePathEntry);
			viewer.refresh(sourcePathEntry);
			mainPage.hasChanges = true;
		}
	}

	protected void sourceEntryRemoveCommand(TreeViewer viewer,
			JastAddJBuildConfiguration.SourcePathEntry sourceEntry) {
		mainPage.buildConfiguration.sourcePathList.remove(sourceEntry);
		this.includeWrapperMap.remove(sourceEntry);
		this.excludeWrapperMap.remove(sourceEntry);
		viewer.refresh();
		mainPage.hasChanges = true;
	}
}
