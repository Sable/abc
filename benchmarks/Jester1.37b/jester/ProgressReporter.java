package jester;

import java.awt.Color;

public interface ProgressReporter {
	void setMaximum(int numberOfFilesThatWillBeTested);
	void progress();
	void setColor(Color aColor);
	void setText(String text);
}
