package org.jastadd.plugin.jastaddj.refactor.extractClass;

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

public class ExtractClassInputPage extends UserInputWizardPage {

	private ControlDecoration fClassNameDecoration;

	public ExtractClassInputPage(String name) {
		super(name);
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		Composite result= new Composite(parent, SWT.NONE);
		result.setLayout(new GridLayout(2, false));
		createClassNameInput(result);
		createTable(result);
		createFieldNameInput(result);
		createEncapsulateInput(result);
		createTopLevelInput(result);
		setControl(result);
	}
	
	private void createFieldNameInput(Composite group) {
		final ExtractClassRefactoring refactoring = (ExtractClassRefactoring)getRefactoring();
		Label l= new Label(group, SWT.NONE);
		l.setText("Field name:");

		final Text text= new Text(group, SWT.BORDER);
		ControlDecoration fParameterNameDecoration = new ControlDecoration(text, SWT.TOP | SWT.LEAD);
		text.setText(refactoring.getFieldName());
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				refactoring.setFieldName(text.getText());
			}

		});
		GridData gridData= new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent= FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		text.setLayoutData(gridData);
	}
	
	private void createEncapsulateInput(Composite group) {
		final ExtractClassRefactoring refactoring = (ExtractClassRefactoring)getRefactoring();
		Label l= new Label(group, SWT.NONE);
		l.setText("Encapsulate:");

		final Button box = new Button(group, SWT.CHECK);
		box.setSelection(refactoring.getEncapsulate());
		box.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				refactoring.setEncapsulate(box.getSelection());
			}
		});
		GridData gridData= new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent= FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		box.setLayoutData(gridData);
	}
	
	private void createTopLevelInput(Composite group) {
		final ExtractClassRefactoring refactoring = (ExtractClassRefactoring)getRefactoring();
		Label l= new Label(group, SWT.NONE);
		l.setText("Top level:");

		final Button box = new Button(group, SWT.CHECK);
		box.setSelection(refactoring.getTopLevel());
		box.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				refactoring.setTopLevel(box.getSelection());
			}
		});
		GridData gridData= new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent= FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		box.setLayoutData(gridData);
	}
	
	private void createClassNameInput(Composite result) {
		Label label = new Label(result, SWT.LEAD);
		label.setText("Class name:");
		final Text text= new Text(result, SWT.SINGLE | SWT.BORDER);
		fClassNameDecoration = new ControlDecoration(text, SWT.TOP | SWT.LEAD);
		text.setText(((ExtractClassRefactoring)getRefactoring()).getClassName());
		text.selectAll();
		text.setFocus();
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				((ExtractClassRefactoring)getRefactoring()).setClassName(text.getText());
			}

		});
		GridData gridData= new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent= FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		text.setLayoutData(gridData);
	}
	
	private void createTable(Composite parent) {
		Composite result= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout(2, false);
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		result.setLayout(layout);
		GridData gridData= new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan= 2;
		result.setLayoutData(gridData);

		Label l= new Label(result, SWT.NONE);
		l.setText("Select fields for extracted class:");
		gridData= new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan= 2;
		gridData.verticalIndent= 5;
		l.setLayoutData(gridData);

		TableLayoutComposite layoutComposite= new TableLayoutComposite(result, SWT.NONE);
		layoutComposite.addColumnData(new ColumnWeightData(40, convertWidthInCharsToPixels(20), true));
		layoutComposite.addColumnData(new ColumnWeightData(60, convertWidthInCharsToPixels(20), true));
		final CheckboxTableViewer tv= CheckboxTableViewer.newCheckList(layoutComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tv.setContentProvider(new FieldContentProvider());
		createColumns(tv);

		Table table= tv.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		gridData= new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gridData);
		ExtractClassRefactoring ecf = (ExtractClassRefactoring)getRefactoring();
		tv.setInput(ecf);
		for (FieldDeclaration field : ecf.getFields()) {
			tv.setChecked(field, !field.isStatic() && !field.isEnumConstant());
		}
		tv.refresh(true);
		gridData= new GridData(GridData.FILL_BOTH);
		layoutComposite.setLayoutData(gridData);
		Composite controls= new Composite(result, SWT.NONE);
		gridData= new GridData(GridData.FILL, GridData.FILL, false, false);
		controls.setLayoutData(gridData);
		GridLayout gridLayout= new GridLayout();
		gridLayout.marginHeight= 0;
		gridLayout.marginWidth= 0;
		controls.setLayout(gridLayout);

		tv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				FieldDeclaration element= (FieldDeclaration)event.getElement();
				if(event.getChecked())
					((ExtractClassRefactoring)getRefactoring()).addField(element);
				else
					((ExtractClassRefactoring)getRefactoring()).removeField(element);
				tv.refresh(element, true);
			}

		});
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection= (IStructuredSelection) tv.getSelection();
				FieldDeclaration field= (FieldDeclaration)selection.getFirstElement();
			}
		});
	}
	
	private abstract class FieldInfoLabelProvider extends CellLabelProvider {
		public void update(ViewerCell cell) {
			FieldDeclaration pi= (FieldDeclaration)cell.getElement();
			cell.setText(doGetValue(pi));
		}

		protected abstract String doGetValue(FieldDeclaration pi);
	}

	private void createColumns(final CheckboxTableViewer tv) {
		TextCellEditor cellEditor= new TextCellEditor(tv.getTable());

		TableViewerColumn viewerColumn= new TableViewerColumn(tv, SWT.LEAD);
		viewerColumn.setLabelProvider(new FieldInfoLabelProvider() {
			protected String doGetValue(FieldDeclaration pi) {
				return pi.getTypeAccess().toString();
			}
		});

		TableColumn column= viewerColumn.getColumn();
		column.setText("Type");
		viewerColumn= new TableViewerColumn(tv, SWT.LEAD);
		viewerColumn.setLabelProvider(new FieldInfoLabelProvider() {
			protected String doGetValue(FieldDeclaration pi) {
				return pi.name();
			}
		});
		column= viewerColumn.getColumn();
		column.setText("Name");
	}	
	
	public class FieldContentProvider implements IStructuredContentProvider {
		public void dispose() {	}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {	}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof ExtractClassRefactoring) {
				return ((ExtractClassRefactoring)inputElement).getFields().toArray();
			}
			return null;
		}

	}	
}
