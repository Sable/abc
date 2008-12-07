package jastaddmodules.translator;

public class BundleTranslationException extends Exception {
	private static final long serialVersionUID = 4651156362980747087L;
	
	public BundleTranslationException(String message) {
		super(message);
	}
	public BundleTranslationException(String message, Throwable cause) {
		super(message, cause);
	}
}
