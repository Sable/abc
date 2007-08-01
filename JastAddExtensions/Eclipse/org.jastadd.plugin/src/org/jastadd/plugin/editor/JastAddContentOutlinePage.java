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
import org.eclipse.jdt.ui.JavaElementLabels;
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
						int offset = document.getLineOffset(beginLine-1);
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
				return ((ASTNode)element).contentOutlineLabel();
			} else if (element instanceof String) {
				return (String)element;
			}
			return "Unknown";
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
		protected IPositionUpdater fPositionUpdater = new DefaultPositionUpdater(JASTADD_JAVA_CODE);
		protected Program content = null;
		public HashMap<ASTNode,Position> positions = new HashMap<ASTNode,Position>();

		protected void parse(IDocument document) {
			IFile file = JastAddDocumentProvider.documentToFile(document);
			JastAddModel model = JastAddModel.getInstance();
			content = model.buildFile(file);
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
			if (content != null) {
				List<Object> contentList = new ArrayList<Object>();

				for (Iterator iter = content.compilationUnitIterator(); iter
						.hasNext();) {
					CompilationUnit unit = (CompilationUnit) iter.next();
					if (unit.fromSource()) {
						String packageName = unit.getPackageDecl();
						if (packageName != null && packageName.length() > 0) {
						  contentList.add(unit.getPackageDecl());
						}
						for (int i = 0; i < unit.getNumTypeDecl(); i++) {
							TypeDecl t = unit.getTypeDecl(i);
							if (t instanceof ClassDecl) {
								contentList.add(t);
							}
							/*
							 * int beginLine = ASTNode.getLine(t.getStart());
							 * int endLine = ASTNode.getLine(t.getEnd());
							 * 
							 * if (beginLine != 0 || endLine != 0) { try { int
							 * offset = document .getLineOffset(beginLine); int
							 * end = document .getLineOffset(document
							 * .getNumberOfLines() == endLine ? endLine - 1 :
							 * endLine); int length = end - offset; Position p =
							 * new Position(offset, length);
							 * document.addPosition(JASTADD_JAVA_CODE, p);
							 * positions.put(t, p); } catch
							 * (BadPositionCategoryException e) { } catch
							 * (BadLocationException e) { } }
							 */
						}
					}
				}
				return contentList.toArray();
			} else {
				return new Object[0];
			}
		}

		public boolean hasChildren(Object element) {
			if(element == null)
				return false;
			if(element instanceof ASTNode) {
				ASTNode node = (ASTNode)element;
				return !node.outlineChildren().isEmpty();
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
				return node.outlineChildren().toArray();
			}
			return new Object[0];
		}
	};
}
