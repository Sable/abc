package org.jastadd.plugin.jastaddj.editor.debug;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.jastaddj.AST.ITypeDecl;
import org.jastadd.plugin.ui.editor.BaseMarkerAnnotationModel;
import org.jastadd.plugin.util.FileInfo;
import org.jastadd.plugin.util.FileInfoMap;
import org.jastadd.plugin.util.NodeLocator;

public class JastAddJBreakpointAdapter implements
		IToggleBreakpointsTargetExtension {

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
			IPath storagePath = null;

			/*
			JastAddModel model = null;
			if (editorInput instanceof FileEditorInput) {
				IFile file = ((FileEditorInput) editorInput).getFile();
				model = JastAddModelProvider.getModel(file);
			} else if (editorInput instanceof JastAddStorageEditorInput) {
				JastAddStorageEditorInput storageInput = (JastAddStorageEditorInput) editorInput;
				model = storageInput.getModel();
				storagePath = storageInput.getStorage().getFullPath();
			}
			*/

			//if (model != null) {
				
				IJastAddNode node = NodeLocator.findNodeInDocument(FileInfoMap
						.buildFileInfo(editorInput), lineNumber + 1, 1);

				while (node != null && !(node instanceof ITypeDecl))
					node = node.getParent();
				if (!(node instanceof ITypeDecl))
					return;

				ITypeDecl typeDecl = (ITypeDecl) node;
				String typeName = typeDecl.constantPoolName().replace('/', '.');
				/*
				IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
				for (int i = 0; i < breakpoints.length; i++) {
					IBreakpoint breakpoint = breakpoints[i];
					if (!(breakpoint instanceof IJavaLineBreakpoint))
						continue;
					IJavaLineBreakpoint javaLineBreakpoint = (IJavaLineBreakpoint) breakpoint;

					if (javaLineBreakpoint.getTypeName().equals(typeName)
							&& javaLineBreakpoint.getLineNumber() == (lineNumber + 1)) {
						breakpoint.delete();
						return;
					}
				}
				*/

				// Remove if exists
				FileInfo info = FileInfoMap.buildFileInfo(editorInput);
				IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
				for (int i = 0; i < breakpoints.length; i++) {
					IBreakpoint breakpoint = breakpoints[i];
					if (!(breakpoint instanceof JastAddJBreakpoint))
						continue;
					JastAddJBreakpoint jastAddJBreakpoint = (JastAddJBreakpoint) breakpoint;
					if (jastAddJBreakpoint.sameAs(typeName, info, lineNumber + 1)) {
						breakpoint.delete();
						return;
					}
				}

				// Add 
				Map<String, Object> attributes = new HashMap<String, Object>();
				if (storagePath != null)
					attributes.put(BaseMarkerAnnotationModel.STORAGE_PATH, storagePath);
				IBreakpoint lineBreakpoint = new JastAddJBreakpoint(resource, typeName, info, lineNumber + 1, attributes); 
				DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(lineBreakpoint);
			}
		//}

	}

	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean canToggleLineBreakpoints(IWorkbenchPart part,
			ISelection selection) {
		return selection instanceof ITextSelection;
	}

	public boolean canToggleMethodBreakpoints(IWorkbenchPart part,
			ISelection selection) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canToggleWatchpoints(IWorkbenchPart part,
			ISelection selection) {
		// TODO Auto-generated method stub
		return false;
	}

	public void toggleMethodBreakpoints(IWorkbenchPart part,
			ISelection selection) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	public boolean canToggleBreakpoints(IWorkbenchPart part,
			ISelection selection) {
		// TODO Auto-generated method stub
		return false;
	}

	public void toggleBreakpoints(IWorkbenchPart part, ISelection selection)
			throws CoreException {
		// TODO Auto-generated method stub

	}
}
