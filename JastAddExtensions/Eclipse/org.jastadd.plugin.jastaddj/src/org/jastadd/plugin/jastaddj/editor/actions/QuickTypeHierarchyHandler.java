package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.jastaddj.editor.JastAddJEditor;
import org.jastadd.plugin.jastaddj.util.FileUtil;
import org.jastadd.plugin.jastaddj.view.JastAddJTypeHierarchyContentProvider;
import org.jastadd.plugin.jastaddj.view.JastAddJTypeHierarchyView;
import org.jastadd.plugin.search.JastAddOnDemandTreeItem;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;
import org.jastadd.plugin.ui.popup.AbstractBaseInformationPresenter;
import org.jastadd.plugin.ui.popup.AbstractBaseInformationProvider;
import org.jastadd.plugin.ui.popup.AbstractBaseTreeInformationControl;
import org.jastadd.plugin.ui.view.JastAddLabelProvider;
import org.jastadd.plugin.ui.view.JastAddOnDemandTreeLabelProviderAdapter;

public class QuickTypeHierarchyHandler extends AbstractBaseActionDelegate {

	@Override
	public void run(IAction action) {
		IInformationControlCreator informationControlCreator = new IInformationControlCreator() {
			public IInformationControl createInformationControl(
					Shell parent) {
				return new AbstractBaseTreeInformationControl(parent,
						"Type Hierarchy") {
					protected void configure() {
						treeViewer.setContentProvider(new JastAddJTypeHierarchyContentProvider());
						treeViewer.setLabelProvider(new JastAddOnDemandTreeLabelProviderAdapter(
										new JastAddLabelProvider(new LabelProvider())));
					}
					
					protected void gotoSelectedElement() {
						JastAddOnDemandTreeItem<IJastAddNode> source = (JastAddOnDemandTreeItem<IJastAddNode>)((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
						//JastAddModel model = JastAddModelProvider.getModel(source.value);
						//if (model != null)
						FileUtil.openFile(source.value);
					}
				};
			}

		};
		AbstractBaseInformationPresenter informationManager = new AbstractBaseInformationPresenter(
				informationControlCreator) {
			protected void setInformationProviders() {
				setInformationProvider(new AbstractBaseInformationProvider() {
					public IJastAddNode filterNode(IJastAddNode node) {
						return JastAddJTypeHierarchyView.filterNode(node);
					}
				}, IDocument.DEFAULT_CONTENT_TYPE);
			}
		};
		
		IEditorPart part = activeEditorPart();
		if (part instanceof JastAddJEditor) {
			JastAddJEditor editor = (JastAddJEditor)part;
			editor.showInformationPresenter(informationManager);
			//informationManager.run((JastAddEditor) this.activeEditorPart());
		}
	}
}
