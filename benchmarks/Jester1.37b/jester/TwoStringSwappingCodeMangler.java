package jester;

public class TwoStringSwappingCodeMangler extends SimpleCodeMangler {
	private String originalString, replacementString;
	
	public TwoStringSwappingCodeMangler(ClassSourceCodeChanger sourceCodeSystem, String originalString, String replacementString) {
		super(sourceCodeSystem);
		this.originalString = originalString;
		this.replacementString = replacementString;
	}
	
	boolean makeSomeChangeToFileSource() throws SourceChangeException {
		IgnoreListDocument s = getOriginalContents();
		int index = s.indexOf(originalString, getIndexOfLastChange() + 1);
		boolean found = index != -1;

		if (!found) {
			return false;
		}

		return makeChange(index, originalString, replacementString);
	}
}