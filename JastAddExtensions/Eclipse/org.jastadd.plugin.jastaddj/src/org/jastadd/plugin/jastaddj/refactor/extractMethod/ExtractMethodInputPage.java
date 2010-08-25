package org.jastadd.plugin.jastaddj.refactor.extractMethod;

import java.io.IOException;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.jastadd.plugin.jastaddj.util.FileUtil;

import AST.Block;
import AST.Modifier;

public class ExtractMethodInputPage extends UserInputWizardPage {

	private ControlDecoration fClassNameDecoration;

	public ExtractMethodInputPage(String name) {
		super(name);
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		Composite result= new Composite(parent, SWT.NONE);
		result.setLayout(new GridLayout(2, false));
		createMethodNameInput(result);
		createVisibilityInput(result);
		createStatementSequenceInput(result);
		setControl(result);
	}
	
	private void createVisibilityInput(Composite group) {
		final ExtractMethodRefactoring refactoring = (ExtractMethodRefactoring)getRefactoring();
		Label l= new Label(group, SWT.NONE);
		l.setText("Visibility:");

		final Composite gp = new Composite(group, SWT.NONE);
		gp.setLayout(new GridLayout(4, false));
		
		Button pub = new Button(gp, SWT.RADIO);
		pub.setText("public");
		pub.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				((ExtractMethodRefactoring)getRefactoring()).setVisibility(Modifier.VIS_PUBLIC);
			}
		});
		Button prot = new Button(gp, SWT.RADIO);
		prot.setText("protected");
		prot.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				((ExtractMethodRefactoring)getRefactoring()).setVisibility(Modifier.VIS_PROTECTED);
			}
		});
		Button pack = new Button(gp, SWT.RADIO);
		pack.setText("package");
		pack.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				((ExtractMethodRefactoring)getRefactoring()).setVisibility(Modifier.VIS_PACKAGE);
			}
		});
		Button priv = new Button(gp, SWT.RADIO);
		priv.setText("private");
		priv.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				((ExtractMethodRefactoring)getRefactoring()).setVisibility(Modifier.VIS_PRIVATE);
			}
		});
		
		switch (((ExtractMethodRefactoring)getRefactoring()).getVisibility()) {
		case Modifier.VIS_PUBLIC:
			pub.setSelection(true);
			break;
		case Modifier.VIS_PROTECTED:
			prot.setSelection(true);
			break;
		case Modifier.VIS_PACKAGE:
			pack.setSelection(true);
			break;
		case Modifier.VIS_PRIVATE:
			priv.setSelection(true);
			break;
		default:
			throw new RuntimeException("Unhandled case");
		}
		
	}
	
	private void createMethodNameInput(Composite result) {
		Label label = new Label(result, SWT.LEAD);
		label.setText("Method name:");
		final Text text= new Text(result, SWT.SINGLE | SWT.BORDER);
		fClassNameDecoration = new ControlDecoration(text, SWT.TOP | SWT.LEAD);
		text.setText(((ExtractMethodRefactoring)getRefactoring()).getMethodName());
		text.selectAll();
		text.setFocus();
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				((ExtractMethodRefactoring)getRefactoring()).setMethodName(text.getText());
			}

		});
		GridData gridData= new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent= FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		text.setLayoutData(gridData);
	}
	
	private void createStatementSequenceInput(Composite result) {
		final Block block = (Block) ((ExtractMethodRefactoring) getRefactoring()).getSelectedNode();
		Label label = new Label(result, SWT.LEAD);
		label.setText("Statements:");
		
		Composite stmts = new Composite(result, SWT.NONE);
		stmts.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		stmts.setLayout(new GridLayout(1, false));
		
		Composite sel = new Composite(stmts, SWT.NONE);
		sel.setLayout(new GridLayout(2, false));

		new Label(sel, SWT.LEAD).setText("Begin:");
		final Slider begin = new Slider(sel, SWT.HORIZONTAL);
		new Label(sel, SWT.LEAD).setText("End:");
		final Slider end = new Slider(sel, SWT.HORIZONTAL);

		final StyledText st = new StyledText(stmts, SWT.READ_ONLY); // TODO: scrolling ????
		st.setWordWrap(true);
		st.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		st.setText(getBlockSource(block));
		
		begin.setValues(0, 0, block.getNumStmt(), 1, 1, 3);
		begin.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				((ExtractMethodRefactoring) getRefactoring()).setBegin(begin.getSelection());
				if (begin.getSelection() > end.getSelection()) {
					end.setSelection(begin.getSelection());
					((ExtractMethodRefactoring) getRefactoring()).setEnd(begin.getSelection());
				}
				updateStyledText(st, block, begin.getSelection(), end.getSelection());
			}
		});
		

		end.setValues(0, 0, block.getNumStmt(), 1, 1, 3);
		end.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				((ExtractMethodRefactoring) getRefactoring()).setEnd(end.getSelection());
				if (begin.getSelection() > end.getSelection()) {
					begin.setSelection(end.getSelection());
					((ExtractMethodRefactoring) getRefactoring()).setBegin(end.getSelection());
				}
				updateStyledText(st, block, begin.getSelection(), end.getSelection());
			}
		});
		

		updateStyledText(st, block, begin.getSelection(), end.getSelection());
		
	}
	
	private String getBlockSource(Block block) {
		String s = "";
		try {
			s = FileUtil.readTextFile(block.compilationUnit().pathName());
		} catch (IOException e) {
		}
		return s.substring(block.getBeginOffset(), block.getEndOffset());
	}
	
	private void updateStyledText(StyledText st, Block block, int begin, int end) {
		int blockOffset = block.getBeginOffset();
		
		StyleRange sr = new StyleRange();
		sr.start = block.getStmt(begin).getBeginOffset() - blockOffset;
		sr.length = block.getStmt(end).getEndOffset() - blockOffset - sr.start;
		sr.foreground = st.getDisplay().getSystemColor(SWT.COLOR_BLUE);
		StyleRange[] s = { sr };
		st.setStyleRanges(s);
	}
		
}
