package org.jastadd.plugin.jastadd.properties;

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.jastadd.plugin.jastaddj.builder.ui.AddEditRemoveField;

public abstract class AddEditRemoveReorderField extends AddEditRemoveField {

	private StructuredViewer viewer;
	private boolean addFile;

	public AddEditRemoveReorderField(StructuredViewer viewer, String[] labels, boolean addFile) {
		super(viewer, labels);
		this.viewer = viewer;
		this.addFile = addFile;
	}

	public Control getControl(Composite parent) {
		
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setFont(parent.getFont());
		GridLayout buttonCompositeLayout = new GridLayout(1, false);
		buttonCompositeLayout.marginWidth = 0;
		buttonCompositeLayout.marginHeight = 0;
		buttonComposite.setLayout(buttonCompositeLayout);

		GridData buttonGridData = new GridData();
		buttonGridData.horizontalAlignment = SWT.FILL;
		buttonGridData.grabExcessHorizontalSpace = true;
		buttonGridData.minimumWidth = 140;

		final Button upButton = new Button(buttonComposite, SWT.PUSH);
		final Button downButton = new Button(buttonComposite, SWT.PUSH);
		final Label spacer = new Label(buttonComposite, SWT.HORIZONTAL & SWT.SEPARATOR);
		if (addFile) {
			final Button addFileButton = new Button(buttonComposite, SWT.PUSH);
			addFileButton.setText("Add &File");
			addFileButton.setLayoutData(buttonGridData);
			addFileButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					addFileCommand();
				}
	
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			final Label spacer2 = new Label(buttonComposite, SWT.HORIZONTAL & SWT.SEPARATOR);
		}
		
		upButton.setText("&Up");
		upButton.setLayoutData(buttonGridData);
		upButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (hasSelection()) {
					upCommand();
					updateUpDown(upButton,downButton);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});


		downButton.setText("&Down");
		downButton.setLayoutData(buttonGridData);
		downButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (hasSelection()) {
					downCommand();
					updateUpDown(upButton,downButton);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				updateUpDown(upButton,downButton);
			}

		});
		
		if (viewer instanceof TreeViewer) {
			((TreeViewer) viewer).addTreeListener(new ITreeViewerListener() {
	
				public void treeCollapsed(TreeExpansionEvent event) {
					updateUpDown(upButton,downButton);
				}
	
				public void treeExpanded(TreeExpansionEvent event) {
					updateUpDown(upButton,downButton);
				}
				
			});
		}
		
		
		// Create Add/Edit/Remove buttons
		super.getControl(buttonComposite);
		
		updateUpDown(upButton,downButton);
		
		return buttonComposite;
	}

	void updateUpDown(Button upButton, Button downButton) {
		if (viewer.getSelection().isEmpty()) {
			upButton.setEnabled(false);
			downButton.setEnabled(false);
		} else {
			upButton.setEnabled(!topOfSelection());
			downButton.setEnabled(!bottomOfSelection());
		}
	}
	
	protected abstract boolean topOfSelection();
	protected abstract boolean bottomOfSelection();
	protected abstract void upCommand();
	protected abstract void downCommand();
	protected abstract void addFileCommand();

}
