package abc.soot.util;

import java.util.List;
import soot.Body;
import soot.jimple.Stmt;
import soot.tagkit.Tag;
import soot.tagkit.AttributeValueException;

/** 
 *  @author Ganesh Sittampalam
 */

public class DisableExceptionCheckTag implements Tag {
    public final static String name="DisableExceptionCheckTag";
    
    public String getName() {
	return name;
    }

    public byte[] getValue() {
	throw new AttributeValueException();
    }
}
