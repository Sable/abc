package org.jastadd.plugin.providers;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IFileEditorInput;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.AST.IOutlineNode;
import org.jastadd.plugin.editor.JastAddStorageEditorInput;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;

public class JastAddContentProvider implements ITreeContentProvider {
	
	private ITreeContentProvider parent;
	
	public JastAddContentProvider(ITreeContentProvider parent) {
		this.parent = parent;
	}

	public JastAddContentProvider() {
		this.parent = new ITreeContentProvider() {
			public Object[] getChildren(Object parentElement) { return new Object[] { }; }
			public Object getParent(Object element) { return null; }
			public boolean hasChildren(Object element) { return false; }
			public Object[] getElements(Object inputElement) { return new Object[] { }; }
			public void dispose() {	}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
		};
	}

	public Object[] getChildren(Object element) {
		if(element instanceof IOutlineNode) {
			IOutlineNode node = (IOutlineNode)element;
			synchronized(((IJastAddNode)node).treeLockObject()) {
				return node.outlineChildren().toArray();
			}
		}
		else if(element instanceof IFile) {
			try {
				IFile file = (IFile)element;
				JastAddModel model = JastAddModelProvider.getModel(file);
				if (model != null) {
					IJastAddNode node = model.getTreeRoot(model.buildFileInfo(file));
					if (node != null && node instanceof IOutlineNode) {
						synchronized(node.treeLockObject()) { 
							return ((IOutlineNode)node).outlineChildren().toArray();
						}
					}
				}
			} catch (Exception e) {
			}
		}
		return parent.getChildren(element);
	}

	public Object getParent(Object element) {
		if(element instanceof IJastAddNode) {
			try {
				IJastAddNode node = (IJastAddNode)element;
				synchronized(node.treeLockObject()) { 
					IJastAddNode parent = node.getParent();
					if (parent != null && parent instanceof IOutlineNode && 
						((IOutlineNode)parent).showInContentOutline()) {
						return parent;
					} else { 
						return getParent(parent);
					}
				}
			} catch (Exception e) {
			}
		}
		return parent.getParent(element);
	}

	public boolean hasChildren(Object element) {
		try {
			if(element instanceof IOutlineNode) {
				synchronized (((IJastAddNode)element).treeLockObject()) {
					return !((IOutlineNode)element).outlineChildren().isEmpty();
				}
			}
			else if(element instanceof IFile) {
				IFile file = (IFile)element;
				JastAddModel model = JastAddModelProvider.getModel(file);
				if (model != null) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return parent.hasChildren(element);
	}

	public Object[] getElements(Object element) {
		try {
			IJastAddNode content = null;
			if(element instanceof IFileEditorInput) {
				IFileEditorInput input = (IFileEditorInput)element;
				IFile file = input.getFile();

				JastAddModel model = JastAddModelProvider.getModel(file);
				if (model != null) {
					IDocument document = model.fileInfoToDocument(model.buildFileInfo(file));
					content = model.getTreeRoot(document);
				}
			}
			else if (element instanceof JastAddStorageEditorInput) {
				JastAddStorageEditorInput storageInput = (JastAddStorageEditorInput)element;
				JastAddModel model = storageInput.getModel();
				content = model.getTreeRoot(model.buildFileInfo(storageInput));
			}
			if(content != null && content instanceof IOutlineNode) {
				synchronized (((IJastAddNode)content).treeLockObject()) {
					return ((IOutlineNode)content).outlineChildren().toArray();
				}
			}
		} catch (Exception e) {
		}
		return parent.getElements(element);
	}

	public void dispose() {
		parent.dispose();
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		parent.inputChanged(viewer, oldInput, newInput);
	}
}