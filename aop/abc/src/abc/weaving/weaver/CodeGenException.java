package abc.weaving.weaver;


/** A runtime exception to throw a codegen error that we really did not
 * expect to happen.  Used in debugging,  should never be thrown in the
 * completed code generator.
 *
 *   @author Laurie Hendren
 *   @date 03-May-04
 */
public class CodeGenException extends RuntimeException {

  CodeGenException(String message)
    { super("CODE GENERATOR EXCEPTION: " + message+ "\n" +
	    "This exception should not occur and is the result of \n" +
	    "incomplete or incorrect code generation.");
    }
}
