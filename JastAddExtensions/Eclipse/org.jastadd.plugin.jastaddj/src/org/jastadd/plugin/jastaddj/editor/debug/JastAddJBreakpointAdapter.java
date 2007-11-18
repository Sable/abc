package org.jastadd.plugin.jastaddj.editor.debug;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.editor.JastAddStorageEditorInput;
import org.jastadd.plugin.jastaddj.AST.ITypeDecl;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;


public class JastAddJBreakpointAdapter implements IToggleBreakpointsTargetExtension {
	
	private ITextEditor editor;
	
	public JastAddJBreakpointAdapter(ITextEditor editor) {
		this.editor = editor;
	}
	
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection)
			throws CoreException {
		
		if (editor != null) {
			IEditorInput editorInput = editor.getEditorInput();
			IResource resource = (IResource) editorInput
					.getAdapter(IResource.class);
			if (resource == null)
				resource = ResourcesPlugin.getWorkspace().getRoot();
			ITextSelection textSelection = (ITextSelection) selection;
			int lineNumber = textSelection.getStartLine();
			IBreakpoint[] breakpoints = DebugPlugin.getDefault()
					.getBreakpointManager().getBreakpoints();
			for (int i = 0; i < breakpoints.length; i++) {
				IBreakpoint breakpoint = breakpoints[i];
				if (resource.equals(breakpoint.getMarker().getResource())) {
					if (((ILineBreakpoint) breakpoint).getLineNumber() == (lineNumber + 1)) {
						breakpoint.delete();
						return;
					}
				}
			}
			JastAddModel model = null;
			if(editorInput instanceof FileEditorInput) {
				IFile file = ((FileEditorInput)editorInput).getFile();
				model = JastAddModelProvider.getModel(file);
			}
			else if (editorInput instanceof JastAddStorageEditorInput) {
				model = ((JastAddStorageEditorInput)editorInput).getModel(); 
			}

			if (model != null) {
				IJastAddNode node = model.findNodeInDocument(model.buildFileInfo(editorInput), lineNumber + 1, 1);
				while(node != null && !(node instanceof ITypeDecl))
					node = node.getParent();
				if(node instanceof ITypeDecl) {
					ITypeDecl typeDecl = (ITypeDecl)node;
					String name = typeDecl.constantPoolName().replace('/', '.');
					IBreakpoint lineBreakpoint = new JavaLineBreakpoint(resource, name, lineNumber + 1, -1, -1, 0, true, new HashMap());
					DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(lineBreakpoint);
				}
			}
		}
		
	}

	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
		return selection instanceof ITextSelection;
	}

	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
		// TODO Auto-generated method stub
		return false;
	}

	public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection) {
		// TODO Auto-generated method stub
		return false;
	}

	public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		// TODO Auto-generated method stub
		
	}
}
