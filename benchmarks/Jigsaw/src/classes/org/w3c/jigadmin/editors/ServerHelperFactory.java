// ServerHelperFactory.java
// $Id: ServerHelperFactory.java,v 1.4 2000/08/16 21:37:31 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

import java.util.Properties;

import org.w3c.jigadmin.PropertyManager;
import org.w3c.jigadmin.RemoteResourceWrapper;

/**
 * The server helper factory.
 * @version $Revision: 1.4 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ServerHelperFactory {

    public final static String SERVER_HELPER_P = "shelper";

    /**
     * Get the server helper associated to the given sever name
     * @param name the name of the server
     * @param rrw the RemoteResourceWrapper of the server
     * @return a ServerHelperInterface instance
     */
    public static 
	ServerHelperInterface getServerHelper(String name, 
					      RemoteResourceWrapper rrw) 
    {
	PropertyManager pm = PropertyManager.getPropertyManager();

	Properties props = pm.getEditorProperties(rrw);
	String editorClass = (String) props.get(SERVER_HELPER_P);

	if (editorClass == null)
	    return null;

	ServerHelperInterface helper = null;

	try {
	    Class  c = Class.forName(editorClass);
	    Object o = c.newInstance();
	    if (o instanceof ServerHelperInterface) {
		helper = (ServerHelperInterface) o;
		helper.initialize(name, rrw, props);
	    } else {
		throw new RuntimeException(editorClass+" doesn't "+
				   "implements ServerHelperInterface.");
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException("cannot create server helper: "+
				       editorClass+" for \""+name);
	}
	return helper;
    }
}
