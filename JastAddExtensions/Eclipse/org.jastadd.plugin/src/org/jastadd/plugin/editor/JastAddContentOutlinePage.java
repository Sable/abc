package org.jastadd.plugin.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.jastadd.plugin.JastAddModel;


import AST.*;

public class JastAddContentOutlinePage extends ContentOutlinePage {
	
	private IEditorInput fInput;
	private IDocumentProvider fDocumentProvider;
	private TextEditor fTextEditor;
	
	public JastAddContentOutlinePage(IDocumentProvider docProvider, TextEditor editor) {
		super();
	    fDocumentProvider = docProvider;
	    fTextEditor = editor;
	}
	
	public void setInput(IEditorInput input) {
		fInput = input;
		update();
	}
	
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer= getTreeViewer();
		viewer.setContentProvider(new JastAddContentProvider());
		viewer.setLabelProvider(new JastAddLabelProvider());
		viewer.addSelectionChangedListener(this);
		if (fInput != null)
			viewer.setInput(fInput);
	}
	
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);
		ISelection selection= event.getSelection();
		if (selection.isEmpty())
			fTextEditor.resetHighlightRange();
		else {
			IStructuredSelection structSelect = (IStructuredSelection)selection; 
			Object obj = structSelect.getFirstElement();
			if (obj instanceof ASTNode) {
				ASTNode node = (ASTNode)obj;
				int beginLine = ASTNode.getLine(node.getStart());
				int endLine = ASTNode.getLine(node.getEnd());
				IDocument document = fDocumentProvider.getDocument(fInput);
				if (beginLine != 0 || endLine != 0) {
					try {
						int offset = document.getLineOffset(beginLine);
						int end = document.getLineOffset(document.getNumberOfLines() == endLine ? endLine-1 : endLine);
						int length = end - offset;
						//Position p = new Position(offset, length);
						//document.addPosition(JASTADD_JAVA_CODE, p);
						//positions.put(t, p);
						try {
						  fTextEditor.setHighlightRange(offset, length, true);
						} catch (IllegalArgumentException x) {
							fTextEditor.resetHighlightRange();
						}
					} catch (BadLocationException e) {
					}
				}
	
			}
		}
	}
	
	public void update() {
		TreeViewer viewer= getTreeViewer();

		if (viewer != null) {
			Control control= viewer.getControl();
			if (control != null && !control.isDisposed()) {
				control.setRedraw(false);
				viewer.setInput(fInput);
				viewer.expandAll();
				control.setRedraw(true);
			}
		}
	}
	
	protected class JastAddLabelProvider extends LabelProvider {
		public String getText(Object element) {
			
			if (element instanceof ASTNode) {
				return ((ASTNode)element).getClass().getName();
			} 
			return "ASTNode";
		}
		public Image getImage(Object element) {
			/*
			String imageKey = null;
			if(element instanceof ASTDecl) {
				imageKey = ISharedImages.IMG_OBJ_FOLDER;
			}
			else if(element instanceof ListComponents) {
				imageKey = org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT;
			}
			else if(element instanceof Components) {
				imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			}
			return imageKey == null ? null : PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
			*/
			String imageKey = org.eclipse.ui.ide.IDE.SharedImages.IMG_OPEN_MARKER;
			return imageKey == null ? null : PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}

	protected class JastAddContentProvider implements ITreeContentProvider {
		protected final static String JASTADD_JAVA_CODE = "__jastadd_java_code"; //$NON-NLS-1$
		protected IPositionUpdater fPositionUpdater =
			new DefaultPositionUpdater(JASTADD_JAVA_CODE);
		protected ASTNode content = null;
		public HashMap<ASTNode,Position> positions = new HashMap<ASTNode,Position>();

		protected void parse(IDocument document) {
			IFile file = JastAddDocumentProvider.documentToFile(document);

			Program program = new Program();
			program.initBytecodeReader(new bytecode.Parser());
			program.initJavaParser(new JavaParser() {
				public CompilationUnit parse(java.io.InputStream is,
						String fileName) throws java.io.IOException,
						beaver.Parser.Exception {
					return new parser.JavaParser().parse(is, fileName);
				}
			});
			program.initPackageExtractor(new scanner.JavaScanner());
			program.initOptions();

			// Add classpaths and filepath
			program.addKeyValueOption("-classpath");
			IProject project = file.getProject();
			IWorkspace workspace = project.getWorkspace();
			IWorkspaceRoot workspaceRoot = workspace.getRoot();
			String workspacePath = workspaceRoot.getRawLocation().toOSString();
			String fileFullPath = file.getFullPath().toOSString();
			String projectFullPath = project.getFullPath().toOSString();
			JastAddModel model = JastAddModel.getInstance();
			String[] classpathEntries = model.getClasspathEntries();
			String[] paths = new String[3];
			paths[0] = "-classpath";
			paths[1] = workspacePath;
			paths[1] += ":" + workspacePath + projectFullPath;
			for (int i = 0; i < classpathEntries.length; i++) {
				paths[1] += ":" + classpathEntries[i];
			}
			paths[2] = workspacePath + fileFullPath;
			program.addOptions(paths);

			Collection files = program.files();
			try {
				for (Iterator iter = files.iterator(); iter.hasNext();) {
					String name = (String) iter.next();
					try {
						program.addSourceFile(name);
					} catch (LexicalError e) {
						throw new LexicalError(name + ": " + e.getMessage());
					}
				}

				for (Iterator iter = program.compilationUnitIterator(); iter
						.hasNext();) {
					CompilationUnit unit = (CompilationUnit) iter.next();
					if (unit.fromSource()) {
						for (int i = 0; i < unit.getNumTypeDecl(); i++) {
							TypeDecl t = unit.getTypeDecl(i);
							if (t instanceof ClassDecl) {
								ClassDecl c = (ClassDecl)t;
								content = c;
								String className = c.name();
								System.out.println(className);
							}
							int beginLine = ASTNode.getLine(t.getStart());
							int endLine = ASTNode.getLine(t.getEnd());
						
							if (beginLine != 0 || endLine != 0) {
								try {
									int offset = document.getLineOffset(beginLine);
									int end = document.getLineOffset(document.getNumberOfLines() == endLine ? endLine-1 : endLine);
									int length = end - offset;
									Position p = new Position(offset, length);
									document.addPosition(JASTADD_JAVA_CODE, p);
									positions.put(t, p);
								} catch (BadPositionCategoryException e) {
								} catch (BadLocationException e) {
								}
							}
						}
					}
				}
			} catch (ParseError e) {
				System.err.println(e.getMessage());
			} catch (LexicalError e) {
				System.err.println(e.getMessage());
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}	
		
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (oldInput != null) {
				IDocument document = fDocumentProvider.getDocument(oldInput);
				if (document != null) {
					//try {
					//	document.removePositionCategory(JASTADD_JAVA_CODE);
					//} catch (BadPositionCategoryException x) {
					//}
					//document.removePositionUpdater(fPositionUpdater);
				}
			}
			content = null;
			//positions = new HashMap<ASTNode,Position>();

			if (newInput != null) {
				IDocument document = fDocumentProvider.getDocument(newInput);
				if (document != null) {
					//document.addPositionCategory(JASTADD_JAVA_CODE);
					//document.addPositionUpdater(fPositionUpdater);

					parse(document);
				}
			}
		}

		public void dispose() {
			if (content != null) {
				content = null;
			}
		}

		public boolean isDeleted(Object element) {
			return false;
		}

		public Object[] getElements(Object element) {
			if (content != null)
				return new Object[] { content };
			else
				return new Object[0];
		}

		public boolean hasChildren(Object element) {
			if(element == null)
				return false;
			if(element instanceof ASTNode) {
				ASTNode node = (ASTNode)element;
				for (int i=0; i < node.getNumChild(); i++) {
					 ASTNode child = node.getChild(i);
					 if (child.showInContentOutline()) {
						 return true;
					 } else if ((child instanceof List || 
							 child instanceof Opt) && 
							 hasChildren(child)) {
						 return true;
					 }
 				}
			}
			return false;
		}

		public Object getParent(Object element) {
			if(element == null)
				return null;
			if(element instanceof ASTNode) {
				ASTNode node = (ASTNode)element;
				ASTNode parent = node.getParent();
				if (parent.showInContentOutline())
					return parent;
				else getParent(parent);
			}
			return null;
		}

		public Object[] getChildren(Object element) {
			if(element == null)
				return new Object[0];
			if(element instanceof ASTNode) {
				ASTNode node = (ASTNode)element;
				List<ASTNode> list = new ArrayList<ASTNode>();
				for(int i = 0; i < node.getNumChild(); i++) {
					ASTNode child = node.getChild(i);
					if (child.showInContentOutline()) {
					  list.add(child);
					} else if (child instanceof List || child instanceof Opt) {
						Object[] subChildren = getChildren(child);
						for (int k = 0; k < subChildren.length; k++) {
							list.add((ASTNode)subChildren[i]);
						}
					}
				}
				return list.toArray();
			}
			return new Object[0];
		}
	};	
}
