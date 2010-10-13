/*
 * @(#)ListMessagesTag.java	1.4 02/04/04
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

import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.search.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

/**
 * Custom tag for listing messages. The scripting variable is only
 * within the body of the tag.
 */
public class ListMessagesTag extends BodyTagSupport {
    private String folder;
    private String session;
    private int msgNum = 0;
    private int messageCount = 0;
    private Message message;
    private Message[] messages;
    private MessageInfo messageinfo;
    
    /**
     * folder attribute getter method.
     */
    public String getFolder() {
        return folder;
    }
    
    /**
     * session attribute getter method.
     */
    public String getSession() {
        return session;
    }
    
    /**
     * folder setter method.
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * session attribute setter method.
     */
    public void setSession(String session) {
        this.session = session;
    }

    /**
     * Method for processing the start of the tag.
     */
    public int doStartTag() throws JspException {
        messageinfo = new MessageInfo();
       
        try {
            Folder folder = (Folder)pageContext.getAttribute(
		getFolder(), PageContext.SESSION_SCOPE);
            FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.DELETED), false);
            messages = folder.search(ft);
            messageCount = messages.length;
        } catch (Exception ex) {
            throw new JspException(ex.getMessage());
        }

        if (messageCount > 0) {
            getMessage();
            return BodyTag.EVAL_BODY_TAG;
        } else
            return BodyTag.SKIP_BODY;
    }
   
    /**
     * Method for processing the body content of the tag.
     */
    public int doAfterBody() throws JspException {
        
        BodyContent body = getBodyContent();
        try {
            body.writeOut(getPreviousOut());
        } catch (IOException e) {
            throw new JspTagException("IterationTag: " + e.getMessage());
        }
        
        // clear up so the next time the body content is empty
        body.clearBody();
       
        if (msgNum < messageCount) {
            getMessage();
            return BodyTag.EVAL_BODY_TAG;
        } else {
            return BodyTag.SKIP_BODY;
        }
    }
    
    /**
     * Helper method for retrieving messages.
     */
    private void getMessage() throws JspException {
        message = messages[msgNum++];
        messageinfo.setMessage(message);
        pageContext.setAttribute(getId(), messageinfo);
    }
}

