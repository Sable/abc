package jester;

public abstract class SimpleCodeMangler implements CodeMangler {
	private ClassSourceCodeChanger sourceCodeSystem;
	//
	private int indexOfLastChange = -1;
	public SimpleCodeMangler(ClassSourceCodeChanger sourceCodeSystem) {
		super();
		this.sourceCodeSystem = sourceCodeSystem;
	}
	int getIndexOfLastChange() {
		return indexOfLastChange;
	}
	IgnoreListDocument getOriginalContents() throws ConfigurationException {
		return sourceCodeSystem.getOriginalContents();
	}
	boolean makeChange(int index, String valueChangedFrom, String valueChangedTo) throws SourceChangeException {
		try {
			sourceCodeSystem.writeOverSourceReplacing(index, valueChangedFrom, valueChangedTo);
			indexOfLastChange = index;
			return true;
		} catch (SourceChangeException ex) {
			indexOfLastChange = index;
			return makeSomeChangeToFileSource();
		}
	}
	public boolean makeChangeToClass() throws SourceChangeException {
		boolean couldMakeChange = makeSomeChangeToFileSource();
		boolean everMadeAChange = indexOfLastChange != -1;
		if (!couldMakeChange && everMadeAChange) {
			sourceCodeSystem.writeOriginalContentsBack();
		}
		return couldMakeChange;
	}
	abstract boolean makeSomeChangeToFileSource() throws SourceChangeException;
}