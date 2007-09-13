package org.jastadd.plugin.editor;

import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.jastadd.plugin.JastAddDocumentProvider;
import org.jastadd.plugin.JastAddModel;
import org.jastadd.plugin.outline.JastAddContentOutlinePage;


public class JastAddEditor extends TextEditor {
	
	private JastAddContentOutlinePage fOutlinePage;
	private JastAddBreakpointAdapter breakpointAdapter;
	
	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(new JastAddSourceViewerConfiguration());
		setDocumentProvider(new JastAddDocumentProvider());
	}
	
	public Object getAdapter(Class required) {
		//System.out.println("JastAddEditor.getAdapter(Class): required.getName() = " + required.getName());
		if (IContentOutlinePage.class.equals(required)) {
			if (fOutlinePage == null) {
				fOutlinePage= new JastAddContentOutlinePage(this);
				if (getEditorInput() != null)
					fOutlinePage.setInput(getEditorInput());
			}
			return fOutlinePage;
		} else if (IToggleBreakpointsTarget.class.equals(required)) {
			if (breakpointAdapter == null) {
				breakpointAdapter = new JastAddBreakpointAdapter(this);
			}
			return breakpointAdapter;
		}
		return super.getAdapter(required);
	}
	
	
	private ProjectionSupport projectionSupport;
	private JastAddEditorFolder folder;
	
	public void createPartControl(Composite parent) {
	    super.createPartControl(parent);
	    
	    ProjectionViewer viewer = (ProjectionViewer)getSourceViewer();

	    projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
	    projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
	    projectionSupport.setHoverControlCreator(new JastAddControlCreator());
	    projectionSupport.install();
	    getSourceViewerConfiguration();

	    //turn projection mode on
	    viewer.doOperation(ProjectionViewer.TOGGLE);
	    
	    folder = new JastAddEditorFolder(viewer.getProjectionAnnotationModel(), this);
	    JastAddModel.getInstance().addListener(folder);
	}
	
	@Override public void dispose() {
		super.dispose();
	    JastAddModel.getInstance().removeListener(folder);
	}

	private class JastAddControlCreator implements IInformationControlCreator {
 	   public IInformationControl createInformationControl(Shell shell) {
  	     return new JastAddSourceInformationControl(shell);
  	   }
	}
	

	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		
		fAnnotationAccess = getAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());
		
		ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);

		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}
}
