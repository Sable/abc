package org.jastadd.plugin.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.keys.IBindingService;
import org.jastadd.plugin.AST.IFoldingNode;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.editor.JastAddEditor;
import org.jastadd.plugin.editor.highlight.JastAddAutoIndentStrategy;
import org.jastadd.plugin.editor.hover.JastAddTextHover;
import org.jastadd.plugin.providers.JastAddContentProvider;
import org.jastadd.plugin.providers.JastAddLabelProvider;

public abstract class JastAddEditorConfiguration {
	
	protected JastAddModel model;
	
	public JastAddEditorConfiguration() {
		this.model = null;
	}
	
	public JastAddEditorConfiguration(JastAddModel model) {
		this.model = model;
	}
	
	
	// No default insertion tactics after newline is provided
	public void getDocInsertionAfterNewline(IDocument doc, DocumentCommand cmd) {
	}
	
	// No default insertion on keypress is provided
	public void getDocInsertionOnKeypress(IDocument doc, DocumentCommand cmd) {
	}
	

	// No default syntax highlighting is provided
	/*
	public ITokenScanner getScanner() {
		return null;
	}
	*/

		// No default is provided
	public IContentAssistProcessor getCompletionProcessor() {
		return null;
	}
	
	// Uses attribute values from ContentOutline.jrag
	public ITreeContentProvider getContentProvider() {
		return new JastAddContentProvider();
	}

	// Uses attribute values from ContentOutline.jrag
	public IBaseLabelProvider getLabelProvider() {
		return new JastAddLabelProvider();
	}

	// Uses attribute values from Hover.jrag
	public ITextHover getTextHover() {
		return new JastAddTextHover(model);
	}
	
	// Uses attribute values from Folding.jrag
	public List<Position> getFoldingPositions(IDocument document) {
		try {
			IJastAddNode node = model.getTreeRoot(document);
			if (node != null) {
				synchronized (node.treeLockObject()) {
					if (node != null && node instanceof IFoldingNode) {
						return ((IFoldingNode)node).foldingPositions(document);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<Position>();
	}
	
	
	// Override getDocInsertionAfterNewline(IDocument doc, DocumentCommand cmd)
	// or getDocInsertionOnKeypress(IDocument doc, DocumentCommand cmd) before
	// considering to override this method
	public IAutoEditStrategy getAutoIndentStrategy() {
		return new JastAddAutoIndentStrategy(model);
	}
	
	public abstract String getEditorContextID();
	
	public String getErrorMarkerID() {
		return "org.eclipse.ui.workbench.texteditor.error";
	}
	
	public String getWarningMarkerID() {
		return "org.eclipse.ui.workbench.texteditor.warning";
	}
	
	public interface ITopMenuActionBuilder {
		public IAction buildAction(String id, String text, String definitionId, IActionDelegate actionDelegate);
		public void enhanceAction(IAction action, String text, String definitionId, IActionDelegate actionDelegate);
	}
	
	public void populateCommands() throws ParseException, IOException {
	}
	
	public void populateTopMenu(IMenuManager menuManager, ITopMenuActionBuilder actionBuilder) {
	}
	
	public void populateContextMenu(IMenuManager menuManager, JastAddEditor editor) {
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
	
	protected IMenuManager findOrAddFindContextMenu(IMenuManager menuManager) {
		return findOrAddMenu(menuManager, ".find.popup", "F&ind");
	}
	
	protected IMenuManager findOrAddRefactorContextMenu(IMenuManager menuManager) {
		return findOrAddMenu(menuManager, ".refactor.popup", "Refac&tor");
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

	protected IHandlerActivation registerCommandHandler(String commandId,
			IHandler handler) {
		IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench()
				.getAdapter(IHandlerService.class);
		return handlerService.activateHandler(commandId, handler);
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
			model.registerStopHandler(new Runnable() {
				public void run() {
					command.undefine();
				}
			});
		}	
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

	private boolean stringsEqual(String s1, String s2) {
		return s1 == null ? s1 == s2 : s1.equals(s2);
	}
	
	protected Binding buildKeyBinding(Command command, String keySequence, String context)
			throws ParseException, IOException {
		return new KeyBinding(KeySequence
				.getInstance(keySequence), new ParameterizedCommand(command,
				null), getActiveBindingsScheme(),
				context, null,
				null, null, KeyBinding.SYSTEM);
	}
}
