/*
 * @(#)ListAttachmentsTag.java	1.3 02/04/04
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
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

/**
 * Custom tag for listing message attachments. The scripting variable is only
 * within the body of the tag.
 */
public class ListAttachmentsTag extends BodyTagSupport {
    private String messageinfo;
    private int partNum = 1;
    private int numParts = 0;
    private AttachmentInfo attachmentinfo;
    private MessageInfo messageInfo;
    private Multipart multipart;

    /**
     * messageinfo attribute getter method.
     */
    public String getMessageinfo() {
        return messageinfo;
    }
    
    /**
     * messageinfo attribute setter method.
     */
    public void setMessageinfo(String messageinfo) {
        this.messageinfo = messageinfo;
    }

    /**
     * Method for processing the start of the tag.
     */
    public int doStartTag() throws JspException {
        messageInfo = (MessageInfo)pageContext.getAttribute(getMessageinfo());
        attachmentinfo = new AttachmentInfo();
        
        try {
            multipart = (Multipart)messageInfo.getMessage().getContent();
            numParts = multipart.getCount();
        } catch (Exception ex) {
            throw new JspException(ex.getMessage());
        }

        getPart();

        return BodyTag.EVAL_BODY_TAG;
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
       
        partNum++;
        if (partNum < numParts) {
            getPart();
            return BodyTag.EVAL_BODY_TAG;
        } else {
            return BodyTag.SKIP_BODY;
        }
    }
    
    /**
     * Helper method for retrieving message parts.
     */
    private void getPart() throws JspException {
        try {
            attachmentinfo.setPart(partNum, multipart.getBodyPart(partNum));
            pageContext.setAttribute(getId(), attachmentinfo);
        } catch (Exception ex) {
            throw new JspException(ex.getMessage());
        }
    }
}

