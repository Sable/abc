package tests.jigsaw;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import tests.CompileHelper;
import tests.jigsaw.DiffMatchPatch.Diff;
import tests.jigsaw.DiffMatchPatch.Operation;
import AST.ASTNode;
import AST.ClassDecl;
import AST.CompilationUnit;
import AST.MethodDecl;
import AST.Problem;
import AST.Program;
import AST.RefactoringException;
import AST.SimpleSet;
import AST.TypeDecl;

public class DiffView extends JPanel {
	private static final long serialVersionUID = -3740617748885779946L;

	interface HandlerProvider {
		Thread createHandler();
	}
	
	public class PullUpMethodData implements HandlerProvider {
		public String typeName;
		public String methodSig;
		public JTextField typeNameField;
		public JTextField methodSigField;

		public PullUpMethodData(String typeName, String methodSig) {
			this.typeName = typeName;
			this.methodSig = methodSig;
		}
		
		public Thread createHandler() {
			return new Thread() {
				public void run() {
					typeName = typeNameField.getText();
					methodSig = methodSigField.getText();
					performRefactoring(new PullUpMethodRefactoring());
				}
			};
		}
	}

	public class ExtractInterfaceData implements HandlerProvider {
		public String typeName;
		public String ifaceName;
		public JTextField typeNameField;
		public JTextField ifaceNameField;

		public ExtractInterfaceData(String typeName, String ifaceName) {
			this.typeName = typeName;
			this.ifaceName = ifaceName;
		}
		
		public Thread createHandler() {
			return new Thread() {
				public void run() {
					typeName = typeNameField.getText();
					ifaceName = ifaceNameField.getText();
					performRefactoring(new ExtractInterfaceRefactoring());
				}
			};			
		}
	}

	private static JFrame frame;
	private JTabbedPane tabbedPane;
	private JLabel status;
	
	private String benchmark = System.getProperty("user.home") 
							+ File.separator + "JastAdd"
							+ File.separator + "benchmarks"
							+ File.separator + "org.apache.commons.codec 1.3"
							+ File.separator + ".classpath";
	private Program prog = null;
	private String prog_is_for_benchmark = null;
	
	private ExtractInterfaceData extractInterfaceData = 
		new ExtractInterfaceData("org.apache.commons.codec.digest.DigestUtils", "p.I");
	
	private PullUpMethodData pullUpMethodData = 
		new PullUpMethodData("org.eclipse.draw2d.parts.Thumbnail", "getScaleX()");
	
	private HandlerProvider handlerProvider;

	public DiffView() {
		setLayout(new BorderLayout());
		
		JPanel controlsPane = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		controlsPane.setLayout(gbl);
		controlsPane.setBorder(BorderFactory.createCompoundBorder(
									BorderFactory.createBevelBorder(BevelBorder.LOWERED),
									BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = 5;
		c.ipady = 5;
		
		// tabbed pane to hold different parameter input panes
		final JTabbedPane parmInputPanes = new JTabbedPane();
		parmInputPanes.add("Extract Interface", createExtractInterfaceInputPane());
		parmInputPanes.add("Pull Up Method", createPullUpMethodInputPane());
		
		handlerProvider = extractInterfaceData;
		parmInputPanes.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				switch(parmInputPanes.getSelectedIndex()) {
				case 0:
					handlerProvider = extractInterfaceData;
					break;
				case 1:
					handlerProvider = pullUpMethodData;
					break;
				}
			}
		});
		
		// add parmInputPane to controlsPane
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        controlsPane.add(parmInputPanes, c);
		
		// create text box for selecting benchmark
        final JTextField benchmarkPath = new JTextField(30);
        benchmarkPath.setText(benchmark);
		JLabel benchboxLabel = new JLabel("Test program:");
		benchboxLabel.setLabelFor(benchmarkPath);
		benchboxLabel.setToolTipText("Enter the path of the .classpath file of a program to run the refactoring on.");
		JButton browseButton = new JButton("Browse...");
		final JFileChooser fc = new JFileChooser();
		fc.setFileHidingEnabled(false);
		fc.addChoosableFileFilter(new FileFilter() {
			public String getDescription() {
				return "Eclipse .classpath files";
			}
			
			public boolean accept(File f) {
				return f.isDirectory() && !f.isHidden() || f.getName().equals(".classpath");
			}
		});
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int status = fc.showOpenDialog(frame);
				if(status == JFileChooser.APPROVE_OPTION)
					benchmarkPath.setText(fc.getSelectedFile().getAbsolutePath());
			}
		});
		
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.weightx = 0.0;
		controlsPane.add(benchboxLabel, c);
		
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		controlsPane.add(benchmarkPath, c);
		
		//c.anchor = GridBagConstraints.EAST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.0;
		controlsPane.add(browseButton, c);
		
		// create "Go" button
		JButton goButton = new JButton("Go!");
		goButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				benchmark = benchmarkPath.getText();
				handlerProvider.createHandler().start();
			}
		});
		
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        controlsPane.add(goButton, c);
		
		// create status bar
		status = new JLabel("Ready");
		status.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
				
        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		
		add(controlsPane, BorderLayout.NORTH);
		add(tabbedPane, BorderLayout.CENTER);
		add(status, BorderLayout.SOUTH);
	}

	private JPanel createExtractInterfaceInputPane() {
		JPanel parmInputPane = new JPanel();
		parmInputPane.setLayout(new GridBagLayout());
		
		// create text field for selecting type
		extractInterfaceData.typeNameField = new JTextField(20);
		extractInterfaceData.typeNameField.setText(extractInterfaceData.typeName);
		addStringInput(parmInputPane, "Class to extract from:", extractInterfaceData.typeNameField, 
						"Enter the fully qualified name of the class to extract from.");
		
		// create text field for new interface name
		extractInterfaceData.ifaceNameField = new JTextField(20);
		extractInterfaceData.ifaceNameField.setText(extractInterfaceData.ifaceName);
		addStringInput(parmInputPane, "Name of new interface:", extractInterfaceData.ifaceNameField,
						"Enter the fully qualified name of the interface to extract into.");
		return parmInputPane;
	}
	
	private JPanel createPullUpMethodInputPane() {
		JPanel parmInputPane = new JPanel();
		parmInputPane.setLayout(new GridBagLayout());
		
		// create text field for selecting type
		pullUpMethodData.typeNameField = new JTextField(20);
		pullUpMethodData.typeNameField.setText(pullUpMethodData.typeName);
		addStringInput(parmInputPane, "Class to pull up from:", pullUpMethodData.typeNameField, 
						"Enter the fully qualified name of the class to pull the method up from.");
		
		// create text field for method signature
		pullUpMethodData.methodSigField = new JTextField(20);
		pullUpMethodData.methodSigField.setText(pullUpMethodData.methodSig);
		addStringInput(parmInputPane, "Method signature:", pullUpMethodData.methodSigField,
						"Enter the signature of the method to extract.");
		return parmInputPane;		
	}
	
	private void addStringInput(JPanel container, String labelText, JTextField textField, String tooltip) {
		JLabel label = new JLabel(labelText);
		label.setLabelFor(textField);
		label.setToolTipText(tooltip);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.ipadx = 5;
		
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		container.add(label, c);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		container.add(textField, c);
	}

	private static void createAndShowGUI() {
		frame = new JFrame("Diff View");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new DiffView());
		frame.pack();
		frame.setVisible(true);
	}
	
	private void performRefactoring(Runnable refactoring) {
		try {
			setBusyCursor();
			Assert.assertNotNull("No benchmark selected.", benchmark);
			if(prog == null || !benchmark.equals(prog_is_for_benchmark)) {
				status("Compiling...");
				prog = CompileHelper.buildProjectFromClassPathFile(new File(benchmark));
				prog_is_for_benchmark = benchmark;
			}
			
			refactoring.run();
			
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
			error(e.getMessage());
			e.printStackTrace();
			prog = null;
		} finally {
			setNormalCursor();
			status("Done");
		}
	}

	private class ExtractInterfaceRefactoring implements Runnable {
		public void run() {
			TypeDecl tp = prog.findType(extractInterfaceData.typeName);
			Assert.assertNotNull("There is no type '" + extractInterfaceData.typeName + "' in " + benchmark + ".", tp);
			Assert.assertTrue("Type '" + extractInterfaceData.typeName + "' is not a class.", tp instanceof ClassDecl);
			int idx = extractInterfaceData.ifaceName.lastIndexOf('.');
			String pkg = idx == -1 ? "" : extractInterfaceData.ifaceName.substring(0, idx);
			String name = extractInterfaceData.ifaceName.substring(idx+1, extractInterfaceData.ifaceName.length());
			Collection<MethodDecl> methods = new LinkedList<MethodDecl>();
			for(Iterator<MethodDecl> iter=tp.localMethodsIterator();iter.hasNext();) {
				MethodDecl meth = iter.next();
				if(!meth.isStatic() && meth.isPublic())
					methods.add(meth);
			}

			// now do the refactoring
			status("Extracting interface...");
			Program.startRecordingASTChangesAndFlush();
			((ClassDecl)tp).doExtractInterface(pkg, name, methods);
		}
	}
	
	private class PullUpMethodRefactoring implements Runnable {
		public void run() {
			TypeDecl tp = prog.findType(pullUpMethodData.typeName);
			Assert.assertNotNull("There is no type '" + pullUpMethodData.typeName + "' in " + benchmark + ".", tp);
			Assert.assertTrue("Type '" + pullUpMethodData.typeName + "' is not a class.", tp instanceof ClassDecl);
			SimpleSet res = tp.localMethodsSignature(pullUpMethodData.methodSig);
			Assert.assertTrue("Class " + pullUpMethodData.typeName + " has no method with signature " + pullUpMethodData.methodSig, res instanceof MethodDecl);
			
			status("Pulling up method...");
			Program.startRecordingASTChangesAndFlush();
			((MethodDecl)res).doPullUpWithRequired();
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
				StyleConstants.setBackground(deleted, Color.lightGray);

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
