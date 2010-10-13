<%--
 % @(#)messagecontent.jsp	1.4 02/04/04
 %
 % Copyright 2001-2002 Sun Microsystems, Inc. All Rights Reserved.
 %
 % Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 % modify and redistribute this software in source and binary code form,
 % provided that i) this copyright notice and license appear on all copies of
 % the software; and ii) Licensee does not utilize the software in a manner
 % which is disparaging to Sun.
 %
 % This software is provided "AS IS," without a warranty of any kind. ALL
 % EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 % IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 % NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 % LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 % OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 % LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 % INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 % CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 % OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 % POSSIBILITY OF SUCH DAMAGES.
 %
 % This software is not designed or intended for use in on-line control of
 % aircraft, air traffic, aircraft navigation or aircraft communications; or in
 % the design, construction, operation or maintenance of any nuclear
 % facility. Licensee represents and warrants that it will not use or
 % redistribute the Software for such purposes.
 %
--%>

<%@ page language="java" import="demo.MessageInfo, demo.AttachmentInfo" %>
<%@ page errorPage="errorpage.jsp" %>
<%@ taglib uri="http://java.sun.com/products/javamail/demo/webapp" 
    prefix="javamail" %>
 

<html>
<head>
    <title>JavaMail messagecontent</title>
</head>

<javamail:message 
id="msginfo"
folder="folder"
num="<%= request.getParameter(\"message\") %>" 
/>

<body bgcolor="#ccccff">
<center><font face="Arial,Helvetica" font size="+3">
<b>Message<sp> 
<sp>in folder /INBOX</b></font></center><p>

<%-- first, display this message's headers --%>
<b>Date:</b>
<%= msginfo.getSentDate() %>
<br>
<% if (msginfo.hasFrom()) { %>
<b>From:</b>
<a href="compose?to=<%= msginfo.getReplyTo() %>"
    target="reply<%= msginfo.getNum() %>">
<%= msginfo.getFrom() %>
</a>
<br>
<% } %>

<% if (msginfo.hasTo()) { %>
<b>To:</b>
<%= msginfo.getTo() %>
<br>
<% } %>   

<% if (msginfo.hasCc()) { %>
<b>CC:</b>
<%= msginfo.getCc() %>
<br>
<% } %>

<b>Subject:</b>
<% if (msginfo.hasSubject()) { %>
<%= msginfo.getSubject() %>
<% } %>
<br>
<pre>
<%= msginfo.getBody() %>
</pre>
<% if (msginfo.hasAttachments()) { %>
<javamail:listattachments
 id="attachment"
 messageinfo="msginfo">
<p><hr>
<b>Attachment Type:</b>
<%= attachment.getAttachmentType() %>
<br>
<% if (attachment.hasMimeType("text/plain") && 
       attachment.isInline()){ %>
<pre>
<%= attachment.getContent() %>
</pre>
<% } else { %>
<b>Filename:</b>
<%= attachment.getFilename() %>
<br>
<b>Description:</b>
<%= attachment.getDescription() %>
<br>
<a href="attachment?message=
<%= msginfo.getNum() %>&part=<%= attachment.getNum() %>">
Display Attachment</a>
<% } %>
</javamail:listattachments>
<% } %>
</body>
</html>

