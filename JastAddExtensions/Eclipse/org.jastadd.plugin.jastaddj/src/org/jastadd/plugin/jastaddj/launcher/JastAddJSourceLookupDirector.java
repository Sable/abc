package org.jastadd.plugin.jastaddj.launcher;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ZipEntryStorage;
import org.eclipse.debug.ui.ISourcePresentation;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.internal.debug.ui.LocalFileStorageEditorInput;
import org.eclipse.jdt.internal.debug.ui.ZipEntryStorageEditorInput;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.model.JastAddJModel;

public class JastAddJSourceLookupDirector extends AbstractSourceLookupDirector implements ISourcePresentation {
	
	private static Set<String> fFilteredTypes;
	
	static {
		fFilteredTypes = new HashSet<String>();
		fFilteredTypes.add(ProjectSourceContainer.TYPE_ID);
		fFilteredTypes.add(WorkspaceSourceContainer.TYPE_ID);
		// can't reference UI constant
		fFilteredTypes.add("org.eclipse.debug.ui.containerType.workingSet"); //$NON-NLS-1$
	}

	
	private IProject project;
	private JastAddJModel model;
	private JastAddJBuildConfiguration buildConfiguration;
	
	public JastAddJSourceLookupDirector(IProject project, JastAddJModel model, JastAddJBuildConfiguration buildConfiguration) {
		super();
		this.project = project;
		this.model = model;
		this.buildConfiguration = buildConfiguration;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector#initializeParticipants()
	 */
	public void initializeParticipants() {
		addParticipants(new ISourceLookupParticipant[] {new JastAddJSourceLookupParticipant(project, model, buildConfiguration)});
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector#supportsSourceContainerType(org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType)
	 */
	public boolean supportsSourceContainerType(ISourceContainerType type) {
		return !fFilteredTypes.contains(type.getId());
	}
	
    public IEditorInput getEditorInput(Object item) {
		if (item instanceof IMarker)
			item = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint((IMarker)item);
		
		if (item instanceof IJavaBreakpoint)
			item = ((IJavaBreakpoint)item).getMarker().getResource();

		if (item instanceof IFile)
			return new FileEditorInput((IFile)item);
		
		if (item instanceof LocalFileStorage)
			return new LocalFileStorageEditorInput((LocalFileStorage)item);
		
		if (item instanceof ZipEntryStorage)
			return new ZipEntryStorageEditorInput((ZipEntryStorage)item);
		
		return null;
	}

     public String getEditorId(IEditorInput input, Object element) {
		IEditorRegistry editorReg = PlatformUI.getWorkbench().getEditorRegistry();
		IEditorDescriptor[] descriptors = editorReg.getEditors(input.getName());
		for(IEditorDescriptor descriptor : descriptors) 
			if (descriptor.getId().equals(model.getEditorID()))
				return descriptor.getId();
		return descriptors.length > 0 ? descriptors[0].getId() : null;
     }
}
