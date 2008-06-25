package abc.ja.om;

//for errors that have to halt the traversals (e.g. module ciruclarity causes precedence
//generation to cause a stack overflow)
public class OMUnrecoverableSemanticError extends RuntimeException {
	public OMUnrecoverableSemanticError(String msg) {
		super(msg);
	}
}
