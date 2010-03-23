package p;

import java.util.ArrayList;

public class ControlBodyUpdates {
	ControlBodyUpdatesParameter parameterObject = new ControlBodyUpdatesParameter();

	private void foo() {
		for (int i = 0; i < parameterObject.getArraylists().get(0); i++)
			parameterObject.setArraylists(null);
		for (int intValue : parameterObject.getInts())
			parameterObject.setArraylists(new ArrayList(parameterObject.getArraylists().size()));
		while (parameterObject.getInts().length != 0)
			parameterObject.setArraylists(null);
		if (parameterObject.getArraylists() == null)
			parameterObject.setArraylists(null);
		int[] newRange= new int[8];
		for (int i = 0; i < parameterObject.getArraylists().get(0); i++) {
			parameterObject.setArraylists(null);
		}
		for (int intValue : parameterObject.getInts()) {
			parameterObject.setArraylists(new ArrayList(parameterObject.getArraylists().size()));
		}
		while (parameterObject.getInts().length != 0) {
			parameterObject.setArraylists(null);
		}
		if (parameterObject.getArraylists() == null) {
			parameterObject.setArraylists(null);
		}
		if (newRange[0] > 0)
			parameterObject.setFNewStart(newRange[0]-1);	// line number start at 0!
		else
			parameterObject.setFNewStart(0);
		if (newRange[0] > 0) {
			parameterObject.setFNewStart(newRange[0]-1);	// line number start at 0!
		} else {
			parameterObject.setFNewStart(0);
		}
	}
}