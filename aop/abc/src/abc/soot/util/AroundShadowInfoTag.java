/*
 * Created on 01-Nov-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.soot.util;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/**
 * @author kuzins
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AroundShadowInfoTag implements Tag {
	public final static String name="AroundShadowInfoTag";
    
    public String getName() {
	return name;
    }

    public byte[] getValue() {
	throw new AttributeValueException();
    }
    public AroundShadowInfoTag(int shadowSize) {
    	this.shadowSize=shadowSize;
    }
    public final int shadowSize;
}
