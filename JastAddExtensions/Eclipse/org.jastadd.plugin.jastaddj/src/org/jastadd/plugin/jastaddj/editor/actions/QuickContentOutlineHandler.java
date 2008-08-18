package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.AST.IOutlineNode;
import org.jastadd.plugin.editor.JastAddEditor;
import org.jastadd.plugin.editor.actions.JastAddActionDelegate;
import org.jastadd.plugin.information.JastAddInformationControl;
import org.jastadd.plugin.information.JastAddInformationPresenter;
import org.jastadd.plugin.information.JastAddInformationProvider;
import org.jastadd.plugin.model.JastAddEditorConfiguration;

public class QuickContentOutlineHandler extends JastAddActionDelegate {

	@Override
	public void run(IAction action) {
		IInformationControlCreator informationControlCreator = new IInformationControlCreator() {
			public IInformationControl createInformationControl(
					Shell parent) {
				return new JastAddInformationControl(parent,
						"Outline") {
					protected void configure() {
						JastAddEditorConfiguration config = activeEditorConfiguration();
						if (config != null) {
							treeViewer.setContentProvider(config.getContentProvider());
							treeViewer.setLabelProvider(config.getLabelProvider());
						}
					}
					
					protected void gotoSelectedElement() {
						IJastAddNode source = (IJastAddNode)((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
						activeModel().openFile(source);
					}
					
					public void setInput(Object input) {
						super.setInput(activeEditorPart().getEditorInput());
						IJastAddNode node = (IJastAddNode)input;
						synchronized (node.treeLockObject()) {
							while (node != null && !(node instanceof IOutlineNode && ((IOutlineNode)node).showInContentOutline()))
								node = node.getParent();					
							if (node != null) {
								treeViewer.setSelection(new StructuredSelection(node), true);
							}
						}
					}
				};
			}

		};
		JastAddInformationPresenter informationManager = new JastAddInformationPresenter(
				informationControlCreator) {
			protected void setInformationProviders() {
				setInformationProvider(new JastAddInformationProvider() {
					public IJastAddNode filterNode(IJastAddNode node) {
						return node;
					}
				}, IDocument.DEFAULT_CONTENT_TYPE);
			}
		};
		informationManager.run((JastAddEditor) this.activeEditorPart());
	}
}
