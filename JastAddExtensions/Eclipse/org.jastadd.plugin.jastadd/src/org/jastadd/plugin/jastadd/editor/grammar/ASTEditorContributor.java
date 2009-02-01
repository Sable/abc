package org.jastadd.plugin.jastadd.editor.grammar;

import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;

public class ASTEditorContributor  extends BasicTextEditorActionContributor {

	/*
	protected String getEditorContextID() {
		return ASTEditor.EDITOR_CONTEXT_ID;
	}

	protected String getEditorID() {
		return ASTEditor.EDITOR_ID;
	}

	protected void registerStopHandler(Runnable stopHandler) {
		Activator.INSTANCE.addStopHandler(stopHandler);
	}

	
	public void init(IActionBars bars) {
		super.init(bars);
		String editorID = getEditorID();
		populateCommands(editorID);
		populateTopMenu(bars.getMenuManager(), editorID);
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
		Map<JastAddModel, MenuActionConf> modelToConf = new HashMap<JastAddModel, MenuActionConf>();
		
		public MenuAction(String id) {
			this.setId(id);
			this.setEnabled(false);
		}
		
		public void enhance(String text, String definitionId, IActionDelegate delegate, JastAddModel model) {
			MenuActionConf conf = new MenuActionConf(text, definitionId, delegate);
			if (modelToConf.isEmpty()) {
				currentConf = conf;
				this.setText(currentConf.text);
				this.setActionDefinitionId(currentConf.definitionId);
			}
			modelToConf.put(model, conf);
		}

		public void editorChanged(IEditorPart editor) {
			if (!(editor instanceof ASTEditor)) {
				currentConf = null;
				this.setEnabled(false);
				return;
			}
			currentConf = modelToConf.get(((ASTEditor)editor).getModel());
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

	private Set<MenuAction> menuActions = new HashSet<MenuAction>();
	
	private void populateCommands(String editorId) {
		List<JastAddModel> models = JastAddModelProvider.getModels();
		for (final JastAddModel model : models) {
			if (!model.getEditorID().equals(editorId)) continue;
			registerCommands();
		}
	}

	private boolean commandsPopulated = false;

	public void registerCommands() {
		if (commandsPopulated) return;
		try {
			populateCommands();
			commandsPopulated = true;
		}
		catch(Exception e) {
		}		
	}
	
	public void populateCommands() throws ParseException, IOException {
	}

	
	private void populateTopMenu(IMenuManager menu, String editorId) {
		List<JastAddModel> models = JastAddModelProvider.getModels();
		for (final JastAddModel model : models) {
			if (!model.getEditorID().equals(editorId)) 
				continue;
			populateTopMenu(
				menu,
				new ITopMenuActionBuilder() {
					public IAction buildAction(String id, String text, String definitionId, IActionDelegate actionDelegate) {
						MenuAction menuAction = new MenuAction(id);
						menuAction.enhance(text, definitionId, actionDelegate, model);
						menuActions.add(menuAction);
						return menuAction;
					}					
					public void enhanceAction(IAction action, String text, String definitionId, IActionDelegate actionDelegate) {
						MenuAction menuAction = (MenuAction)action;
						menuAction.enhance(text, definitionId, actionDelegate, model);
						menuActions.add(menuAction);
					}						
				});
		}
	}
	
	public void populateTopMenu(IMenuManager menuManager, ITopMenuActionBuilder actionBuilder) {
	}
	
	public interface ITopMenuActionBuilder {
		public IAction buildAction(String id, String text, String definitionId, IActionDelegate actionDelegate);
		public void enhanceAction(IAction action, String text, String definitionId, IActionDelegate actionDelegate);
	}

	
	@Override
    public void setActiveEditor(IEditorPart part) {
    	super.setActiveEditor(part);

    	for(MenuAction m : menuActions) {
    		m.editorChanged(part);
    	}
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
	
	protected Binding buildKeyBinding(Command command, String keySequence, String context)
			throws ParseException, IOException {
		return new KeyBinding(KeySequence
				.getInstance(keySequence), new ParameterizedCommand(command,
				null), getActiveBindingsScheme(),
				context, null,
				null, null, KeyBinding.SYSTEM);
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
	
*/
	
}
