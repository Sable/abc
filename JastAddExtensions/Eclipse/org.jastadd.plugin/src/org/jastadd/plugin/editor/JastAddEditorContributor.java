package org.jastadd.plugin.editor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.jastadd.plugin.model.JastAddEditorConfiguration;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;

public abstract class JastAddEditorContributor extends BasicTextEditorActionContributor {
	
	public void init(IActionBars bars) {
		super.init(bars);
		String editorID = getEditorID();
		populateCommands(editorID);
		populateTopMenu(bars.getMenuManager(), editorID);
	}	
	
	protected abstract String getEditorID();
	
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
			if (!(editor instanceof JastAddEditor)) {
				currentConf = null;
				this.setEnabled(false);
				return;
			}
			currentConf = modelToConf.get(((JastAddEditor)editor).getModel());
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
			model.registerCommands();
		}
	}
	
	private void populateTopMenu(IMenuManager menu, String editorId) {
		List<JastAddModel> models = JastAddModelProvider.getModels();
		for (final JastAddModel model : models) {
			if (!model.getEditorID().equals(editorId)) continue;
			
			model.getEditorConfiguration().populateTopMenu(
					menu,
					new JastAddEditorConfiguration.ITopMenuActionBuilder() {

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
	
	@Override
    public void setActiveEditor(IEditorPart part) {
    	super.setActiveEditor(part);

    	for(MenuAction m : menuActions) {
    		m.editorChanged(part);
    	}
    }
}