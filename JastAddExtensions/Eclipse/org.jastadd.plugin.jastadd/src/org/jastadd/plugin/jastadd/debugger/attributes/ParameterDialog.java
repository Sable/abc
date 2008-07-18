package org.jastadd.plugin.jastadd.debugger.attributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jastadd.plugin.jastadd.debugger.attributes.AttributeEvaluation.NonPrimitiveTypeException;

/**
 * Displays a list of parameters that are required for a particular method,
 * including their types, and allows the user to input values for each.
 * 
 * Only works for primitive values.
 * @author luke
 *
 */
public class ParameterDialog extends Dialog {
	private static final String FIELD_NAME = "FIELD_NAME";

	private Composite panel;
	private String title;
	
	private List<FieldSummary> fields = new LinkedList<FieldSummary>();
	private List<Validator> validators = new ArrayList<Validator>();
	private List<Control> controlList = new ArrayList<Control>();
	private Map<String, String> valueMap = new HashMap<String, String>();
	private IJavaDebugTarget target;
	
	public ParameterDialog(Shell parent, String title, IJavaDebugTarget target) {
		super(parent);
		this.title = title;
		this.target = target;

		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createButtonBar(Composite parent) {
		Control bar = super.createButtonBar(parent);
		validateFields();
		return bar;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite)super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		panel = new Composite(container, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		for (FieldSummary field : fields) {
			createTextField(field);
		}
		
		Dialog.applyDialogFont(container);
		return container;
	}
	
	public void createTextField(final FieldSummary field) {
		Label label = new Label(panel, SWT.NONE);
		label.setText(field.name + " (" + field.type + ")");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		final Text text = new Text(panel, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.setData(FIELD_NAME, field.name);
		
		// make sure rows are the same height on both panels.
		label.setSize(label.getSize().x, text.getSize().y); 
		
		if (field.initialValue != null) {
			text.setText(field.initialValue);
		}
		
		// Ensure that this is a valid value for the field
		validators.add(new Validator() {
			public boolean validate() {
				try {
					AttributeEvaluation.newPrimitiveValue(target, text.getText(), field.type); //$NON-NLS-1$
					return true;
				} catch (NumberFormatException ex) {
					return false;
				} catch (NonPrimitiveTypeException e) {
					return false;
				}
			}
		});
		
		if (!field.allowsEmpty) {
			validators.add(new Validator() {
				public boolean validate() {
					return !text.getText().equals("") || field.type.equals("String"); //$NON-NLS-1$
				}
			});
			text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateFields();
				}
			});
		}
		
		controlList.add(text);
	}
	
	public void addField(String name, String type, String initialValue, boolean allowsEmpty) {
		fields.add(new FieldSummary(name, type, initialValue, allowsEmpty));
	}
	
	public String getValue(String key) {
		return valueMap.get(key);
	}
	
	protected void okPressed() {
		for (Control control : controlList) {
			if (control instanceof Text) {
				valueMap.put((String) control.getData(FIELD_NAME), ((Text)control).getText());
			}
		}
		controlList = null;
		super.okPressed();
	}
	
	public void validateFields() {
		for(Validator validator : validators) {
			if (!validator.validate()) {
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				return;
			}
		}
		getButton(IDialogConstants.OK_ID).setEnabled(true);
	}
	
	protected class Validator {
		boolean validate() {
			return true;
		}
	}
	
	protected class FieldSummary {
		String type;
		String name;
		String initialValue;
		boolean allowsEmpty;
		
		public FieldSummary(String name,String type, String initialValue, boolean allowsEmpty) {
			this.type = type;
			this.name = name;
			this.initialValue = initialValue;
			this.allowsEmpty = allowsEmpty;
		}
	}
	
}
