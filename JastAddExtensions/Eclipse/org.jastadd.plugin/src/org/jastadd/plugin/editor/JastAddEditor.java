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
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.jastadd.plugin.editor.debug.JastAddBreakpointAdapter;
import org.jastadd.plugin.editor.folding.JastAddEditorFolder;
import org.jastadd.plugin.editor.hover.JastAddSourceInformationControl;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.outline.JastAddContentOutlinePage;
import org.jastadd.plugin.resources.JastAddDocumentProvider;

/**
 * JastAdd editor providing various JastAdd related editor features
 */
public class JastAddEditor extends TextEditor {
	
	public static final String ID = "org.jastadd.plugin.editor.JastAddEditor";
	public static final String CONTEXT_ID = "org.jastadd.plugin.JastAddEditorScope";
	public static final String ERROR_MARKER_ID = "org.eclipse.ui.workbench.texteditor.error";
	public static final String WARNING_MARKER_ID = "org.eclipse.ui.workbench.texteditor.warning";
	
	private JastAddContentOutlinePage fOutlinePage;
	private JastAddBreakpointAdapter breakpointAdapter;
	private ProjectionSupport projectionSupport;
	private JastAddEditorFolder folder;
	private IContextActivation contextActivation;

	
	/** 
	 * Overriden method from TextEditor which adds a JastAdd specific SourceViewerConfiguration
	 * and DocumentProvider.
	 */
	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(new JastAddSourceViewerConfiguration());
		setDocumentProvider(new JastAddDocumentProvider());
	}
	
	/**
	 * Overriden method from TextEditor which adds a JastAdd specific ContentOutline and
	 * BreakpointAdapter.
	 */
	@Override
	public Object getAdapter(Class required) {
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
	
	/**
	 * Overriden method from AbstractDecoratedTextEditor. Adds projection support
	 * which provides folding in the editor. Activates the JastAdd editor context which activates
	 * commands and keybindings related to the context.
	 */
	@Override
	public void createPartControl(Composite parent) {
	    super.createPartControl(parent);
	    
	    ProjectionViewer viewer = (ProjectionViewer)getSourceViewer();
	    projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
	    projectionSupport.addSummarizableAnnotationType(ERROR_MARKER_ID); //$NON-NLS-1$
		projectionSupport.addSummarizableAnnotationType(WARNING_MARKER_ID); //$NON-NLS-1$
	    projectionSupport.setHoverControlCreator(new JastAddControlCreator());
	    projectionSupport.install();
	    getSourceViewerConfiguration();
	    //turn projection mode on
	    viewer.doOperation(ProjectionViewer.TOGGLE);
	    
	    folder = new JastAddEditorFolder(viewer.getProjectionAnnotationModel(), this);
	    JastAddModel.getInstance().addListener(folder);
	    
	    IContextService contextService = (IContextService) getSite().getService(IContextService.class);
	    contextActivation = contextService.activateContext(CONTEXT_ID);
	    
	    //IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
	    //IHandler handler = new FindDeclarationHandler();
	    //handlerService.activateHandler("org.jastadd.plugin.search.declaration", handler);
	    //handler = new FindReferencesHandler();
	    //handlerService.activateHandler("org.jastadd.plugin.search.references", handler);
	    //handler = new FindImplementsHandler();
	    //handlerService.activateHandler("org.jastadd.plugin.search.implements", handler);
	}
	
	/**
	 * Overriden method from TextEditor which removes listeners and contexts. 
	 */
	@Override 
	public void dispose() {
		super.dispose();
	    JastAddModel.getInstance().removeListener(folder);
	    IContextService contextService = (IContextService) getSite().getService(IContextService.class);
	    contextService.deactivateContext(contextActivation);
	}

	/**
	 * Overriden from AbstractDecoratedTextEditor. Adds a projection viewer which provides
	 * support for folding.
	 */
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess = getAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());
		ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);
		return viewer;
	}
	
	
	/**
	 * ControlCreator class used when creating the hover window for collapsed folding markers 
	 */
	private class JastAddControlCreator implements IInformationControlCreator {
	 	   public IInformationControl createInformationControl(Shell shell) {
	  	     return new JastAddSourceInformationControl(shell);
	  	   }
	}	
}
