package org.jastadd.plugin.jastaddj.refactor.addParameter;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.jastadd.plugin.ReconcilingStrategy;
import org.jastadd.plugin.jastaddj.editor.JastAddJSourceViewerConfiguration;

import AST.MethodDecl;

public class AddParameterInputPage extends UserInputWizardPage {
	private AddParameterRefactoring refactoring;
	private SourceViewer preview;
	
	public AddParameterInputPage(String name) {
		super(name);
	}
	
	public void createControl(Composite parent) {
		refactoring = (AddParameterRefactoring)getRefactoring();
		
		refactoring.setParmName("newParam");
		refactoring.setType("java.lang.Object");
		refactoring.setDefaultValue("null");
		refactoring.setCreateDelegate(false);
		
		int nargs = refactoring.getMethod().getNumParameter();
		
		initializeDialogUnits(parent);
		Composite result = new Composite(parent, SWT.NONE);
		result.setLayout(new GridLayout(1, false));
		
		Composite sel = new Composite(result, SWT.NONE);
		sel.setLayout(new GridLayout(2, false));
		
		createLabelAndText(sel, "Parameter name:", "newParam", new TextModificationListener() {
			public void modified(String text) {
				refactoring.setParmName(text);
				refresh_preview();
			}
		});
		
		createLabelAndText(sel, "Parameter type:", "java.lang.Object", new TextModificationListener() {
			public void modified(String text) {
				refactoring.setType(text);
				refresh_preview();
			}
		});
		
		createLabelAndText(sel, "Default value:", "null", new TextModificationListener() {
			public void modified(String text) {
				refactoring.setDefaultValue(text);
				refresh_preview();
			}
		});
		
		createLabel(sel, "Create delegate:");
		final Button delegate = new Button(sel, SWT.CHECK);
		delegate.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				refactoring.setCreateDelegate(delegate.getSelection());
				refresh_preview();
			}
		});
		
		createLabel(sel, "Position:");
		final Slider pos = new Slider(sel, SWT.HORIZONTAL);
		pos.setValues(nargs, 0, nargs+1, 1, 1, 1);
		pos.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				refactoring.setParmPos(pos.getSelection());
				refresh_preview();
			}
		});
		
		Group preview_group = new Group(result, SWT.SHADOW_ETCHED_IN);
		preview_group.setText("Preview");
		preview_group.setLayout(new GridLayout(1, false));
		preview_group.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		preview = new SourceViewer(preview_group, new VerticalRuler(10), SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		preview.configure(new JastAddJSourceViewerConfiguration(new ReconcilingStrategy()));
		preview.setDocument(new Document(preview_signature()));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = convertHeightInCharsToPixels(5);
		preview.getControl().setLayoutData(gd);

		setControl(result);
		
		refresh_preview();
	}
	
	private abstract class TextModificationListener {
		public abstract void modified(String text);
	}
	
	private void createLabelAndText(Composite result, String labelText, String deflt, final TextModificationListener listener) {
		createLabel(result, labelText);
		
		final Text textInput = new Text(result, SWT.BORDER);
		textInput.setText(deflt);
		textInput.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				listener.modified(textInput.getText());
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = convertWidthInCharsToPixels(25);
		textInput.setLayoutData(gd);
	}

	private void createLabel(Composite result, String labelText) {
		Label label = new Label(result, SWT.NONE);
		label.setText(labelText);
	}
	
	private void refresh_preview() {
		if(preview != null)
			preview.setDocument(new Document(preview_signature()));		
	}
	
	private String preview_signature() {
		StringBuffer sig = new StringBuffer();
		MethodDecl md = refactoring.getMethod();
		
		sig.append(md.type().toString());
		sig.append(" ");
		sig.append(md.name());
		sig.append("(");
		
		for(int i=0;i<=md.getNumParameter();++i) {
			if(i == refactoring.getParmPos()) {
				if(i != 0)
					sig.append(", ");
				sig.append(preview_parm(refactoring.getParmType(), refactoring.getParmName(), false));
				if(i == 0)
					sig.append(", ");
			}
			if(i < md.getNumParameter()) {
				if(i != 0)
					sig.append(", ");
				sig.append(preview_parm(md.getParameter(i).getTypeAccess().toString(), md.getParameter(i).name(), 
										md.getParameter(i).isVariableArity()));
			}
		}
		
		sig.append(")");
		
		if(md.getNumException() > 0) {
			sig.append(" throws ");
			for(int i=0;i<md.getNumException();++i) {
				if(i != 0)
					sig.append(", ");
				sig.append(md.getException(i).toString());
			}
		}
		
		sig.append(" {\n");
		sig.append("    ...\n");
		sig.append("}");
		
		return sig.toString();
	}
	
	private String preview_parm(String type, String name, boolean vararg) {
		return type + (vararg ? "..." : "") + " " + name;
	}
}
