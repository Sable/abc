// LanguageAttributeModifier.java
// $Id: LanguageAttributeModifier.java,v 1.2 2000/08/16 21:37:27 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class LanguageAttributeModifier implements EditorModifier {

    /**
     * Modify the selected item, returns the 2 first characters
     * @return The modifier item
     */

    public String modify(String item) {
	return item.substring(0,2);
    }

    /**
     * Create a new LanguageAttributeModifier
     */
    public LanguageAttributeModifier() {

    }    
}
