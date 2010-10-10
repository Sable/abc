// XMLDescrWriter.java
// $Id: XMLDescrWriter.java,v 1.6 2002/06/12 09:40:12 ylafon Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.resources.serialization.xml;

import java.io.Writer;
import java.io.Reader;
import java.io.IOException;

import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.SimpleAttribute;
import org.w3c.tools.resources.ArrayAttribute;
import org.w3c.tools.resources.FrameArrayAttribute;
import org.w3c.tools.resources.serialization.AttributeDescription;
import org.w3c.tools.resources.serialization.ResourceDescription;
import org.w3c.tools.resources.serialization.EmptyDescription;

/**
 * @version $Revision: 1.6 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class XMLDescrWriter extends XMLWriter implements JigXML {

    protected void startResource(ResourceDescription res) 
	throws IOException
    {
	String classname = res.getClassName();
	writer.write("<"+RESOURCE_TAG+" "+CLASS_ATTR+"='"+
		     classname+"'>\n");
	writeInherit(res.getClassHierarchy(), 1);
	writeInterfaces(res.getInterfaces());
    }

    protected void startDescription(ResourceDescription res) 
	throws IOException
    {
	String classname = res.getClassName();
	writer.write("<"+DESCR_TAG+" "+CLASS_ATTR+"='");
	writer.write(classname);
	String id = res.getIdentifier();
	if (id == null)
	    id = NULL;
	writer.write("' "+NAME_ATTR+"='");
	writer.write(id);
	writer.write("'>\n");
    }

    protected void closeDescription() 
	throws IOException
    {
	writer.write("</"+DESCR_TAG+">\n");
    }

    protected void writeInherit(String classes[], int idx)
	throws IOException
    {
	if (idx < classes.length) {
	    level++;
	    indent();
	    writer.write("<"+INHERIT_TAG+" "+CLASS_ATTR+"='");
	    writer.write(classes[idx]);
	    writer.write("'>\n");
	    writeInherit(classes, ++idx);
	    indent();
	    writer.write("</"+INHERIT_TAG+">\n");
	    level--;
	}
    }

    protected void writeInterfaces(String classes[]) 
	throws IOException
    {
	level++;
	for (int i = 0 ; i < classes.length ; i++) {
	    indent();
	    writer.write("<"+IMPLEMENTS_TAG+" "+CLASS_ATTR+"='");
	    writer.write(classes[i]);
	    writer.write("'/>\n");
	}
	level--;
    }

    protected void writeAttributeDescription(AttributeDescription descr)
	throws IOException
    {
	level++;
	String    classname = descr.getClassName();
	Attribute attr      = descr.getAttribute();
	Object    value     = descr.getValue();
	if (attr instanceof SimpleAttribute) {
	    indent();
	    writer.write("<"+ATTRIBUTE_TAG+" "+NAME_ATTR+"='");
	    writer.write(descr.getName());
	    writer.write("' "+FLAG_ATTR+"='");
	    writer.write(attr.getFlag());
	    writer.write("' "+CLASS_ATTR+"='");
	    writer.write(classname);
	    if (value == null) {
		writer.write("'>"+NULL);
	    } else {
		writer.write("'>");
		writer.write(encode(((SimpleAttribute)attr).pickle(value)));
	    }
	    writer.write("</"+ATTRIBUTE_TAG+">\n");
	} else if (attr instanceof ArrayAttribute) {
	    indent(); 
	    writer.write("<"+ARRAY_TAG+" "+NAME_ATTR+"='");
	    writer.write(descr.getName());
	    writer.write("' "+FLAG_ATTR+"='");
	    writer.write(attr.getFlag());
	    writer.write("' "+CLASS_ATTR+"='");
	    writer.write(classname);
	    writer.write("' ");
	    String values[] = null;
	    if (value == null)
		values = new String[0];
	    else
		values = ((ArrayAttribute)attr).pickle(value);
	    int len = values.length;
	    writer.write(""+LENGTH_ATTR+"='");
	    writer.write(String.valueOf(len));
	    writer.write("'>\n");
	    level++;
	    for (int i = 0 ; i < len ; i++) {
		indent(); 
		writer.write("<"+VALUE_TAG+">");
		writer.write(encode(values[i]));
		writer.write("</"+VALUE_TAG+">\n");
	    }
	    level--;
	    indent(); 
	    writer.write("</"+ARRAY_TAG+">\n");
	} else if (attr instanceof FrameArrayAttribute) {
	    indent();
	    writer.write("<"+RESARRAY_TAG+" "+NAME_ATTR+
			 "='"+descr.getName()+"' ");
	    writer.write(""+CLASS_ATTR+"='"+classname+"' ");
	    ResourceDescription frames[] = null;
	    if (value == null)
		frames = new ResourceDescription[0];
	    else
		frames = (ResourceDescription[]) value;
	    int len = frames.length;
	    writer.write(""+LENGTH_ATTR+"='");
	    writer.write(String.valueOf(len));
	    writer.write("'>\n");
	    for (int i = 0 ; i < len ; i++) {
		writeResourceDescription(frames[i]);
	    }
	    indent();
	    writer.write("</"+RESARRAY_TAG+">\n");
	}
	level--;
    }

    public void writeResourceDescription(ResourceDescription resource) 
	throws IOException
    {
	level++;
	indent();
	if (resource instanceof EmptyDescription) {
	    startDescription(resource);
	    indent();
	    closeDescription();
	} else {
	    startResource(resource);
	    AttributeDescription attrs [] = 
		resource.getAttributeDescriptions();
	    for (int j = 0 ; j < attrs.length ; j++)
		writeAttributeDescription(attrs[j]);
	    indent();
	    closeResource();
	}
	level--;
    }

    public XMLDescrWriter(Writer writer) {
	super(writer);
    }

}
