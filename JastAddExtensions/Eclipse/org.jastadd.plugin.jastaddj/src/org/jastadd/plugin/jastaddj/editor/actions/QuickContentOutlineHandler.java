package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.compiler.ast.IOutlineNode;
import org.jastadd.plugin.jastaddj.editor.JastAddJEditor;
import org.jastadd.plugin.jastaddj.util.FileUtil;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;
import org.jastadd.plugin.ui.popup.AbstractBaseTreeInformationControl;
import org.jastadd.plugin.ui.popup.AbstractBaseInformationPresenter;
import org.jastadd.plugin.ui.popup.AbstractBaseInformationProvider;
import org.jastadd.plugin.ui.view.JastAddContentProvider;
import org.jastadd.plugin.ui.view.JastAddLabelProvider;

public class QuickContentOutlineHandler extends AbstractBaseActionDelegate {

	@Override
	public void run(IAction action) {
		IInformationControlCreator informationControlCreator = new IInformationControlCreator() {
			public IInformationControl createInformationControl(
					Shell parent) {
				return new AbstractBaseTreeInformationControl(parent,
						"Outline") {
					protected void configure() {
						treeViewer.setContentProvider(new JastAddContentProvider());
						treeViewer.setLabelProvider(new JastAddLabelProvider());
					}
					
					protected void gotoSelectedElement() {
						IJastAddNode source = (IJastAddNode)((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
						FileUtil.openFile(source);
					}
					
					public void setInput(Object input) {
						super.setInput(activeEditorPart().getEditorInput());
						IJastAddNode node = (IJastAddNode)input;
						synchronized (((IASTNode)node).treeLockObject()) {
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
		AbstractBaseInformationPresenter informationManager = new AbstractBaseInformationPresenter(
				informationControlCreator) {
			protected void setInformationProviders() {
				setInformationProvider(new AbstractBaseInformationProvider() {
					public IJastAddNode filterNode(IJastAddNode node) {
						return node;
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
