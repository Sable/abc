package org.jastadd.plugin.jastaddj.builder.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;

public class UIUtil {

	public static GridData stretchControlHorizontal(GridData gridData) {
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		return gridData;
	}
	
	public static GridData stretchControl(GridData gridData) {
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		return gridData;
	}

	public static GridData suggestCharWidth(GridData gridData, Control control, int charWidth) {
		GC gc = new GC(control);
		gc.setFont(control.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		gridData.widthHint = fontMetrics.getAverageCharWidth() * charWidth;
		return gridData;
	}
	
	public static GridData suggestCharSize(GridData gridData, Control control, int charWidth, int charHeight) {
		GC gc = new GC(control);
		gc.setFont(control.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		gridData.widthHint = fontMetrics.getAverageCharWidth() * charWidth;
		gridData.heightHint = fontMetrics.getHeight() * charHeight;
		return gridData;
	}
	
}
