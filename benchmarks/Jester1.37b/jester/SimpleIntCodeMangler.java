package jester;

public class SimpleIntCodeMangler extends SimpleCodeMangler {
	public SimpleIntCodeMangler(ClassSourceCodeChanger sourceCodeSystem) {
		super(sourceCodeSystem);
	}
	static int charAsInt(char c) {
		return c - '0';
	}
	String incremented(int i) {
		if (i == 9) {
			return "0";
		}
		return String.valueOf(i + 1);
	}
	int indexOfNumber(IgnoreListDocument s) {
		int startIndex = getIndexOfLastChange() == -1 ? 0 : getIndexOfLastChange() + 1;
		for (int index = startIndex; index < s.length(); index++) {
			char characterToCheck = s.charAt(index);
			if (Character.isDigit(characterToCheck)){
				if(index > 0){
					char precedingCharacter = s.charAt(index - 1);
					boolean ignoreThisBecauseItIsEitherWrongToMutateOrTheNumberHasAlreadyBeenMutated = Character.isLetterOrDigit(precedingCharacter);
					if(ignoreThisBecauseItIsEitherWrongToMutateOrTheNumberHasAlreadyBeenMutated){
						continue;
					}
				}
				return index;
			}
		}
		return -1;
	}
	boolean makeSomeChangeToFileSource() throws SourceChangeException {
		int index = indexOfNumber(getOriginalContents());
		if (index == -1) {
			return false;
		}

		char valueCharacterChangedFrom = getOriginalContents().charAt(index);
		int originalNumber = charAsInt(valueCharacterChangedFrom);
		String valueCharacterChangedTo = incremented(originalNumber);

		return makeChange(index, String.valueOf(valueCharacterChangedFrom), valueCharacterChangedTo);
	}
}