package abc.soot.util;

import java.util.List;
import soot.Body;
import soot.jimple.Stmt;
import soot.tagkit.Tag;
import soot.tagkit.AttributeValueException;

/** 
 *  @author Ganesh Sittampalam
 */

public class RedirectedExceptionSpecTag implements Tag {
    public final static String name="RedirectedExceptionSpecTag";
    
    public String getName() {
	return name;
    }

    public byte[] getValue() {
	throw new AttributeValueException();
    }

    public RedirectedExceptionSpecTag(Body body,List/*<Stmt>*/ stmts) {
	this.body=body;
	this.stmts=stmts;
    }

    /** The body containing the replacement statements */
    public Body body;

    /** The statements whose exception specifications should be checked 
	in place of the existing one */
    public List/*<Stmt>*/ stmts;
}
