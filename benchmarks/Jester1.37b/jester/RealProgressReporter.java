package jester;

import java.awt.Color;

public class RealProgressReporter implements ProgressReporter {
	private RealProgressReporterUI ui = new RealProgressReporterUI();
	private final Configuration myConfiguration;

	public RealProgressReporter(Configuration config) {
	  myConfiguration = config;
	}
	
	public void setMaximum(int numberOfFilesThatWillBeTested) {
		ui.fProgressBar.setMaximum(numberOfFilesThatWillBeTested);
	}

	public void progress() {
		ui.fProgressBar.setValue(ui.fProgressBar.getValue() + 1);
		
		if (myConfiguration.closeUIOnFinish() && ui.fProgressBar.getMaximum() == ui.fProgressBar.getValue()) {
			ui.dispose();
		}
	}

	public void setColor(Color aColor) {
		ui.fProgressBar.setForeground(aColor);
	}

	public void setText(String text) {
		ui.fTextArea.setText(text);
	}

}
