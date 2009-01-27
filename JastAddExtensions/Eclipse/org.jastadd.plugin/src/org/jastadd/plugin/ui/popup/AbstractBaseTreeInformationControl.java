/**
 * 
 */
package org.jastadd.plugin.ui.popup;

import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

public abstract class AbstractBaseTreeInformationControl extends AbstractBaseInformationControl
		implements IInformationControl, IInformationControlExtension2 {

	private int treeStyle;
	private int treeHeight;
	private int treeWidth;

	protected Tree tree;
	protected TreeViewer treeViewer;

	public AbstractBaseTreeInformationControl(Shell parent, String title,
			int shellStyle, int treeStyle, int treeWidth, int treeHeight) {
		super(parent, shellStyle, true, false, false, false, false, null, null);

		this.treeStyle = treeStyle;
		this.treeWidth = treeWidth;
		this.treeHeight = treeHeight;

		if (title != null)
			this.setTitleText(title);
		
		create();
	}

	public AbstractBaseTreeInformationControl(Shell parent, String title) {
		this(parent, title, SWT.RESIZE, SWT.V_SCROLL | SWT.H_SCROLL, 60, 12);
	}

	protected Control createDialogArea(Composite parent) {
		GC gc = new GC(parent);
		gc.setFont(parent.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();

		tree = new Tree(parent, treeStyle);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = fontMetrics.getAverageCharWidth() * treeWidth;
		gd.heightHint = tree.getItemHeight() * treeHeight;
		tree.setLayoutData(gd);

		treeViewer = new TreeViewer(tree);
		tree.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				gotoSelectedElement();
				dispose();
			}
		});
		tree.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				if (e.button != 1)
					return;
				if (tree.getItem(new Point(e.x, e.y)) == null) return;

				gotoSelectedElement();
				dispose();
			}
		});		
		configure();
		return tree;
	}

	protected abstract void configure();
	
	protected abstract void gotoSelectedElement();

	public void setInput(Object input) {
		treeViewer.setInput(input);
		treeViewer.refresh();
		treeViewer.expandAll();
	}

	public void setFocus() {
		super.setFocus();
		tree.setFocus();
	}

	public boolean isFocusControl() {
		return treeViewer.getControl().isFocusControl();
	}
}