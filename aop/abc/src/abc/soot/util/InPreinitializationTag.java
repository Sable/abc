package abc.soot.util;

import soot.tagkit.Tag;
import soot.tagkit.AttributeValueException;

public class InPreinitializationTag implements Tag {
    public final static String name="inpreinitialization";
    
    public String getName() {
	return name;
    }

    public byte[] getValue() {
	throw new AttributeValueException();
    }
}
