package org.jastadd.plugin.ui.view;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.compiler.ast.IOutlineNode;

public class JastAddContentProvider implements ITreeContentProvider {
	
	private ITreeContentProvider parent;
	
	/*
	public JastAddContentProvider(ITreeContentProvider parent) {
		this.parent = parent;
	}
	*/

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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object element) {
		if(element instanceof IOutlineNode) {
			IOutlineNode node = (IOutlineNode)element;
			//synchronized(((IJastAddNode)node).treeLockObject()) {
				return node.outlineChildren().toArray();
			//}
		}
		/*
		else if(element instanceof IFile) {
				IFile file = (IFile)element;
				
				String path = file.getRawLocation().toOSString();
				IProject project = file.getProject();
				IASTNode ast = Activator.getASTRegistry().lookupAST(path, project);
				
				if (ast != null && ast instanceof IOutlineNode) {
					IOutlineNode node = (IOutlineNode)ast;
					return node.outlineChildren().toArray();
				}
				
		}
		*/
		return parent.getChildren(element);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if(element instanceof IJastAddNode) {
				IJastAddNode node = (IJastAddNode)element;
				//synchronized(node.treeLockObject()) { 
					IJastAddNode parent = node.getParent();
					if (parent != null && parent instanceof IOutlineNode && 
						((IOutlineNode)parent).showInContentOutline()) {
						return parent;
					} else { 
						return getParent(parent);
					}
				//}
		}
		return parent.getParent(element);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if(element instanceof IOutlineNode) {
			synchronized (((IASTNode)element).treeLockObject()) {
				return !((IOutlineNode)element).outlineChildren().isEmpty();
			}
		}
		/*
			else if(element instanceof IFile) {
				IFile file = (IFile)element;

				String path = file.getRawLocation().toOSString();
				IProject project = file.getProject();
				IASTNode ast = Activator.getASTRegistry().lookupAST(path, project);
				if (ast != null) {
					return true;
				}
			}
		 */
		return parent.hasChildren(element);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object element) {

		/*
		IJastAddNode content = null;
		if(element instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput)element;
			IFile file = input.getFile();

			String path = file.getRawLocation().toOSString();
			IProject project = file.getProject();
			IASTNode ast = Activator.getASTRegistry().lookupAST(path, project);

			if (ast != null && ast instanceof IJastAddNode) {
				IJastAddNode node = (IJastAddNode)ast;
				content = node;
			}
		}
		else if (element instanceof JastAddStorageEditorInput) {
			JastAddStorageEditorInput storageInput = (JastAddStorageEditorInput)element;

			// TODO this name is probably not the right key
			String path = storageInput.getName();
			IProject project = storageInput.getProject();
			IASTNode ast = Activator.getASTRegistry().lookupAST(path, project);

			if (ast != null && ast instanceof IJastAddNode) {
				IJastAddNode node = (IJastAddNode)ast;
				content = node;
			}
		}
		*/
		if(element != null && element instanceof IOutlineNode) {
			//synchronized (((IJastAddNode)content).treeLockObject()) {
				return ((IOutlineNode)element).outlineChildren().toArray();
			//}
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