package org.jastadd.plugin.jastaddj.editor;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.ParameterizedCommand;
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
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.jastadd.plugin.jastaddj.Activator;
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
import org.jastadd.plugin.jastaddj.refactor.Refactorings;
import org.jastadd.plugin.jastaddj.refactor.Refactorings.RefactoringInfo;

public class JastAddJEditorContributor extends BasicTextEditorActionContributor {

	
	private Set<MenuAction> menuActions = new HashSet<MenuAction>();
	private boolean commandsPopulated = false;
	
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorActionBarContributor#init(org.eclipse.ui.IActionBars)
	 */
	public void init(IActionBars bars) {
		super.init(bars);
		String editorID = getEditorID();
		populateCommands(editorID);
		populateTopMenu(bars.getMenuManager(), editorID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.BasicTextEditorActionContributor#setActiveEditor(org.eclipse.ui.IEditorPart)
	 */
    public void setActiveEditor(IEditorPart part) {
    	super.setActiveEditor(part);
    	for(MenuAction m : menuActions) {
    		m.editorChanged(part);
    	}
    }

	
    /**
     * 
     */
	public void registerCommands() {
		if (commandsPopulated) return;
		try {
			populateCommands();
			commandsPopulated = true;
		}
		catch(Exception e) {
		}		
	}
	
	/**
	 * 
	 * @param menuManager
	 * @param actionBuilder
	 */
	public void populateTopMenu(IMenuManager menuManager,
			ITopMenuActionBuilder actionBuilder) {
		IMenuManager refactorMenu = findOrAddRefactorTopMenu(menuManager);
		populateRefactorTopMenuItems(refactorMenu, actionBuilder);

		IMenuManager findMenu = findOrAddFindTopMenu(menuManager);
		populateFindTopMenuItems(findMenu, actionBuilder);
	}
	
	/**
	 * 
	 * @throws ParseException
	 * @throws IOException
	 */
	public void populateCommands() throws ParseException, IOException {
		installSourceCommand(
				"org.jastadd.plugin.jastaddj.find.FindDeclaration",
				"Find Declaration", "JastAddJ Find Declaration", "F3",
				new FindDeclarationHandler());

		installSourceCommand("org.jastadd.plugin.jastaddj.find.FindReferences",
				"Find References", "JastAddJ Find References", "Ctrl+Shift+G",
				new FindReferencesHandler());

		installSourceCommand("org.jastadd.plugin.jastaddj.find.FindImplements",
				"Find Implements", "JastAddJ Find Implements", "Ctrl+I",
				new FindImplementsHandler());

		installSourceCommand("org.jastadd.plugin.jastaddj.query.ReferenceHierarchy",
				"Reference Hierarchy", "JastAddJ Reference Hierarchy", "Ctrl+Alt+R",
				new ReferenceHierarchyHandler());
		
		installSourceCommand("org.jastadd.plugin.jastaddj.query.TypeHierarchy",
				"Type Hierarchy", "JastAddJ Type Hierarchy", "Ctrl+Alt+T",
				new TypeHierarchyHandler());

		installSourceCommand("org.jastadd.plugin.jastaddj.query.QuickTypeHierarchy",
				"Quick Type Hierarchy", "JastAddJ Quick Type Hierarchy", "Ctrl+T",
				new QuickTypeHierarchyHandler());
		
		installSourceCommand("org.jastadd.plugin.jastaddj.query.QuickContentOutline",
				"Quick Outline", "JastAddJ Quick Outline", "Ctrl+O",
				new QuickContentOutlineHandler());
		
		for(RefactoringInfo<?> info : Refactorings.refactorings)
			installSourceCommand(info.getId(), info.getName(), info.getDescription(),
								 info.getKeyBinding(), info.newHandler());
		
		/*
		installSourceCommand("org.jastadd.plugin.jastaddj.completion",
				"Completion", "JastAddJ Completion", "Ctrl+Space", 
				new CompletionHandler());
		*/
		
	}

	
	public interface ITopMenuActionBuilder {
		public IAction buildAction(String id, String text, String definitionId, IActionDelegate actionDelegate);
		public void enhanceAction(IAction action, String text, String definitionId, IActionDelegate actionDelegate);
	}	
	



	
	protected String getEditorContextID() {
		return JastAddJEditor.EDITOR_CONTEXT_ID;
	}

	protected String getEditorID() {
		return JastAddJEditor.EDITOR_ID;
	}
	
	protected void registerStopHandler(Runnable stopHandler) {
		Activator.INSTANCE.addStopHandler(stopHandler);
	}
		
	protected Category getCategory(String categoryId) {
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
				.getAdapter(ICommandService.class);
		Category category = commandService.getCategory(categoryId);
		if (!category.isDefined())
			throw new IllegalStateException("Category '" + categoryId
					+ "' not found!");
		return category;
	}
	
	protected Command registerCommand(String commandId, Category category,
			String name, String description) {
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
				.getAdapter(ICommandService.class);
		Command command = commandService.getCommand(commandId);
		if (!command.isDefined()) {
			command.define(name, description, category);
			return command;
		}
		else
			return null;
	}
	
	protected void installCommand(String commandId, String name,
			String description, String categoryId, String keySequence, IHandler handler)
			throws ParseException, IOException {
		final Command command = registerCommand(commandId,
				getCategory(categoryId), name,
				description);
		if (command != null) {
			if (keySequence != null)
				setCommandBinding(command, keySequence);
			registerCommandHandler(commandId, handler);
			registerStopHandler(new Runnable() {
				public void run() {
					command.undefine();
				}
			});
		}	
	}
	
	protected IHandlerActivation registerCommandHandler(String commandId,
			IHandler handler) {
		IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench()
				.getAdapter(IHandlerService.class);
		return handlerService.activateHandler(commandId, handler);
	}
	
	protected String getActiveBindingsScheme() {
		IBindingService bindingService = (IBindingService) PlatformUI.getWorkbench()
				.getAdapter(IBindingService.class);
		return bindingService.getActiveScheme().getId();
	}
	
	protected void setCommandBinding(Command command, String keySequence)
			throws ParseException, IOException {
		registerBinding(buildKeyBinding(command, keySequence, getEditorContextID()));
	}
	
	protected void registerBinding(Binding newBinding) throws IOException {
		IBindingService bindingService = (IBindingService) PlatformUI.getWorkbench()
				.getAdapter(IBindingService.class);
		Binding[] bindings = bindingService.getBindings();

		Binding[] newBindings = new Binding[bindings.length+1];
		System.arraycopy(bindings, 0, newBindings, 0, bindings.length);
		newBindings[bindings.length] = newBinding;

		bindingService.savePreferences(bindingService.getActiveScheme(),
				newBindings);
	}
	
	protected boolean bindingShadowed(Binding newBinding) {
		IBindingService bindingService = (IBindingService) PlatformUI.getWorkbench().getAdapter(IBindingService.class);
		Binding[] bindings = bindingService.getBindings();
		for(Binding binding : bindings)
			if (deletesBinding(binding, newBinding))
				return true;
		return false;
	}

	protected Binding buildKeyBinding(Command command, String keySequence, String context)
	throws ParseException, IOException {
		return new KeyBinding(KeySequence
				.getInstance(keySequence), new ParameterizedCommand(command,
						null), getActiveBindingsScheme(),
						context, null,
						null, null, KeyBinding.SYSTEM);
	}


	
	
	private void populateCommands(String editorId) {
		/*
		List<JastAddModel> models = JastAddModelProvider.getModels();
		for (final JastAddModel model : models) {
			if (!model.getEditorID().equals(editorId)) continue;
			registerCommands();
		}
		*/
		
		registerCommands();
	}
	
	private void populateTopMenu(IMenuManager menu, String editorId) {
		//List<JastAddModel> models = JastAddModelProvider.getModels();
		//for (final JastAddModel model : models) {		
//			if (!editorId.equals(JastAddJEditor.EDITOR_ID)) 
//				continue;
		
			populateTopMenu(
				menu,
				new ITopMenuActionBuilder() {
					public IAction buildAction(String id, String text, String definitionId, IActionDelegate actionDelegate) {
						MenuAction menuAction = new MenuAction(id);
						menuAction.enhance(text, definitionId, actionDelegate); //, model);
						menuActions.add(menuAction);
						return menuAction;
					}					
					public void enhanceAction(IAction action, String text, String definitionId, IActionDelegate actionDelegate) {
						MenuAction menuAction = (MenuAction)action;
						menuAction.enhance(text, definitionId, actionDelegate); //, model);
						menuActions.add(menuAction);
					}						
				});
		//}
	}
	
	private boolean deletesBinding(Binding oldBinding, Binding newBinding) {
		return stringsEqual(oldBinding.getContextId(), newBinding
				.getContextId())
				&& stringsEqual(oldBinding.getLocale(), newBinding.getLocale())
				&& stringsEqual(oldBinding.getPlatform(), newBinding
						.getPlatform())
				&& stringsEqual(oldBinding.getSchemeId(), newBinding
						.getSchemeId())
				&& oldBinding.getParameterizedCommand() != null
				&& oldBinding.getParameterizedCommand().equals(
						newBinding.getParameterizedCommand());
	}
	
	private boolean stringsEqual(String s1, String s2) {
		return s1 == null ? s1 == s2 : s1.equals(s2);
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
	
	protected IMenuManager findOrAddFindTopMenu(IMenuManager menuManager) {
		return findOrAddMenu(menuManager, ".find.menu", "F&ind");
	}
	
	protected IMenuManager findOrAddRefactorTopMenu(IMenuManager menuManager) {
		return findOrAddMenu(menuManager, ".refactor.menu", "Refac&tor");
	}
	
	protected IAction tryEnhanceTopMenuItem(IMenuManager menuManager, ITopMenuActionBuilder actionBuilder, String id, String text, String definitionId, IActionDelegate actionDelegate) {
		IContributionItem item = menuManager.find(id);
		if (item != null && item instanceof ActionContributionItem) {
			IAction action = ((ActionContributionItem)item).getAction();
			actionBuilder.enhanceAction((IAction)action, text, definitionId, actionDelegate);
			return null;
		}
		return actionBuilder.buildAction(id, text, definitionId, actionDelegate);
	}

	protected void addOrEnhanceTopMenuItem(IMenuManager menuManager, ITopMenuActionBuilder actionBuilder, String id, String text, String definitionId, IActionDelegate actionDelegate) {
		IAction action = tryEnhanceTopMenuItem(menuManager, actionBuilder, id, text, definitionId, actionDelegate);
		if (action != null)
			menuManager.insertAfter("additions", action);
	}
	
	
	protected void installSourceCommand(String commandId, String name,
			String description, String keySequence, IHandler handler)
			throws ParseException, IOException {
		installCommand(commandId, name, description,
				"org.jastadd.plugin.category.Source", keySequence, handler);
	}

	protected void populateFindTopMenuItems(IMenuManager searchMenu,
			ITopMenuActionBuilder actionBuilder) {

		addOrEnhanceTopMenuItem(searchMenu, actionBuilder,
				"org.jastadd.plugin.jastaddj.find.FindDeclarationTopMenuItem",
				"Find Declaration",
				"org.jastadd.plugin.jastaddj.find.FindDeclaration",
				new FindDeclarationHandler());

		addOrEnhanceTopMenuItem(searchMenu, actionBuilder,
				"org.jastadd.plugin.jastaddj.find.FindReferencesTopMenuItem",
				"Find References",
				"org.jastadd.plugin.jastaddj.find.FindReferences",
				new FindReferencesHandler());

		addOrEnhanceTopMenuItem(searchMenu, actionBuilder,
				"org.jastadd.plugin.jastaddj.find.FindImplementsTopMenuItem",
				"Find &Implements",
				"org.jastadd.plugin.jastaddj.find.FindImplements",
				new FindImplementsHandler());
	}

	protected void populateRefactorTopMenuItems(IMenuManager refactorMenu,
			ITopMenuActionBuilder actionBuilder) {
		for(RefactoringInfo<?> info : Refactorings.refactorings)
			addOrEnhanceTopMenuItem(refactorMenu, actionBuilder, 
				info.getTopMenuId(), info.getMenuText(), info.getId(), info.newHandler());
	}	

	private static class MenuActionConf {
		public String text;
		public String definitionId;
		private IActionDelegate delegate;
		
		public MenuActionConf(String text, String definitionId,
				IActionDelegate delegate) {
			this.text = text;
			this.definitionId = definitionId;
			this.delegate = delegate;
		}
	}
	
	private static class MenuAction extends Action {
		MenuActionConf currentConf;
		Map<String, MenuActionConf> modelToConf = new HashMap<String, MenuActionConf>();
		
		public MenuAction(String id) {
			this.setId(id);
			this.setEnabled(false);
		}
		
		public void enhance(String text, String definitionId, IActionDelegate delegate) {//, JastAddModel model) {
			MenuActionConf conf = new MenuActionConf(text, definitionId, delegate);
			if (modelToConf.isEmpty()) {
				currentConf = conf;
				this.setText(currentConf.text);
				this.setActionDefinitionId(currentConf.definitionId);
			}
			modelToConf.put(JastAddJEditor.EDITOR_ID, conf);
		}

		public void editorChanged(IEditorPart editor) {
			if (!(editor instanceof JastAddJEditor)) {
				currentConf = null;
				this.setEnabled(false);
				return;
			}
			currentConf = modelToConf.get(JastAddJEditor.EDITOR_ID);
			if (currentConf != null) {
				this.setText(currentConf.text);
				this.setActionDefinitionId(currentConf.definitionId);
				this.setEnabled(true);
			}
			else
				this.setEnabled(false);
		}
		
		@Override
		public void run() {
			if (currentConf != null)
				currentConf.delegate.run(this);
		}
	};
}
