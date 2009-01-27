package org.jastadd.plugin.ui.view;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public abstract class AbstractBaseHierarchyView<T> extends ViewPart {
	
	protected Label label;
	protected Tree tree;
	protected TreeViewer treeViewer;
	protected Composite composite;
	protected Composite parentComposite;

	@SuppressWarnings("unchecked")
	protected static AbstractBaseHierarchyView activate(String viewID) throws CoreException {
		AbstractBaseHierarchyView view = (AbstractBaseHierarchyView) PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.showView(viewID, null, IWorkbenchPage.VIEW_ACTIVATE);
		return view;
	}

	public void createPartControl(Composite parentComposite) {
		this.parentComposite = parentComposite;
		composite = new Composite(parentComposite, SWT.NONE);
		composite.setFont(parentComposite.getFont());
		composite.setLayout(new GridLayout(1, false));

		label = new Label(composite, SWT.LEFT);
		configureLabel(label);

		tree = new Tree(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setFont(parentComposite.getFont());

		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		tree.setLayoutData(gridData);

		treeViewer = new TreeViewer(tree);
		configureTreeViewer(treeViewer);		
	}
	
	protected abstract void configureLabel(Label label);
	
	protected abstract void configureTreeViewer(TreeViewer treeViewer);
	
	protected void refreshLayout() {
		parentComposite.layout(true, true);
	}
	
	public void setFocus() {
		tree.setFocus();
	}
}
