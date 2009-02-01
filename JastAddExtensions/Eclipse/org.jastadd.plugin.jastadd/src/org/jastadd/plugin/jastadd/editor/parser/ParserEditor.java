package org.jastadd.plugin.jastadd.editor.parser;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

public class ParserEditor extends AbstractDecoratedTextEditor { //implements IASTRegistryListener {
	
	public static final String EDITOR_ID = "org.jastadd.plugin.jastadd.ParserEditor";
	public static final String EDITOR_CONTEXT_ID = "org.jastadd.plugin.jastadd.ParserEditorContext";
	
	/*
	// Reconciling strategy
	private ReconcilingStrategy fStrategy;
	
	// ASTRegistry listener fields
	private ASTRegistry fRegistry;
	private IASTNode fRoot;
	private String fKey;
	private IProject fProject;
	
	public ParserEditor() {
		fRegistry = org.jastadd.plugin.Activator.getASTRegistry();
		fOutlinePage = new JastAddContentOutlinePage(this, model); 
		fStrategy = new ReconcilingStrategy();//new ReconcilingStrategy(model);
	}
	
	
	public String getEditorContextID() {
		return EDITOR_CONTEXT_ID;
	}

	protected class JastAddResourceBundle extends ResourceBundle {

		private HashMap<String,String> map = new HashMap<String,String>();

		@Override
		public Enumeration<String> getKeys() {
			ResourceBundle parent = this.parent;
			return new ResourceBundleEnumeration(map.keySet(),
					(parent != null) ? parent.getKeys() : null);
		}

		@Override
		protected Object handleGetObject(String key) {
			return map.get(key);
		}

	}

	@Override
	protected void createActions() {
		super.createActions();
		// This action will fire a CONTENTASSIST_PROPOSALS operation
		// when executed
		IAction action = new TextOperationAction(new JastAddResourceBundle(),
				"ContentAssistProposal", this, SourceViewer.CONTENTASSIST_PROPOSALS);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action);
		setActionActivationCode("ContentAssistProposal",' ', -1, SWT.CTRL);
	}	

	@Override
	public void doSave(IProgressMonitor progressMonitor) {
		super.doSave(progressMonitor);
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
				//model.logError(e, "Problem with error check on save");
			}
		}
	}

	private JastAddContentOutlinePage fOutlinePage;
	private ProjectionSupport projectionSupport;
	private JastAddEditorFolder folder;
	private IContextActivation contextActivation;

	private JastAddModel model;

	public JastAddModel getModel() {
		return this.model;
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);

		IFileEditorInput fInput = null;
		if (input instanceof IFileEditorInput) {
			fInput = (IFileEditorInput)input;
			
			// TODO Remove when model is replaced
			// No need to create and set a new source viewer at every doSetInput
			//IFile file = fInput.getFile();
			//model = JastAddModelProvider.getModel(file);
			//setSourceViewerConfiguration(new JastAddSourceViewerConfiguration(model, file));
			 
		}
		
		// TODO Remove?
		else if (input instanceof JastAddStorageEditorInput) {
			// No need to create and set a new source viewer at every doSetInput
			//JastAddStorageEditorInput storageInput = (JastAddStorageEditorInput)input;
			//model = storageInput.getModel();
			//setSourceViewerConfiguration(new JastAddSourceViewerConfiguration(model));
			
		}
		
		// Update AST
		resetAST(fInput);
		// Update file in reconciling strategy
		fStrategy.setFile(fInput.getFile());

		
		//if (input instanceof IFileEditorInput) {
		//	IFileEditorInput fileInput = (IFileEditorInput)input;
		//	IFile file = fileInput.getFile();
		//	model = JastAddModelProvider.getModel(file);
		//	setSourceViewerConfiguration(new JastAddSourceViewerConfiguration(model, file));
		//}
		//else if (input instanceof JastAddStorageEditorInput) {
		//	JastAddStorageEditorInput storageInput = (JastAddStorageEditorInput)input;
		//	model = storageInput.getModel();
		//	setSourceViewerConfiguration(new JastAddSourceViewerConfiguration(model));
		//}		
		//super.doSetInput(input);
		
	}

	
	protected void doSetInput(IEditorInput input) throws CoreException {
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
	}

	
	// ASTRegistry listener methods
	
	@Override
	public void childASTChanged(IProject project, String key) {
		fRoot = fRegistry.lookupAST(fKey, fProject);
		update();
	}

	@Override
	public void projectASTChanged(IProject project) {
		fRoot = fRegistry.lookupAST(fKey, fProject);
		update();
	}
	
	//
	 // Update to the AST corresponding to the file input
	 //@param fInput The new file input
	 //
	private void resetAST(IFileEditorInput fInput) {
		// Reset 
		fRegistry.removeListener(this);
		fRoot = null;
		fProject = null;
		fKey = null;
		
		// Update
		if (fInput != null) {
			IFile file = fInput.getFile();
			fKey = file.getRawLocation().toOSString();
			fProject = file.getProject();			
			fRoot = fRegistry.lookupAST(fKey, fProject);
			if (fRoot != null) {
				fRegistry.addListener(this, fProject, fKey);
			}
			update();
		}
	}
	
	
	///
	 // Updates the outline and the view
	 //
	private void update() {
		// Update outline
		fOutlinePage.updateAST(fRoot);
		// Update folding
		updateProjectionAnnotations();
	}
	

	
	//
	 // Update projection annotations
	 //
	private void updateProjectionAnnotations() {
		ProjectionViewer viewer = (ProjectionViewer)getSourceViewer();
		if (viewer == null) {
			return;
		}
		
		// Enable projection
		viewer.enableProjection();

		// Collect old annotations
		ProjectionAnnotationModel annotationModel = viewer.getProjectionAnnotationModel();
		if (model == null) {
			return;
		}
		Collection<Annotation> oldAnnotations = new ArrayList<Annotation>();
		for (Iterator<Annotation> itr = annotationModel.getAnnotationIterator(); itr.hasNext();) {
			oldAnnotations.add(itr.next());
		}

		// Collect new annotations
		HashMap<Annotation,Object> newAnnotations = new HashMap<Annotation,Object>();
		
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



	// 
	 // Overriden method from TextEditor which adds a JastAdd specific SourceViewerConfiguration
	 // and DocumentProvider.
	 //
	@Override
	protected void initializeEditor() {
		super.initializeEditor();		
		setDocumentProvider(new JastAddDocumentProvider());
	}

	//
	 // Overriden method from TextEditor which adds a JastAdd specific ContentOutline and
	 // BreakpointAdapter.
	 //
	@Override
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			if (fOutlinePage == null) {
				fOutlinePage =  new JastAddContentOutlinePage(this, model);
				if (getEditorInput() != null)
					fOutlinePage.setInput(getEditorInput());
			}
			return fOutlinePage;
		}
		return super.getAdapter(required);
	}


	//
	 // Overriden method from AbstractDecoratedTextEditor. Adds projection support
	 // which provides folding in the editor. Activates the JastAdd editor context which activates
	 // commands and keybindings related to the context.
	 //
	
	@Override
	
	public void createPartControl(Composite parent) {
		setEditorContextMenuId(getEditorSite().getId());

		super.createPartControl(parent);

		ProjectionViewer viewer = (ProjectionViewer)getSourceViewer();
		projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
		if (model != null) {
			projectionSupport.addSummarizableAnnotationType(ICompiler.ERROR_MARKER_ID); //$NON-NLS-1$
			projectionSupport.addSummarizableAnnotationType(ICompiler.WARNING_MARKER_ID); //$NON-NLS-1$
		}
		projectionSupport.setHoverControlCreator(new JastAddControlCreator());
		projectionSupport.install();
		getSourceViewerConfiguration();
		viewer.doOperation(ProjectionViewer.TOGGLE);

		folder = new JastAddEditorFolder(viewer.getProjectionAnnotationModel(), this);
		if (model != null)
			model.addListener(folder);

		if (model != null) {
			IContextService contextService = (IContextService) getSite().getService(IContextService.class);
			contextActivation = contextService.activateContext(getEditorContextID());
		}
	}
	
	
	//Overriden method from TextEditor which removes listeners and contexts. 
	 
	@Override 
	public void dispose() {
		super.dispose();

		IEditorInput input = getEditorInput();
		if (model != null) {
			if(input instanceof IFileEditorInput) {
				IFileEditorInput fileInput = (IFileEditorInput)input;
				IFile file = fileInput.getFile();
				final JastAddModel model = JastAddModelProvider.getModel(file);
				if (this.model == model) {
					FileInfoMap.releaseFileInfo(FileInfoMap.buildFileInfo(input));
				}
			} else if(input instanceof JastAddStorageEditorInput) {
				JastAddStorageEditorInput storageInput = (JastAddStorageEditorInput)input;
				FileInfoMap.releaseFileInfo(FileInfoMap.buildFileInfo(input));
			}
			model.removeListener(folder);
		}
		IContextService contextService = (IContextService) getSite().getService(IContextService.class);
		contextService.deactivateContext(contextActivation);
	}

	public void installInformationPresenter(InformationPresenter presenter) {
		presenter.install(getSourceViewer());
	}

	
	 // Overriden from AbstractDecoratedTextEditor. Adds a projection viewer which provides
	 // support for folding.
	 
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess = getAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());
		ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);
		return viewer;
	}

	
	// ControlCreator class used when creating the hover window for collapsed folding markers 
	 
	private class JastAddControlCreator implements IInformationControlCreator {
		public IInformationControl createInformationControl(Shell shell) {
			return new JastAddSourceInformationControl(shell, model);
		}
	}

	public void populateContextMenu(IMenuManager menuManager) {
	}

	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		populateContextMenu(menu);
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
*/
}
