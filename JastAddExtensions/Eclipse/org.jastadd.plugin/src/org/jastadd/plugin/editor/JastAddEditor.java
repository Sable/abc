package org.jastadd.plugin.editor;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.jastadd.plugin.editor.JastAddEditorContributor.ITopMenuActionBuilder;
import org.jastadd.plugin.editor.folding.JastAddEditorFolder;
import org.jastadd.plugin.editor.hover.JastAddSourceInformationControl;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;
import org.jastadd.plugin.outline.JastAddContentOutlinePage;
import org.jastadd.plugin.resources.JastAddDocumentProvider;

import sun.util.ResourceBundleEnumeration;

/**
 * JastAdd editor providing various JastAdd related editor features
 */
public abstract class JastAddEditor extends TextEditor {
	
	
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
				model.logError(e, "Problem with error check on save");
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

	/** 
	 * Overriden method from TextEditor which adds a JastAdd specific SourceViewerConfiguration
	 * and DocumentProvider.
	 */
	@Override
	protected void initializeEditor() {
		super.initializeEditor();		
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
				fOutlinePage =  new JastAddContentOutlinePage(this, model);
				if (getEditorInput() != null)
					fOutlinePage.setInput(getEditorInput());
			}
			return fOutlinePage;
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
		setEditorContextMenuId(getEditorSite().getId());

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
	    	    
	    if (model != null) {
	    	IContextService contextService = (IContextService) getSite().getService(IContextService.class);
	    	contextActivation = contextService.activateContext(getEditorContextID());
	    }
	}
	
	public abstract String getEditorContextID();
	
	/**
	 * Overriden method from TextEditor which removes listeners and contexts. 
	 */
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
					model.releaseFileInfo(model.buildFileInfo(input));
				}
			} else if(input instanceof JastAddStorageEditorInput) {
				JastAddStorageEditorInput storageInput = (JastAddStorageEditorInput)input;
				model.releaseFileInfo(model.buildFileInfo(input));
			}
			model.removeListener(folder);
		}
	    IContextService contextService = (IContextService) getSite().getService(IContextService.class);
	    contextService.deactivateContext(contextActivation);
	}
	
	public void installInformationPresenter(InformationPresenter presenter) {
		presenter.install(getSourceViewer());
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
	  	     return new JastAddSourceInformationControl(shell, model);
	  	   }
	}

	
	
	public void populateContextMenu(IMenuManager menuManager, JastAddEditor editor) {
	}
	
	
	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		populateContextMenu(menu, this);
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

	
	
	
	
}
