package org.jastadd.plugin.jastadd.properties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.jastadd.plugin.jastadd.properties.FolderList.FileEntry;
import org.jastadd.plugin.jastadd.properties.FolderList.FolderEntry;
import org.jastadd.plugin.jastadd.properties.FolderList.ParserFolderList;
import org.jastadd.plugin.jastadd.properties.FolderList.PathEntry;
import org.jastadd.plugin.jastadd.properties.FolderList.Pattern;
import org.jastadd.plugin.jastaddj.builder.ui.UIUtil;

class FolderListPage implements JastAddBuildConfigurationPropertyPage.IPage {
	private Shell shell;
	private FolderList folderList;
	private String title;
	private boolean hasChanges;
	private IProject project;
	
	FolderListPage(Shell shell, IProject project, FolderList folderList, String title) {
		this.shell = shell;
		this.folderList = folderList;
		this.hasChanges = false;
		this.project = project;
		this.title = title;
	}
	
	public FolderList getFolderList() {
		return folderList;
	}
	
	public String getTitle() {
		return "&" + title + " Files";
	}
	
	public Control getControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(1, false));

		Label titleLabel = new Label(composite, SWT.LEFT);
		titleLabel.setText(title + " File&s:");
		
		Composite folderComposite = new Composite(composite, SWT.NONE);
		folderComposite.setFont(parent.getFont());
		GridLayout folderCompositeLayout = new GridLayout(2, false);
		folderCompositeLayout.marginWidth = 0;
		folderCompositeLayout.marginHeight = 0;
		folderComposite.setLayout(folderCompositeLayout);

		folderComposite.setLayoutData(UIUtil.stretchControl(new GridData()));

		Tree folderTree = new Tree(folderComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		folderTree.setFont(parent.getFont());
		folderTree.setLayoutData(UIUtil.suggestCharSize(UIUtil.stretchControl(new GridData()), parent, 20, 15));
		final TreeViewer folderViewer = new TreeViewer(folderTree);
		folderViewer.setContentProvider(new FolderContentProvider());
		folderViewer.setLabelProvider(new FolderLabelProvider());
		folderViewer.setInput(folderList);
		
		Control buttonControl = new AddEditRemoveReorderField(folderViewer, new String[] {"&Add", "&Edit", "&Remove"}, true) {
			protected boolean hasSelection() {
				return getSelectedFolderEntry(folderViewer) != null;
			}
			protected void addCommand() {
				folderAddCommand(folderViewer);
			}
			protected void editCommand() {
				folderEditCommand(folderViewer, getSelectedFolderEntry(folderViewer));
							
			}
			protected void removeCommand() {
				for (PathEntry entry : getSelectedFolderEntries(folderViewer)) {
					folderRemoveCommand(folderViewer, entry);
				}
			}
			
			protected void downCommand() {
				folderDownCommand(folderViewer, getSelectedFolderEntry(folderViewer));
			}
			
			protected void upCommand() {
				folderUpCommand(folderViewer, getSelectedFolderEntry(folderViewer));
			}

			protected boolean topOfSelection() {
				PathEntry folderEntry = getSelectedFolderEntry(folderViewer);
				return folderEntry != null && folderList.entries().contains(folderEntry) && folderList.entries().indexOf(folderEntry) == 0;
			}

			protected boolean bottomOfSelection() {
				PathEntry folderEntry = getSelectedFolderEntry(folderViewer);
				return folderEntry != null && folderList.entries().contains(folderEntry) && folderList.entries().indexOf(folderEntry) == (folderList.entries().size() - 1);
			}

			protected void addFileCommand() {
				fileAddCommand(folderViewer);
			}			
		}.getControl(folderComposite);
		
		GridData buttonControlGridData = new GridData();
		buttonControlGridData.verticalAlignment = SWT.TOP;
		buttonControlGridData.verticalSpan = 2;
		buttonControl.setLayoutData(buttonControlGridData);
		
		Composite outputComposite = new Composite(composite, SWT.NONE);
		outputComposite.setFont(parent.getFont());
		GridLayout outputCompositeLayout = new GridLayout(3, false);
		outputCompositeLayout.marginWidth = 0;
		outputCompositeLayout.marginHeight = 0;
		outputComposite.setLayout(outputCompositeLayout);
		
		Label outputFolderLabel = new Label(outputComposite, SWT.NONE);
		outputFolderLabel.setText("&Output Folder:");

		final Text outputFolderControl = new Text(outputComposite, SWT.SINGLE | SWT.BORDER);
		outputFolderControl.setFont(parent.getFont());
		if (folderList.getOutputFolder() != null)
			outputFolderControl.setText(folderList.getOutputFolder());
		outputFolderControl.setLayoutData(UIUtil.suggestCharWidth(UIUtil. stretchControlHorizontal(new GridData()), parent, 50));		
		outputFolderControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = outputFolderControl.getText();
				if (text.length() > 0)
					folderList.setOutputFolder(text);
				else
					folderList.setOutputFolder(null);
				hasChanges = true;
			}
		});
		
		final Button browseButton = new Button(outputComposite, SWT.RIGHT);
		SelectionAdapter adapter = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					DirectoryDialog dialog = new DirectoryDialog (shell);
					IPath path = project.getRawLocation();
					if (path == null) {
						IPath projectPath = project.getFullPath();
						path = ResourcesPlugin.getWorkspace().getRoot().getRawLocation().append(projectPath);
					}
					String projectDir = path.toOSString();
					dialog.setFilterPath(projectDir);
					String dir = dialog.open();
				    if (dir != null) {
				    	if (dir.contains(dir)) {
				    		dir = dir.substring(projectDir.length() + 1);
				    	}
				    	outputFolderControl.setText(dir);
				    }
				}
		};
		browseButton.addSelectionListener(adapter);
		browseButton.setText("&Browse...");
		GridData browseControlGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		browseButton.setLayoutData(browseControlGridData);
		

		if (folderList instanceof ParserFolderList) {
			
			Label parserNameLabel = new Label(outputComposite, SWT.LEFT);
			parserNameLabel.setText("&Parser Name:");
			
			final ParserFolderList parserList = (ParserFolderList) folderList;
			final Text parserNameControl = new Text(outputComposite, SWT.SINGLE | SWT.BORDER);
			parserNameControl.setFont(parent.getFont());
			if (parserList.getParserName() != null)
				parserNameControl.setText(parserList.getParserName());
			
			GridData parseNameControlData = new GridData();
			parseNameControlData.horizontalSpan = 2;
			parserNameControl.setLayoutData(UIUtil.suggestCharWidth(UIUtil.stretchControlHorizontal(parseNameControlData), parent, 50));		
			
			parserNameControl.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					String text = parserNameControl.getText();
					if (text.length() > 0)
						parserList.setParserName(text);
					else
						parserList.setParserName(null);
					hasChanges = true;
				}
			});
		}
		
		return composite;
	}
	
	public boolean hasChanges() {
		return hasChanges;
	}
	
	public boolean updateBuildConfiguration() {
		return true;
	}
	
	protected class ListWrapper {
		FolderEntry folderEntry;

		ListWrapper(FolderEntry folderEntry) {
			this.folderEntry = folderEntry;
		}
	}

	protected class IncludeWrapper extends ListWrapper {
		IncludeWrapper(FolderEntry folderEntry) {
			super(folderEntry);
		}
	}

	protected class ExcludeWrapper extends ListWrapper {
		ExcludeWrapper(FolderEntry folderEntry) {
			super(folderEntry);
		}
	}

	protected class FileWrapper extends ListWrapper {
		FileWrapper(FolderEntry folderEntry) {
			super(folderEntry);
		}
		
		private List<Pattern> currentPatterns;
		PatternWrapper[] patternWrappers;
		
		public PatternWrapper[] getPatternWrappers() {
			// We rebuild the list of files only if entries have changed
			// By only rebuilding the list when we need to, we can ensure
			// that if a file is selected when we press up/down,
			// that same file is still selected after the refresh.
			if (currentPatterns == null || !currentPatterns.equals(folderEntry.getFileList())) {
				currentPatterns = folderEntry.getFileList();
				Pattern[] patterns = currentPatterns.toArray(new Pattern[0]);
				patternWrappers = new PatternWrapper[patterns.length];
				for (int i = 0; i < patterns.length; i++) {
					patternWrappers[i] = new PatternWrapper(folderEntry, this, patterns[i]);
				}
			}
			return patternWrappers;
		}
	}
	
	protected class PatternWrapper {
		FolderEntry folderEntry;
		Pattern pattern;
		ListWrapper listWrapper;

		PatternWrapper(FolderEntry folderEntry, ListWrapper listWrapper, Pattern pattern) {
			this.folderEntry = folderEntry;
			this.pattern = pattern;
			this.listWrapper = listWrapper;
		}
	}
	
	protected Map<FolderEntry, IncludeWrapper> includeWrapperMap = new HashMap<FolderEntry, IncludeWrapper>();
	protected Map<FolderEntry, ExcludeWrapper> excludeWrapperMap = new HashMap<FolderEntry, ExcludeWrapper>();
	protected Map<FolderEntry, FileWrapper> fileWrapperMap = new HashMap<FolderEntry, FileWrapper>();

	private final class FolderContentProvider implements ITreeContentProvider {	
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof FolderList) {
				return ((FolderList) parentElement).entries().toArray();
			} else if (parentElement instanceof FolderEntry) {
				FolderEntry folderEntry = (FolderEntry) parentElement;
				
				IncludeWrapper includeWrapper = includeWrapperMap.get(folderEntry);
				if (includeWrapper == null) {
					includeWrapper = new IncludeWrapper(folderEntry);
					includeWrapperMap.put(folderEntry, includeWrapper);
				}
				
				ExcludeWrapper excludeWrapper = excludeWrapperMap.get(folderEntry);
				if (excludeWrapper == null) {
					excludeWrapper = new ExcludeWrapper(folderEntry);
					excludeWrapperMap.put(folderEntry, excludeWrapper);
				}
				
				FileWrapper fileWrapper = fileWrapperMap.get(folderEntry);
				if (fileWrapper == null) {
					fileWrapper = new FileWrapper(folderEntry);
					fileWrapperMap.put(folderEntry, fileWrapper);
				}	
				
				return new Object[] {
						includeWrapper,
						excludeWrapper,
						fileWrapper };
			} else if (parentElement instanceof FileWrapper) {
				FileWrapper fileWrapper = (FileWrapper) parentElement;
				PatternWrapper[] patternWrappers = fileWrapper.getPatternWrappers();
				return patternWrappers;
			} else
				return new Object[0];
		}

		public Object getParent(Object element) {
			if (element instanceof ListWrapper)
				return ((ListWrapper) element).folderEntry;
			else if (element instanceof PatternWrapper)
				return ((PatternWrapper) element).listWrapper;
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
	
	private final class FolderLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element instanceof PathEntry) {
				PathEntry folderEntry = (PathEntry) element;
				return folderEntry.getPath();
			} else if (element instanceof PatternWrapper) {
				return ((PatternWrapper) element).pattern.value;
			} else if (element instanceof ListWrapper) {
				ListWrapper wrapper = (ListWrapper) element;
				StringBuffer buffer = new StringBuffer();
				if (wrapper instanceof IncludeWrapper)
					buffer.append("Includes: ");
				else if (wrapper instanceof ExcludeWrapper)
					buffer.append("Excludes: ");
				else if (wrapper instanceof FileWrapper) {
					buffer.append("Files: ");
					return buffer.toString();
				} else
					Assert.isTrue(false);

				List<Pattern> list = (wrapper instanceof IncludeWrapper) ? wrapper.folderEntry.getIncludeList() : ((wrapper instanceof ExcludeWrapper) ? wrapper.folderEntry.getExcludeList() : wrapper.folderEntry.getFileList());
				for (Iterator<Pattern> i = list.iterator(); i.hasNext();) {
					buffer.append(i.next().value);
					if (i.hasNext())
						buffer.append(",");
				}
				return buffer.toString();
			}
			return null;
		}
	}

	public PathEntry getSelectedFolderEntry(TreeViewer viewer) {
		ITreeSelection selection = (ITreeSelection) viewer.getSelection();
		if (selection == null)
			return null;
		Object target = selection.getFirstElement();
		if (target == null)
			return null;
		if (target instanceof PathEntry)
			return (PathEntry) target;
		else if (target instanceof ListWrapper)
			return ((ListWrapper) target).folderEntry;
		else if (target instanceof PatternWrapper)
			return ((PatternWrapper) target).folderEntry;
		else
			return null;
	}

	public Set<PathEntry> getSelectedFolderEntries(TreeViewer viewer) {
		ITreeSelection selection = (ITreeSelection) viewer.getSelection();
		Set<PathEntry> entries = new HashSet<PathEntry>();
		if (selection == null) {
		} else if (selection.size() == 1) {
			entries.add(getSelectedFolderEntry(viewer));
		} else {
		
			//Object target = selection.getFirstElement();
			for (Object target : selection.toList()) {
				if (target == null) {
				} else if (target instanceof PathEntry)
					entries.add((PathEntry) target);
				else if (target instanceof ListWrapper)
					entries.add(((ListWrapper) target).folderEntry);
				else if (target instanceof PatternWrapper)
					entries.add(((PatternWrapper) target).folderEntry);
			}
		}
		return entries;
	}
	
	protected void fileAddCommand(TreeViewer viewer) {
		FileDialog dialog = new FileDialog(shell, SWT.MULTI);

		String absoluteDirectory = setupAddEditDialog(dialog, "");
		
		String newFile = dialog.open();
		if (newFile != null) {
			// If we've changed the directory, we want to create
			// a relative path to the current directory
			String prefix = "";
			Path absolutePath = new Path(absoluteDirectory);
			Path newPath = new Path(dialog.getFilterPath());
			if (!absolutePath.equals(newPath)) {
				prefix = FileFolderUtils.relativize(absolutePath,newPath);
			}
			
			for (String filePattern : dialog.getFileNames()) {
				FileEntry fileEntry = new FileEntry();
				fileEntry.setPath(prefix + filePattern);
				folderList.add(fileEntry);
			}
			viewer.refresh();
			hasChanges = true;
		}
	}
	
	private String setupAddEditDialog(FileDialog dialog, String fileName) {
		// Set dialog title and text.
		dialog.setText(title + " file selection");

		// Set filters
		dialog.setFilterExtensions(new String[]{folderList.getFilter(), "*.*"});
		dialog.setFilterNames(new String[]{title + " Files", "All files"});
		
		// Get the absolute path of the directory of this entry
		String absoluteDirectory = project.getLocation().addTrailingSeparator().toString();
							
		// Get the relative path of the file and, if it exists, resolve it against
		// the base directory.
		Path filePath = new Path(fileName);
		String file = filePath.lastSegment();
		if (file != null) {
			if (filePath.isAbsolute()) {
				// If the file already set is an absolute path, just use that
				dialog.setFilterPath(filePath.removeLastSegments(1).toString());
			} else {
				// Otherwise calculate a new absolute path from the directory and the relative section
				dialog.setFilterPath(FileFolderUtils.absolutePath(absoluteDirectory + filePath.removeLastSegments(1), project));
			}
			dialog.setFileName(file);
		} else {
			dialog.setFilterPath(absoluteDirectory);
		}
		return absoluteDirectory;
	}
	
	protected void folderAddCommand(TreeViewer viewer) {
		FolderEntry folderEntry = new FolderEntry();
		EditFolderEntryDialog dialog = new EditFolderEntryDialog(shell, project, folderEntry, folderList.getFilter(), title, false);
		if (dialog.open() == IDialogConstants.OK_ID) {
			dialog.update(folderEntry);
			folderList.add(folderEntry);
			viewer.refresh();
			hasChanges = true;
		}
	}

	protected void folderEditCommand(TreeViewer viewer, PathEntry pathEntry) {
		if (pathEntry instanceof FolderEntry) {
			FolderEntry folderEntry = (FolderEntry) pathEntry;
			// Ensure we pass a copy of the folder entry, so that can be changed independently of the real entry.
			// This allows the dialog to cancel the changes.
			EditFolderEntryDialog dialog = new EditFolderEntryDialog(shell, project, folderEntry.copy(), folderList.getFilter(), title, false);
			if (dialog.open() == IDialogConstants.OK_ID) {
				dialog.update(folderEntry);
				viewer.refresh(folderEntry);
				hasChanges = true;
			}
		} else {
			FileEntry fileEntry =(FileEntry) pathEntry;
			FileDialog dialog = new FileDialog(shell, SWT.OPEN);

			String absoluteDirectory = setupAddEditDialog(dialog, fileEntry.getPath());
			
			String newFile = dialog.open();
			if (newFile != null) {
				// If we've changed the directory, we want to create
				// a relative path to the current directory
				String prefix = "";
				Path absolutePath = new Path(absoluteDirectory);
				Path newPath = new Path(dialog.getFilterPath());
				if (!absolutePath.equals(newPath)) {
					prefix = FileFolderUtils.relativize(absolutePath,newPath);
				}
				
				fileEntry.setPath(prefix + dialog.getFileName());
				viewer.refresh();
				hasChanges = true;
			}
		}
	}

	protected void folderRemoveCommand(TreeViewer viewer, PathEntry folderEntry) {
		folderList.remove(folderEntry);
		if (folderEntry instanceof FolderEntry) {
			this.includeWrapperMap.remove(folderEntry);
			this.excludeWrapperMap.remove(folderEntry);
			this.fileWrapperMap.remove(folderEntry);
		}
		viewer.refresh();
		hasChanges = true;
	}
	
	protected void folderUpCommand(TreeViewer viewer, PathEntry folderEntry) {
		folderList.up(folderEntry);
		viewer.refresh();
		hasChanges = true;
	}
	
	protected void folderDownCommand(TreeViewer viewer, PathEntry folderEntry) {
		folderList.down(folderEntry);
		viewer.refresh();
		hasChanges = true;
	}
}
