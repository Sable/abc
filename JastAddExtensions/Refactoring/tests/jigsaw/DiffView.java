package tests.jigsaw;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import tests.jigsaw.DiffMatchPatch.Diff;
import tests.jigsaw.DiffMatchPatch.Operation;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import AST.ASTNode;
import AST.ClassDecl;
import AST.CompilationUnit;
import AST.MethodDecl;
import AST.Problem;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class DiffView extends JPanel {
	private static JFrame frame;
	private JTabbedPane tabbedPane;
	private JLabel status;
	
	private RealProgramTests.BenchmarkProgram benchmark = RealProgramTests.BenchmarkProgram.apachecodec;
	private String typeName = "org.apache.commons.codec.digest.DigestUtils";
	private String ifaceName = "p.I";
	
	private Program prog = null;
	private RealProgramTests.BenchmarkProgram prog_is_for_benchmark = null;
	
	public DiffView() {
		setLayout(new BorderLayout());
		
		// create combo box for selecting benchmark
		final JComboBox benchbox = new JComboBox(RealProgramTests.BenchmarkProgram.values());
		benchbox.setSelectedIndex(benchmark.ordinal());
		benchbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				benchmark = RealProgramTests.BenchmarkProgram.values()[benchbox.getSelectedIndex()];
			}
		});
		benchbox.setToolTipText("Select a benchmark to run the refactoring on.");
		
		// create text field for selecting type
		final JTextField typeTextField = new JTextField(20);
		typeTextField.setText(typeName);
		typeTextField.setToolTipText("Enter the fully qualified name of the class to extract from.");
		
		// create text field for new interface name
		final JTextField ifaceTextField = new JTextField(20);
		ifaceTextField.setText(ifaceName);
		ifaceTextField.setToolTipText("Enter the fully qualified name of the interface to extract into.");
		
		// create "Go" button
		JButton goButton = new JButton("Go!");
		goButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				typeName = typeTextField.getText();
				ifaceName = ifaceTextField.getText();
				new Thread() {
					public void run() {
						doExtract();
					}
				}.start();
			}
		});
		
		// create labels
		JLabel benchboxLabel = new JLabel("Select benchmark:");
		benchboxLabel.setLabelFor(benchbox);
		JLabel typeTextFieldLabel = new JLabel("Class to extract from:");
		typeTextFieldLabel.setLabelFor(typeTextField);
		JLabel ifaceTextFieldLabel = new JLabel("Name of new interface:");
		ifaceTextFieldLabel.setLabelFor(ifaceTextField);
		
		// create status bar
		status = new JLabel("Ready");
		status.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		
		JPanel controlsPane = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		controlsPane.setLayout(gbl);
		controlsPane.setBorder(BorderFactory.createCompoundBorder(
									BorderFactory.createBevelBorder(BevelBorder.LOWERED),
									BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		addLabelTextRows(new JLabel[]{benchboxLabel, typeTextFieldLabel, ifaceTextFieldLabel}, 
						 new JComponent[]{benchbox, typeTextField, ifaceTextField}, 
						 gbl, controlsPane);
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER; //last
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        controlsPane.add(goButton, c);
        
        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		
		add(controlsPane, BorderLayout.NORTH);
		add(tabbedPane, BorderLayout.CENTER);
		add(status, BorderLayout.SOUTH);
	}
	
	private void addLabelTextRows(JLabel[] jLabels, JComponent[] jComponents, GridBagLayout gbl, JPanel controlsPane) {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		int n = jLabels.length;
		
		for(int i=0;i<n;++i) {
			c.gridwidth = GridBagConstraints.RELATIVE;
			c.fill = GridBagConstraints.NONE;
			c.weightx = 0.0;
			controlsPane.add(jLabels[i], c);
			
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			controlsPane.add(jComponents[i], c);
		}
	}

	private static void createAndShowGUI() {
		frame = new JFrame("Diff View");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new DiffView());
		frame.pack();
		frame.setVisible(true);
	}
	
	private void doExtract() {
		try {
			setBusyCursor();
			Assert.assertNotNull("No benchmark selected.", benchmark);
			if(prog == null || benchmark != prog_is_for_benchmark) {
				status("Compiling...");
				prog = benchmark.compile();
				prog_is_for_benchmark = benchmark;
			}
			TypeDecl tp = prog.findType(typeName);
			Assert.assertNotNull("There is no type '" + typeName + "' in " + benchmark + ".", tp);
			Assert.assertTrue("Type '" + typeName + "' is not a class.", tp instanceof ClassDecl);
			int idx = ifaceName.lastIndexOf('.');
			String pkg = idx == -1 ? "" : ifaceName.substring(0, idx);
			String name = ifaceName.substring(idx+1, ifaceName.length());
			Collection<MethodDecl> methods = new LinkedList<MethodDecl>();
			for(Iterator<MethodDecl> iter=tp.localMethodsIterator();iter.hasNext();) {
				MethodDecl meth = iter.next();
				if(!meth.isStatic() && meth.isPublic())
					methods.add(meth);
			}
			
			// now do the refactoring
			status("Applying refactoring...");
			Program.startRecordingASTChangesAndFlush();
			((ClassDecl)tp).doExtractInterface(pkg, name, methods);
			
			// check for errors
			status("Checking for errors...");
			LinkedList<Problem> errors = new LinkedList<Problem>();
			prog.errorCheck(errors);
			if(!errors.isEmpty()) {
				error(errors.toString());
				prog.clearErrors();			
			}
			
			status("Computing changes...");
			Map<String, String> changedCUs = ASTNode.computeChanges(Program.getUndoStack());
			Program.undoAll();
			prog.flushCaches();
			
			clearTabs();
			for(Map.Entry<String, String> changedCU : changedCUs.entrySet()) {
				String pathName = changedCU.getKey(),
					   newCU = changedCU.getValue();
				CompilationUnit cu = null;
				for(Iterator<CompilationUnit> iter=prog.compilationUnitIterator();iter.hasNext();) {
					CompilationUnit ccu = iter.next();
					if(ccu.pathName() != null && ccu.pathName().equals(pathName)) {
						cu = ccu;
						break;
					}
				}
				DiffMatchPatch differ = new DiffMatchPatch();
				LinkedList<Diff> diffs = differ.diff_compute(cu == null ? "" : cu.toString(), newCU, true);
				boolean hasChanges = false;
				for(Diff d : diffs) {
					if(d.text.length() > 0 && d.operation != Operation.EQUAL) {
						hasChanges = true;
						break;
					}
				}
				if(hasChanges)
					addTab(pathName, diffs);
			}
		} catch(AssertionFailedError afe) {
			error(afe.getMessage());
			prog = null;
		} catch(RefactoringException rfe) {
			error(rfe.getMessage());
			prog = null;
		} catch(Exception e) {
			e.printStackTrace();
			prog = null;
		} finally {
			setNormalCursor();
			status("Done");
		}
	}
	
	private void setBusyCursor() {
		if(frame != null)
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				}
			});
	}
	
	private void setNormalCursor() {
		if(frame != null)
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					frame.setCursor(null);
				}
			});
	}
	
	private void clearTabs() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				tabbedPane.removeAll();
			}
		});
	}
	
	private void status(final String msg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				status.setText(msg);
			}
		});
	}
	
	private int MIN_WIDTH = 200, MIN_HEIGHT = 100;
	private int DEFLT_WIDTH = 600, DEFLT_HEIGHT = 400;
	private void addTab(final String cuName, final Collection<Diff> diffs) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Style deflt = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
				
				JTextPane oldPane = new JTextPane();
				StyledDocument oldDoc = oldPane.getStyledDocument();
				Style deleted = oldDoc.addStyle("deleted", deflt);
				StyleConstants.setBackground(deleted, Color.darkGray);

				JTextPane newPane = new JTextPane();
				StyledDocument newDoc = newPane.getStyledDocument();
				Style added = newDoc.addStyle("added", deflt);
				StyleConstants.setBackground(added, Color.lightGray);
				
				try {
					for(Diff d : diffs) {
						if(d.text.length() == 0)
							continue;
						switch(d.operation) {
						case EQUAL:
							oldDoc.insertString(oldDoc.getLength(), d.text, deflt);
							newDoc.insertString(newDoc.getLength(), d.text, deflt);
							break;
						case INSERT:
							newDoc.insertString(newDoc.getLength(), d.text, added);
							break;
						case DELETE:
							oldDoc.insertString(oldDoc.getLength(), d.text, deleted);
							break;
						}
					}
				} catch (BadLocationException e) {
					e.printStackTrace();
				}

				JScrollPane oldPaneScroll = new JScrollPane(oldPane);
				oldPaneScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				oldPaneScroll.setPreferredSize(new Dimension(DEFLT_WIDTH, DEFLT_HEIGHT));
				oldPaneScroll.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));

				JScrollPane newPaneScroll = new JScrollPane(newPane);
				newPaneScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				newPaneScroll.setPreferredSize(new Dimension(DEFLT_WIDTH, DEFLT_HEIGHT));
				newPaneScroll.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));

				JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, oldPaneScroll, newPaneScroll);
				splitPane.setOneTouchExpandable(true);
				splitPane.setResizeWeight(0.5);
				
				int idx = cuName.lastIndexOf("/");
				tabbedPane.addTab(cuName.substring(idx+1, cuName.length()), splitPane);
				
				frame.pack();
			}
		});
	}
	
	private void error(final String msg) {
		if(frame == null)
			System.err.println(msg);
		else
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
				}
			});
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
