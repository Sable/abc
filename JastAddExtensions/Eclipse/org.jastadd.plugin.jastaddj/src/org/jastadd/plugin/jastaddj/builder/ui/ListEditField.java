package org.jastadd.plugin.jastaddj.builder.ui;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;


abstract class ListEditField<T> {
	private final List<T> data;
	
	ListEditField(List<T> data) {
		this.data = data;
	}
	
	Control getControl(Composite parent) {
		Composite dataComposite = new Composite(parent, SWT.NONE);
		dataComposite.setFont(parent.getFont());
		GridLayout dataCompositeLayout = new GridLayout(2, false);
		dataCompositeLayout.marginWidth = 0;
		dataCompositeLayout.marginHeight = 0;
		dataComposite.setLayout(dataCompositeLayout);

		UIUtil.stretchControl(dataComposite);

		org.eclipse.swt.widgets.List dataList = new org.eclipse.swt.widgets.List(dataComposite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL);
		dataList.setFont(parent.getFont());
		UIUtil.stretchControl(dataList);
		final ListViewer dataViewer = new ListViewer(dataList);
		dataViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return data.toArray();
			}
			
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			public void dispose() {
			}			
		});
		dataViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return formatElement(element);
			}
		});
		dataViewer.setInput(data);
		
		AddEditRemoveField addEditRemoveField = buildAddEditRemoveField(dataViewer);	
		Control buttonControl = addEditRemoveField.getControl(dataComposite);
		
		GridData buttonControlGridData = new GridData();
		buttonControlGridData.verticalAlignment = SWT.TOP;
		buttonControlGridData.verticalSpan = 2;
		buttonControl.setLayoutData(buttonControlGridData);
		
		return dataComposite;
	}
	
	List<T> getData() {
		return data;
	}
	
	protected abstract AddEditRemoveField buildAddEditRemoveField(StructuredViewer dataViewer);
	
	protected abstract String formatElement(Object element);

}
