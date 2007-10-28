package org.jastadd.plugin.jastaddj.builder.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;

class UIUtil {

	static void stretchControlHorizontal(final Control control) {
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		control.setLayoutData(gridData);
	}
	
	static void stretchControl(Control control) {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		control.setLayoutData(gridData);
	}
}
