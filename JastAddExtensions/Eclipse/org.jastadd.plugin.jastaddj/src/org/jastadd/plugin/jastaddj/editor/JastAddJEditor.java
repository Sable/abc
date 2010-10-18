package org.jastadd.plugin.jastaddj.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.jastadd.plugin.Activator;
import org.jastadd.plugin.ReconcilingStrategy;
import org.jastadd.plugin.compiler.ICompiler;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IFoldingNode;
import org.jastadd.plugin.jastaddj.editor.actions.AddParameterRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.ChangeParameterTypeRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.EncapsulateFieldRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.ExtractClassRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.ExtractInterfaceRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.ExtractMethodRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.ExtractTempRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.FindDeclarationHandler;
import org.jastadd.plugin.jastaddj.editor.actions.FindImplementsHandler;
import org.jastadd.plugin.jastaddj.editor.actions.FindReferencesHandler;
import org.jastadd.plugin.jastaddj.editor.actions.InlineMethodRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.InlineTempRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.PullUpMethodRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.PushDownMethodRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.QuickContentOutlineHandler;
import org.jastadd.plugin.jastaddj.editor.actions.QuickTypeHierarchyHandler;
import org.jastadd.plugin.jastaddj.editor.actions.ReferenceHierarchyHandler;
import org.jastadd.plugin.jastaddj.editor.actions.RenameRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.TypeHierarchyHandler;
import org.jastadd.plugin.jastaddj.editor.debug.JastAddJBreakpointAdapter;
import org.jastadd.plugin.jastaddj.refactor.Refactorings;
import org.jastadd.plugin.jastaddj.refactor.Refactorings.RefactoringInfo;
import org.jastadd.plugin.registry.ASTRegistry;
import org.jastadd.plugin.registry.IASTRegistryListener;
import org.jastadd.plugin.ui.popup.AbstractBaseInformationPresenter;
import org.jastadd.plugin.ui.view.AbstractBaseContentOutlinePage;
import org.jastadd.plugin.util.FileInfoMap;
import org.jastadd.plugin.util.JastAddDocumentProvider;
import org.jastadd.plugin.util.JastAddStorageEditorInput;

import sun.util.ResourceBundleEnumeration;

public class JastAddJEditor extends AbstractDecoratedTextEditor implements IASTRegistryListener {

	public static final String EDITOR_ID = "org.jastadd.plugin.jastaddj.JastAddJEditor";
	public static final String EDITOR_CONTEXT_ID = "org.jastadd.plugin.jastaddj.JastAddJEditorContext";

	private static final String BUNDLE_FOR_CONSTRUCTED_KEYS= "org.eclipse.jdt.internal.ui.javaeditor.ConstructedJavaEditorMessages";//$NON-NLS-1$
	
	// Content outline
	protected AbstractBaseContentOutlinePage fOutlinePage;

	// Reconciling strategy
	protected ReconcilingStrategy fStrategy;
	
	// ASTRegistry listener fields
	protected IASTNode fRoot;
	protected String fKey;
	protected IProject fProject;

	protected IContextActivation contextActivation;
	protected JastAddJBreakpointAdapter breakpointAdapter;	
		
	/**
	 * Creates a new JastAddJ editor
	 */
	public JastAddJEditor() {
		fOutlinePage = new JastAddJContentOutlinePage(this); 
		fStrategy = new ReconcilingStrategy();
	}


	public String getEditorContextID() {
		return JastAddJEditor.EDITOR_CONTEXT_ID;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.editors.text.TextEditor#initializeEditor()
	 */
	@Override
	protected void initializeEditor() {
		super.initializeEditor();		
		setDocumentProvider(new JastAddDocumentProvider());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.editors.text.TextEditor#doSetInput(org.eclipse.ui.IEditorInput)
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

		/* Save this for later
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fileInput = (IFileEditorInput)input;
			IFile file = fileInput.getFile();
			model = JastAddModelProvider.getModel(file);
			setSourceViewerConfiguration(new JastAddSourceViewerConfiguration(model, file));
		}
		else if (input instanceof JastAddStorageEditorInput) {
			JastAddStorageEditorInput storageInput = (JastAddStorageEditorInput)input;
			model = storageInput.getModel();
			setSourceViewerConfiguration(new JastAddSourceViewerConfiguration(model));
		}		
		super.doSetInput(input);
		*/
	}
	

	// ASTRegistry listener methods
	
	@Override
	public void childASTChanged(IProject project, String key) {
		System.out.println("JastAddJEditor.childASTChanged, project=" + project.getName() + ", key=" + key);
		ASTRegistry reg = Activator.getASTRegistry();
		fRoot = reg.lookupAST(fKey, fProject);
		update();
	}

	@Override
	public void projectASTChanged(IProject project) {
		System.out.println("JastAddJEditor.projectASTChanged, project=" + project.getName());
		ASTRegistry reg = Activator.getASTRegistry();
		fRoot = reg.lookupAST(fKey, fProject);
		update();
	}
	
	/**
	 * Update to the AST corresponding to the file input
	 * @param fInput The new file input
	 */
	private void resetAST(IFileEditorInput fInput) {
		// Reset
		ASTRegistry reg = Activator.getASTRegistry();
		reg.removeListener(this);
		fRoot = null;
		fProject = null;
		fKey = null;
		
		// Update
		if (fInput != null) {
			IFile file = fInput.getFile();
			fKey = file.getRawLocation().toOSString();
			fProject = file.getProject();
			reg.addListener(this, fProject, fKey);
			fRoot = reg.lookupAST(fKey, fProject);
			update();
		}
	}
	
	 
	/**
	 * Updates the outline and the view
	 */
	private void update() {
		// Update outline
		fOutlinePage.updateAST(fRoot);
		// Update folding
		updateProjectionAnnotations();
	}
	
	
	
	/**
	 * Update projection annotations
	 */
	private void updateProjectionAnnotations() {
		ProjectionViewer viewer = (ProjectionViewer)getSourceViewer();
		if (viewer == null) {
			return;
		}
		
		// Enable projection
		viewer.enableProjection();

		// Collect old annotations
		ProjectionAnnotationModel annotationModel = viewer.getProjectionAnnotationModel();
		//if (model == null) {
		//	return;
		//}
		Collection<Annotation> oldAnnotations = new ArrayList<Annotation>();
		for (Iterator<Annotation> itr = annotationModel.getAnnotationIterator(); itr.hasNext();) {
			oldAnnotations.add(itr.next());
		}

		// Collect new annotations
		HashMap<Annotation,Object> newAnnotations = new HashMap<Annotation,Object>();
		
		// TODO fick the foldingPositions call, don't have a reference to an IDocument
		
		if (fRoot != null && fRoot instanceof IFoldingNode) {
			
			IFoldingNode node = (IFoldingNode)fRoot;
			IDocument document = getSourceViewer().getDocument();
			Collection<Position> positions = node.foldingPositions(document);
			for (Position pos : positions) {
				ProjectionAnnotation annotation = new ProjectionAnnotation();
				annotation.markExpanded();
				newAnnotations.put(annotation, pos);
			}
		}

		// Update annotations
		annotationModel.modifyAnnotations(oldAnnotations.toArray(new Annotation[] {}), newAnnotations, null);
	}

	
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor progressMonitor) {
		super.doSave(progressMonitor);
		
		// TODO Do this in a new way
		// Force an error check on the project		

		/*
		IEditorInput input = getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput)input).getFile();
			final IProject project = file.getProject();
			
			
			final JastAddModel m = model;
			IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					m.checkForErrors(project, monitor);
				}
			};
			
			try {
				project.getWorkspace().run(runnable, progressMonitor);
			} catch (CoreException e) {
				String message = "Problem with error check on save"; 
				IStatus status = new Status(IStatus.ERROR, 
						Activator.JASTADDJ_PLUGIN_ID,
						IStatus.ERROR, message, e);
				Activator.INSTANCE.getLog().log(status);
			}
		}
		*/
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.editors.text.TextEditor#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			return fOutlinePage;
		} else if (IToggleBreakpointsTarget.class.equals(required)) {
			if (breakpointAdapter == null) {
				breakpointAdapter = new JastAddJBreakpointAdapter(this);
			}
			return breakpointAdapter;
		}
		return super.getAdapter(required);
	}
	
	/**
	 * Creates a new source viewer configuration
	 * @return The new source viewer configuration
	 */
	protected SourceViewerConfiguration createSourceViewerConfiguration() {
		return new JastAddJSourceViewerConfiguration(fStrategy);
	}

	
	/**
	 * Overriden method from AbstractDecoratedTextEditor. Adds projection support
	 * which provides folding in the editor. Activates the JastAdd editor context which activates
	 * commands and keybindings related to the context.
	 */
	@Override
	public void createPartControl(Composite parent) {
		setEditorContextMenuId(getEditorSite().getId());
		
		 // Set the source viewer configuration before the call to createPartControl to set viewer configuration	
	    super.setSourceViewerConfiguration(createSourceViewerConfiguration());
	    super.createPartControl(parent);

	    /*
		super.createPartControl(parent);
	    
	    ProjectionViewer viewer = (ProjectionViewer)getSourceViewer();
	    projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
	    if (model != null) {
	    	projectionSupport.addSummarizableAnnotationType(model.getEditorConfiguration().getErrorMarkerID()); //$NON-NLS-1$
	    	projectionSupport.addSummarizableAnnotationType(model.getEditorConfiguration().getWarningMarkerID()); //$NON-NLS-1$
	    }
	    projectionSupport.setHoverControlCreator(new JastAddControlCreator());
	    projectionSupport.install();
	    getSourceViewerConfiguration();
	    viewer.doOperation(ProjectionViewer.TOGGLE);
	    
	    folder = new JastAddEditorFolder(viewer.getProjectionAnnotationModel(), this);
	    if (model != null)
	    	model.addListener(folder);
	    
	    */
	    	    
	    //if (model != null) {
	    	IContextService contextService = (IContextService) getSite().getService(IContextService.class);
	    	contextActivation = contextService.activateContext(getEditorContextID());
	    //}
	    
	    update();
	}
	
	/**
	 * Overriden method from TextEditor which removes listeners and contexts. 
	 */
	@Override 
	public void dispose() {
		super.dispose();
		
		IEditorInput input = getEditorInput();
		//if (model != null) {
			if(input instanceof IFileEditorInput) {
				IFileEditorInput fileInput = (IFileEditorInput)input;
				IFile file = fileInput.getFile();
				//final JastAddModel model = JastAddModelProvider.getModel(file);
				//if (this.model == model) {
					FileInfoMap.releaseFileInfo(FileInfoMap.buildFileInfo(input));
				//}
			} else if(input instanceof JastAddStorageEditorInput) {
				JastAddStorageEditorInput storageInput = (JastAddStorageEditorInput)input;
				FileInfoMap.releaseFileInfo(FileInfoMap.buildFileInfo(input));
			}
			//model.removeListener(folder);
		//}
	    IContextService contextService = (IContextService) getSite().getService(IContextService.class);
	    contextService.deactivateContext(contextActivation);
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.text.source.IVerticalRuler, int)
	 */
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		
		// Code from the super class implementation of this method
		fAnnotationAccess= getAnnotationAccess();
		fOverviewRuler= createOverviewRuler(getSharedColors());
		
		// Projection support
		ProjectionViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
	    ProjectionSupport projectionSupport = new ProjectionSupport(viewer,getAnnotationAccess(),getSharedColors());
	    projectionSupport.addSummarizableAnnotationType(ICompiler.ERROR_MARKER_ID);
	    projectionSupport.addSummarizableAnnotationType(ICompiler.WARNING_MARKER_ID);
	    projectionSupport.install();
	    // Ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);
		
		return viewer;

		/*
		fAnnotationAccess = getAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());
		ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);
		return viewer;
		*/
	}

		
	/**
	 * ControlCreator class used when creating the hover window for collapsed folding markers 
	 */
	/*
	private class JastAddControlCreator implements IInformationControlCreator {
	 	   public IInformationControl createInformationControl(Shell shell) {
	  	     return new JastAddSourceInformationControl(shell, model);
	  	   }
	}
	*/

	

	/**
	 * ACTIONS
	 */
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.editors.text.TextEditor#createActions()
	 */
	@Override
	protected void createActions() {	
		// Old jastadd part
		super.createActions();
		// This action will fire a CONTENTASSIST_PROPOSALS operation
		// when executed
		IAction action = new TextOperationAction(new JastAddResourceBundle(),
					"ContentAssistProposal", this, SourceViewer.CONTENTASSIST_PROPOSALS);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action);
		setActionActivationCode("ContentAssistProposal",' ', -1, SWT.CTRL);

		// Old jastaddJ part
		action= new ContentAssistAction(ResourceBundle.getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS), "ContentAssistProposal.", this); //$NON-NLS-1$
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action); //$NON-NLS-1$
		markAsStateDependentAction("ContentAssistProposal", true); //$NON-NLS-1$
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(action, IJavaHelpContextIds.CONTENT_ASSIST_ACTION);
	}	

	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.editors.text.TextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		populateContextMenu(menu);
	}
	
	public void populateContextMenu(IMenuManager menuManager) {
		menuManager.insertAfter("group.open", buildContextMenuItem("Quick Out&line",
				"org.jastadd.plugin.jastaddj.query.QuickContentOutline",
				new QuickContentOutlineHandler()));
		
		menuManager.insertAfter("group.open", buildContextMenuItem("Quick Type H&ierarchy",
				"org.jastadd.plugin.jastaddj.query.QuickTypeHierarchy",
				new QuickTypeHierarchyHandler()));
		
		menuManager.insertAfter("group.open", buildContextMenuItem("Open Reference &Hierarchy",
				"org.jastadd.plugin.jastaddj.query.ReferenceHierarchy",
				new ReferenceHierarchyHandler()));
		
		menuManager.insertAfter("group.open", buildContextMenuItem("Open Type &Hierarchy",
				"org.jastadd.plugin.jastaddj.query.TypeHierarchy",
				new TypeHierarchyHandler()));

		IMenuManager refactorMenu = findOrAddRefactorContextMenu(menuManager);
		populateRefactorContextMenuItems(refactorMenu);

		IMenuManager findMenu = findOrAddFindContextMenu(menuManager);
		populateFindContextMenuItems(findMenu);
	}
	
	
	protected void populateFindContextMenuItems(IMenuManager findMenu) {

		addContextMenuItem(findMenu, "Find Declaration",
				"org.jastadd.plugin.jastaddj.find.FindDeclaration",
				new FindDeclarationHandler());

		addContextMenuItem(findMenu, "Find References",
				"org.jastadd.plugin.jastaddj.find.FindReferences",
				new FindReferencesHandler());

		addContextMenuItem(findMenu, "Find &Implements",
				"org.jastadd.plugin.jastaddj.find.FindImplements",
				new FindImplementsHandler());
		
	}

	protected void populateRefactorContextMenuItems(IMenuManager refactorMenu) {
		for(RefactoringInfo<?> info : Refactorings.refactorings)
			addContextMenuItem(refactorMenu, info.getMenuText(), 
							   info.getId(), info.newHandler());
	}
		
	protected IMenuManager findOrAddMenu(IMenuManager menuManager, String idSuffix, String text) {
		String id = getEditorContextID() + idSuffix;
		IMenuManager newMenuManager = menuManager.findMenuUsingPath(id);
		if (newMenuManager == null)
			newMenuManager = new MenuManager(text, id);
		newMenuManager.add(new Separator("additions"));
		menuManager.insertAfter("additions", newMenuManager);
		return newMenuManager;
	}
	
	protected IMenuManager findOrAddFindContextMenu(IMenuManager menuManager) {
		return findOrAddMenu(menuManager, ".find.popup", "F&ind");
	}
	
	protected IMenuManager findOrAddRefactorContextMenu(IMenuManager menuManager) {
		return findOrAddMenu(menuManager, ".refactor.popup", "Refac&tor");
	}
	
	protected void addContextMenuItem(IMenuManager menuManager, String text, String definitionId, IActionDelegate actionDelegate) {
		menuManager.add(buildContextMenuItem(text, definitionId, actionDelegate));
	}
	
	protected IAction buildContextMenuItem(String text, String definitionId, final IActionDelegate actionDelegate) {
		IAction action = new Action() {
			public void run() {
				actionDelegate.run(this);
			}
		};
		action.setText(text);
		action.setActionDefinitionId(definitionId);
		return action;
	}
	
	
	
	/**
	 * Resource bundle class to use with content assist
	 */
	protected class JastAddResourceBundle extends ResourceBundle {
		
		private HashMap<String,String> map = new HashMap<String,String>();

		/*
		 * (non-Javadoc)
		 * @see java.util.ResourceBundle#getKeys()
		 */
		@Override
		public Enumeration<String> getKeys() {
			ResourceBundle parent = this.parent;
	        return new ResourceBundleEnumeration(map.keySet(),
	                (parent != null) ? parent.getKeys() : null);
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
		 */
		@Override
		protected Object handleGetObject(String key) {
			return map.get(key);
		}
	}


	/**
	 * Display an information presenter in the editor
	 * @param presenter The presenter to show
	 */
	public void showInformationPresenter(AbstractBaseInformationPresenter presenter) {
		presenter.install(getSourceViewer());
		presenter.showInformation();
	}


	/*
	@Override
	public void selectionChanged(SelectionChangedEvent event) {

		ISelection selection = event.getSelection();
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection = (ITextSelection)selection;
			int line = textSelection.getStartLine();
		}
	}
	*/
	
}