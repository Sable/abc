package org.jastadd.plugin.jastadd.editor.grammar;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.jastadd.plugin.Activator;
import org.jastadd.plugin.ReconcilingStrategy;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.registry.ASTRegistry;
import org.jastadd.plugin.registry.IASTRegistryListener;

public class ASTEditor extends AbstractDecoratedTextEditor implements IASTRegistryListener {
	
	public static final String EDITOR_ID = "org.jastadd.plugin.jastadd.ASTEditor";
	public static final String EDITOR_CONTEXT_ID = "org.jastadd.plugin.jastadd.ASTEditorContext";
	
	// Content outline
	protected ASTContentOutlinePage fOutlinePage;

	// Reconciling strategy
	protected ReconcilingStrategy fStrategy;
	
	// ASTRegistry listener fields
	protected ArrayList<IASTNode> fRootList;
	protected String fKey;
	protected IProject fProject;

	public ASTEditor() {
		fOutlinePage = new ASTContentOutlinePage(this); 
		fStrategy = new ReconcilingStrategy();
		fRootList = new ArrayList<IASTNode>();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#doSetInput(org.eclipse.ui.IEditorInput)
	 */
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		IFileEditorInput fInput = null;
		if (input instanceof IFileEditorInput) {
			fInput = (IFileEditorInput)input; 
		}
		// Update AST
		resetAST(fInput);
		// Update file in reconciling strategy
		if (fInput != null)
			fStrategy.setFile(fInput.getFile());
	}
	
	// ASTRegistry listener methods
	
	@Override
	public void childASTChanged(IProject project, String key) {
		System.out.println("ASTEditor.childASTChanged, project=" + project.getName() + ", key=" + key);
		lookupASTList();
		update();
	}

	@Override
	public void projectASTChanged(IProject project) {
		System.out.println("ASTEditor.projectASTChanged, project=" + project.getName());
		lookupASTList();
		update();
	}
	
	private void lookupASTList() {
		int postIndex = 0;
		boolean failedLookup = false;
		ASTRegistry reg = Activator.getASTRegistry();
		fRootList.clear();
		while (!failedLookup) {
			IASTNode root = reg.lookupAST(fKey + "#" + postIndex++, fProject);
			if (root == null) {
				failedLookup = true;
			} else {
				fRootList.add(root);
			}
		}
	}
	
	/**
	 * Update to the AST corresponding to the file input
	 * @param fInput The new file input
	 */
	private void resetAST(IFileEditorInput fInput) {
		// Reset
		ASTRegistry reg = Activator.getASTRegistry();
		reg.removeListener(this);
		fRootList.clear();
		fProject = null;
		fKey = null;
		
		// Update
		if (fInput != null) {
			IFile file = fInput.getFile();
			fKey = file.getRawLocation().toOSString();
			fProject = file.getProject();
			reg.addListener(this, fProject, fKey);
			lookupASTList();
			update();
		}
	}
	
	 
	/**
	 * Updates the outline and the view
	 */
	private void update() {
		// Update outline
		fOutlinePage.updateASTList(fRootList);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			return fOutlinePage;
		} 
		return super.getAdapter(required);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		setEditorContextMenuId(getEditorSite().getId());		
		 // Set the source viewer configuration before the call to createPartControl to set viewer configuration	
	    super.setSourceViewerConfiguration(new ASTSourceViewerConfiguration(fStrategy));
	    super.createPartControl(parent);
	}
}