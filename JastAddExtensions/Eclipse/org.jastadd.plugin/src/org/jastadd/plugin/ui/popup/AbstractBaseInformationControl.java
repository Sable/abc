/**
 * 
 */
package org.jastadd.plugin.ui.popup;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractBaseInformationControl extends
		PopupDialog implements IInformationControl,
		IInformationControlExtension2 {

	public AbstractBaseInformationControl(Shell parent, int shellStyle,
			boolean takeFocusOnOpen, boolean persistSize, boolean persistLocation,
			boolean showDialogMenu, boolean showPersistAction,
			String titleText, String infoText) {
		super(parent, shellStyle, takeFocusOnOpen, persistSize, persistLocation,
				showDialogMenu, showPersistAction, titleText, infoText);
	}

	public void setInformation(String information) {
		// Ignore
	}

	public void setSizeConstraints(int maxWidth, int maxHeight) {
		// ignore
	}

	public Point computeSizeHint() {
		return getShell().getSize();
	}

	public void setVisible(boolean visible) {
		if (visible) {
			open();
		} else {
			getShell().setVisible(false);
		}
	}

	public void setSize(int width, int height) {
		getShell().setSize(width, height);
	}

	public void setLocation(Point location) {
		getShell().setLocation(location);
	}

	public void dispose() {
		close();
	}

	public void setForegroundColor(Color foreground) {
		applyForegroundColor(foreground, getContents());
	}

	public void setBackgroundColor(Color background) {
		applyBackgroundColor(background, getContents());
	}

	public void setFocus() {
		getShell().forceFocus();
	}

	public void addFocusListener(FocusListener listener) {
		getShell().addFocusListener(listener);
	}

	public void removeFocusListener(FocusListener listener) {
		getShell().removeFocusListener(listener);
	}

	public void addDisposeListener(DisposeListener listener) {
		getShell().addDisposeListener(listener);
	}

	public void removeDisposeListener(DisposeListener listener) {
		getShell().removeDisposeListener(listener);
	}
}