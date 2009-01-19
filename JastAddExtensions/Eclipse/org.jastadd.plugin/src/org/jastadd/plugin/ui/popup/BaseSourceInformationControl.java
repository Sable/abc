package org.jastadd.plugin.ui.popup;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class BaseSourceInformationControl implements IInformationControl, IInformationControlExtension, DisposeListener {

	private Shell fShell;
	private int fMaxWidth;
	private int fMaxHeight;
	private StyledText fText;
	private SourceViewer fViewer;
	private Label fStatusField;
	private Label fSeparator;
	private Font fStatusTextFont;
	private static final int BORDER = 1;
	
	public BaseSourceInformationControl(Shell parent) {
		
		GridLayout layout;
		GridData gd;

		int shellStyle = SWT.TOOL | SWT.NO_TRIM | SWT.LEFT_TO_RIGHT;
		
		fShell = new Shell(parent, SWT.NO_FOCUS | SWT.ON_TOP | shellStyle);
		Display display = fShell.getDisplay();
		fShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));

		Composite composite = fShell;
		layout = new GridLayout(1, false);
		int border = ((shellStyle & SWT.NO_TRIM) == 0) ? 0 : BORDER;
		layout.marginHeight = border;
		layout.marginWidth = border;
		composite.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gd);
		
		// Source viewer
		fViewer = new SourceViewer(composite, null, null, false, SWT.NONE);
		fViewer.configure(new SourceInformationControlConfiguration());
		fViewer.setEditable(false);

		fText = fViewer.getTextWidget();
		gd = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
		fText.setLayoutData(gd);
		fText.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		fText.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		
		Font font = JFaceResources.getFont("org.eclipse.jdt.ui.editors.textfont"); //$NON-NLS-1$
		StyledText styledText = fViewer.getTextWidget();
		styledText.setFont(font);

		fText.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e)  {
				if (e.character == 0x1B) // ESC
					fShell.dispose();
			}

			public void keyReleased(KeyEvent e) {}
		});

		addDisposeListener(this);
	}
	
	private class SourceInformationControlConfiguration extends SourceViewerConfiguration {
	    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
			PresentationReconciler reconciler= new PresentationReconciler();
			DefaultDamagerRepairer dr= new DefaultDamagerRepairer(null); // TODO change to something better than null
			reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
			reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
			return reconciler;
		}
	}
	
	public void addDisposeListener(DisposeListener listener) {
		fShell.addDisposeListener(listener);
	}

	public void addFocusListener(FocusListener listener) {
		fText.addFocusListener(listener);
	}

	public Point computeSizeHint() {
		// compute the preferred size
		int x = SWT.DEFAULT;
		int y = SWT.DEFAULT;
		Point size = fShell.computeSize(x, y);
		if (size.x > fMaxWidth)
			x = fMaxWidth;
		if (size.y > fMaxHeight)
			y = fMaxHeight;
		// recompute using the constraints if the preferred size is larger than the constraints
		if (x != SWT.DEFAULT || y != SWT.DEFAULT)
			size = fShell.computeSize(x, y, false);
		return size;
	}

	public void dispose() {
		if (fShell != null && !fShell.isDisposed())
			fShell.dispose();
		else
			widgetDisposed(null);
	}

	public boolean isFocusControl() {
		return fText.isFocusControl();
	}

	public void removeDisposeListener(DisposeListener listener) {
		fShell.removeDisposeListener(listener);
	}

	public void removeFocusListener(FocusListener listener) {
		fText.removeFocusListener(listener);
	}

	public void setBackgroundColor(Color background) {
		fText.setBackground(background);
	}

	public void setFocus() {
		fShell.forceFocus();
		fText.setFocus();
	}

	public void setForegroundColor(Color foreground) {
		fText.setForeground(foreground);
	}

	public void setInformation(String content) {
		if (content == null) {
			fViewer.setInput(null);
			return;
		}
		IDocument doc = new Document(content);
		fViewer.setInput(doc);
	}

	public void setLocation(Point location) {
		fShell.setLocation(location);
	}

	public void setSize(int width, int height) {
		if (fStatusField != null) {
			GridData gd= (GridData)fViewer.getTextWidget().getLayoutData();
			Point statusSize= fStatusField.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			Point separatorSize= fSeparator.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			gd.heightHint= height - statusSize.y - separatorSize.y;
		}
		fShell.setSize(width, height);
		if (fStatusField != null)
			fShell.pack(true);
	}

	public void setSizeConstraints(int maxWidth, int maxHeight) {
		fMaxWidth= maxWidth;
		fMaxHeight= maxHeight;
	}

	public void setVisible(boolean visible) {
		fShell.setVisible(visible);	
	}

	public boolean hasContents() {
		return fText.getCharCount() > 0;
	}

	public void widgetDisposed(DisposeEvent e) {
		if (fStatusTextFont != null && !fStatusTextFont.isDisposed())
			fStatusTextFont.dispose();

		fStatusTextFont= null;
		fShell = null;
		fText = null;
	}
}
