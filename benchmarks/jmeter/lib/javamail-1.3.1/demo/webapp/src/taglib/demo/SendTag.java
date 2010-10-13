/*
 * @(#)SendTag.java	1.3 02/04/04
 *
 * Copyright 2001-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND
 * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES
 * SUFFERED BY LICENSEE AS A RESULT OF  OR RELATING TO USE, MODIFICATION
 * OR DISTRIBUTION OF THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL
 * SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 *
 */

package demo;

import java.util.*;
import java.net.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

/**
 * Custom tag for sending messages.
 */
public class SendTag extends BodyTagSupport {
    private String body;
    private String cc;
    private String host;
    private String recipients;
    private String sender;
    private String subject;

    /**
     * host attribute setter method.
     */
    public void setHost(String host) {
        this.host = host;
    }
    
    /**
     * recipient attribute setter method.
     */
    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    /**
     * sender attribute setter method.
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * cc attribute setter method.
     */
    public void setCc(String cc) {
        this.cc = cc;
    }

    /**
     * subject attribute setter method.
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Method for processing the end of the tag.
     */
    public int doEndTag() throws JspException {
        Properties props = System.getProperties();
        
        try {
            if (host != null)
                props.put("mail.smtp.host", host);
            else if (props.getProperty("mail.smtp.host") == null)
                props.put("mail.smtp.host", InetAddress.getLocalHost().
                    getHostName());
        } catch (Exception ex) {
            throw new JspException(ex.getMessage());
        }
        Session session = Session.getDefaultInstance(props, null);
		
	Message msg = new MimeMessage(session);
	InternetAddress[] toAddrs = null, ccAddrs = null;

        try {
	    if (recipients != null) {
	        toAddrs = InternetAddress.parse(recipients, false);
	        msg.setRecipients(Message.RecipientType.TO, toAddrs);
	    } else
	        throw new JspException("No recipient address specified");

            if (sender != null)
                msg.setFrom(new InternetAddress(sender));
            else
                throw new JspException("No sender address specified");

	    if (cc != null) {
                ccAddrs = InternetAddress.parse(cc, false);
	        msg.setRecipients(Message.RecipientType.CC, ccAddrs);
	    }

	    if (subject != null)
	        msg.setSubject(subject);

	    if ((body = getBodyContent().getString()) != null)
	        msg.setText(body);
            else
                msg.setText("");

            Transport.send(msg);
	
        } catch (Exception ex) {
            throw new JspException(ex.getMessage());
        }

        return(EVAL_PAGE);
   }
}

