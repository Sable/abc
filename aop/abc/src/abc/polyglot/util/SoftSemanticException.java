package abc.polyglot.util;

import polyglot.types.SemanticException;

/** An unchecked version of SemanticException for use
 *  when there really is no choice (e.g. when you need
 *  to implement an interface that doesn't support the 
 *  checked version. Unwrap into the checked version as
 *  quickly as possible.
 *  @author Ganesh Sittampalam
 */

public class SoftSemanticException extends RuntimeException {
    public SoftSemanticException(SemanticException e) {
	super(e);
    }

    public SemanticException unwrap() {
	return (SemanticException) getCause();
    }
}