/**
 * 
 */
package org.jastadd.plugin.information;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.information.InformationPresenter;
import org.jastadd.plugin.editor.JastAddEditor;

public abstract class JastAddInformationPresenter extends
		InformationPresenter {
	public JastAddInformationPresenter(IInformationControlCreator creator) {
		super(creator);
		setInformationProviders();
	}

	public void run(JastAddEditor editor) {
		editor.installInformationPresenter(this);
		showInformation();
	}

	protected abstract void setInformationProviders();

	protected void hideInformationControl() {
		super.hideInformationControl();
		uninstall();
	}
}