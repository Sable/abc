package org.jastadd.plugin.jastaddj.refactor.extractInterface;

import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.jastadd.plugin.jastaddj.refactor.extractClass.TableLayoutComposite;

import AST.MethodDecl;

public class ExtractInterfaceInputPage extends UserInputWizardPage {
	ExtractInterfaceRefactoring refactoring;
	
	public ExtractInterfaceInputPage(String name) {
		super(name);
	}

	@Override
	public void createControl(Composite parent) {
		refactoring = (ExtractInterfaceRefactoring)getRefactoring();
		initializeDialogUnits(parent);
		Composite result= new Composite(parent, SWT.NONE);
		result.setLayout(new GridLayout(2, false));
		createInterfaceNameInput(result);
		createPackageNameInput(result);
		createTable(result);
		setControl(result);
	}
	
	private void createInterfaceNameInput(Composite result) {
		Label label = new Label(result, SWT.LEAD);
		label.setText("Interface name:");
		final Text text= new Text(result, SWT.SINGLE | SWT.BORDER);
		text.setText(refactoring.getIfaceName());
		text.selectAll();
		text.setFocus();
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				refactoring.setIfaceName(text.getText());
			}

		});
		GridData gridData= new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent= FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		text.setLayoutData(gridData);
	}
	
	private void createPackageNameInput(Composite result) {
		Label label = new Label(result, SWT.LEAD);
		label.setText("Package:");
		final Text text = new Text(result, SWT.SINGLE | SWT.BORDER);
		text.setText(refactoring.getPackageName());
		text.selectAll();
		text.setFocus();
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				refactoring.setPackageName(text.getText());
			}

		});
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
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
		l.setText("Select methods for extracted interface:");
		gridData= new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan= 2;
		gridData.verticalIndent= 5;
		l.setLayoutData(gridData);

		TableLayoutComposite layoutComposite= new TableLayoutComposite(result, SWT.NONE);
		layoutComposite.addColumnData(new ColumnWeightData(40, convertWidthInCharsToPixels(20), true));
		layoutComposite.addColumnData(new ColumnWeightData(60, convertWidthInCharsToPixels(20), true));
		final CheckboxTableViewer tv= CheckboxTableViewer.newCheckList(layoutComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tv.setContentProvider(new MethodContentProvider());
		createColumns(tv);

		Table table= tv.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		gridData= new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gridData);
		tv.setInput(refactoring);
		for(MethodDecl md : refactoring.getMethods()) {
			tv.setChecked(md, !md.isStatic());
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
				MethodDecl element= (MethodDecl)event.getElement();
				if(event.getChecked())
					refactoring.addMethod(element);
				else
					refactoring.removeMethod(element);
				tv.refresh(element, true);
			}

		});
	}
	
	private abstract class MethodInfoLabelProvider extends CellLabelProvider {
		public void update(ViewerCell cell) {
			MethodDecl pi = (MethodDecl)cell.getElement();
			cell.setText(doGetValue(pi));
		}

		protected abstract String doGetValue(MethodDecl pi);
	}

	private void createColumns(final CheckboxTableViewer tv) {
		TextCellEditor cellEditor= new TextCellEditor(tv.getTable());

		TableViewerColumn viewerColumn= new TableViewerColumn(tv, SWT.LEAD);
		viewerColumn.setLabelProvider(new MethodInfoLabelProvider() {
			protected String doGetValue(MethodDecl pi) {
				return pi.getTypeAccess().toString();
			}
		});

		TableColumn column= viewerColumn.getColumn();
		column.setText("Return Type");
		viewerColumn= new TableViewerColumn(tv, SWT.LEAD);
		viewerColumn.setLabelProvider(new MethodInfoLabelProvider() {
			protected String doGetValue(MethodDecl pi) {
				return pi.signature();
			}
		});
		column= viewerColumn.getColumn();
		column.setText("Signature");
	}	
	
	public class MethodContentProvider implements IStructuredContentProvider {
		public void dispose() {	}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {	}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof ExtractInterfaceRefactoring) {
				return ((ExtractInterfaceRefactoring)inputElement).getMethods().toArray();
			}
			return null;
		}

	}	
}
