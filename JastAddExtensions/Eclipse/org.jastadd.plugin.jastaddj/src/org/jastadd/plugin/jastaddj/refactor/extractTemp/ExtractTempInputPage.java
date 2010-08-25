package org.jastadd.plugin.jastaddj.refactor.extractTemp;

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

public class ExtractTempInputPage extends UserInputWizardPage {

	private ControlDecoration fClassNameDecoration;

	public ExtractTempInputPage(String name) {
		super(name);
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		Composite result= new Composite(parent, SWT.NONE);
		result.setLayout(new GridLayout(2, false));
		createVarNameInput(result);
		createMakeFinalInput(result);
		setControl(result);
	}
	
	private void createMakeFinalInput(Composite group) {
		final ExtractTempRefactoring refactoring = (ExtractTempRefactoring)getRefactoring();
		Label l= new Label(group, SWT.NONE);
		l.setText("Make Final:");

		final Button box = new Button(group, SWT.CHECK);
		box.setSelection(refactoring.isMakeFinal());
		box.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				refactoring.setMakeFinal(box.getSelection());
			}
		});
		GridData gridData= new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent= FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		box.setLayoutData(gridData);
	}
	
	private void createVarNameInput(Composite result) {
		Label label = new Label(result, SWT.LEAD);
		label.setText("Variable name:");
		final Text text= new Text(result, SWT.SINGLE | SWT.BORDER);
		fClassNameDecoration = new ControlDecoration(text, SWT.TOP | SWT.LEAD);
		text.setText(((ExtractTempRefactoring)getRefactoring()).getVarName());
		text.selectAll();
		text.setFocus();
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				((ExtractTempRefactoring)getRefactoring()).setVarName(text.getText());
			}

		});
		GridData gridData= new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent= FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		text.setLayoutData(gridData);
	}
		
}
