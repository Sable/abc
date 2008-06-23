package org.jastadd.plugin.jastaddj.builder.ui;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeViewerListener;
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

public abstract class AddEditRemoveField {
	private StructuredViewer viewer;
	private String[] labels;
	
	public AddEditRemoveField(StructuredViewer viewer, String[] labels) {
		this.viewer = viewer;
		this.labels = labels;
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

		Button addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setText(labels[0]);
		addButton.setLayoutData(buttonGridData);
		addButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				addCommand();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		final Button editButton = new Button(buttonComposite, SWT.PUSH);
		editButton.setText(labels[1]);
		editButton.setLayoutData(buttonGridData);
		editButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (hasSelection())
					editCommand();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		final Button removeButton = new Button(buttonComposite, SWT.PUSH);
		removeButton.setText(labels[2]);
		removeButton.setLayoutData(buttonGridData);
		removeButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (hasSelection())
					removeCommand();			
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		viewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						updateButtonsEnabled(editButton, removeButton);
					}
				});
		updateButtonsEnabled(editButton, removeButton);
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (hasSelection())
					editCommand();
			}
		});
		
		if (viewer instanceof TreeViewer) {
			((TreeViewer) viewer).addTreeListener(new ITreeViewerListener() {
	
				public void treeCollapsed(TreeExpansionEvent event) {
					updateButtonsEnabled(editButton, removeButton);
				}
	
				public void treeExpanded(TreeExpansionEvent event) {
					updateButtonsEnabled(editButton, removeButton);
				}
				
			});
		}
		
		return buttonComposite;
	}
	
	void updateButtonsEnabled(Button editButton, Button removeButton) {
		boolean hasSelection = hasSelection();
		editButton.setEnabled(hasSelection);
		removeButton.setEnabled(hasSelection);		
	}
	
	StructuredViewer getViewer() {
		return viewer;
	}
	
	protected abstract boolean hasSelection();
	protected abstract void addCommand();
	protected abstract void editCommand();
	protected abstract void removeCommand();
}
