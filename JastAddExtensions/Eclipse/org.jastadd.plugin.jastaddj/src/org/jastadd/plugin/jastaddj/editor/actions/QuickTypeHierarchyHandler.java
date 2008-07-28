package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.editor.JastAddEditor;
import org.jastadd.plugin.editor.actions.JastAddActionDelegate;
import org.jastadd.plugin.information.JastAddInformationControl;
import org.jastadd.plugin.information.JastAddInformationPresenter;
import org.jastadd.plugin.information.JastAddInformationProvider;
import org.jastadd.plugin.jastaddj.view.JastAddJTypeHierarchyContentProvider;
import org.jastadd.plugin.jastaddj.view.JastAddJTypeHierarchyView;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;
import org.jastadd.plugin.providers.JastAddLabelProvider;
import org.jastadd.plugin.providers.JastAddOnDemandTreeLabelProviderAdapter;
import org.jastadd.plugin.providers.model.JastAddOnDemandTreeItem;

public class QuickTypeHierarchyHandler extends JastAddActionDelegate {

	@Override
	public void run(IAction action) {
		IInformationControlCreator informationControlCreator = new IInformationControlCreator() {
			public IInformationControl createInformationControl(
					Shell parent) {
				return new JastAddInformationControl(parent,
						"Type Hierarchy") {
					protected void configure() {
						treeViewer.setContentProvider(new JastAddJTypeHierarchyContentProvider());
						treeViewer.setLabelProvider(new JastAddOnDemandTreeLabelProviderAdapter(
										new JastAddLabelProvider(new LabelProvider())));
					}
					
					protected void gotoSelectedElement() {
						JastAddOnDemandTreeItem<IJastAddNode> source = (JastAddOnDemandTreeItem<IJastAddNode>)((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
						JastAddModel model = JastAddModelProvider.getModel(source.value);
						if (model != null)
							model.openFile(source.value);
					}
				};
			}

		};
		JastAddInformationPresenter informationManager = new JastAddInformationPresenter(
				informationControlCreator) {
			protected void setInformationProviders() {
				setInformationProvider(new JastAddInformationProvider() {
					public IJastAddNode filterNode(IJastAddNode node) {
						return JastAddJTypeHierarchyView.filterNode(node);
					}
				}, IDocument.DEFAULT_CONTENT_TYPE);
			}
		};
		informationManager.run((JastAddEditor) this.activeEditorPart());
	}
}
