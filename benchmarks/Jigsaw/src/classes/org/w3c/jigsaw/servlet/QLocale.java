// QLocale.java
// $Id: QLocale.java,v 1.3 2000/08/16 21:37:45 ylafon Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigsaw.servlet;

import java.util.Locale;

/**
 * @version $Revision: 1.3 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class QLocale {

    protected double quality;
    protected Locale locale;

    public double getLanguageQuality() {
	return quality;
    }

    public Locale getLocale() {
	return locale;
    }

    /**
     * Construct a locale from language, country and quality.
     */
    public QLocale(String language, String country, double quality) {
	this.locale  = new Locale(language, country);
	this.quality = quality;
    }

}
