package org.jastadd.plugin.jastaddj.refactor.pullUpMethod;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import AST.FieldDeclaration;

public class PullUpMethodInputPage extends UserInputWizardPage {

	private ControlDecoration fClassNameDecoration;

	public PullUpMethodInputPage(String name) {
		super(name);
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		Composite result= new Composite(parent, SWT.NONE);
		result.setLayout(new GridLayout(2, false));
		createOnlyAbstractInput(result);
		setControl(result);
	}
	
	private void createOnlyAbstractInput(Composite group) {
		final PullUpMethodRefactoring refactoring = (PullUpMethodRefactoring)getRefactoring();
		Label l= new Label(group, SWT.NONE);
		l.setText("Pull up abstract method only:");

		final Button box = new Button(group, SWT.CHECK);
		box.setSelection(refactoring.isOnlyAbstract());
		box.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				refactoring.setOnlyAbstract(box.getSelection());
			}
		});
		GridData gridData= new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent= FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		box.setLayoutData(gridData);
	}
	
}
