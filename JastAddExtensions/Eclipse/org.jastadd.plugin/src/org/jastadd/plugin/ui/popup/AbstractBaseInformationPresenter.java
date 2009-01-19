/**
 * 
 */
package org.jastadd.plugin.ui.popup;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.information.InformationPresenter;

public abstract class AbstractBaseInformationPresenter extends
		InformationPresenter {
	public AbstractBaseInformationPresenter(IInformationControlCreator creator) {
		super(creator);
		setInformationProviders();
	}

	/*
	public void run(JastAddEditor editor) {
		editor.installInformationPresenter(this);
		showInformation();
	}
	*/

	protected abstract void setInformationProviders();

	protected void hideInformationControl() {
		super.hideInformationControl();
		uninstall();
	}
}