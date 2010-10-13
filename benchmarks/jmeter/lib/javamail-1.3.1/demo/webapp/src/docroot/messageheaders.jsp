<%--
 % @(#)messageheaders.jsp	1.4 02/04/04
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

<%@ page language="java" import="demo.MessageInfo" %>
<%@ page errorPage="errorpage.jsp" %>
<%@ taglib uri="http://java.sun.com/products/javamail/demo/webapp" 
    prefix="javamail" %>

<html>
<head>
	<title>JavaMail messageheaders</title>
</head>

<body bgcolor="#ccccff"><hr>
    
<center><font face="Arial,Helvetica" font size="+3">
<b>Folder INBOX</b></font></center><p>
   
<font face="Arial,Helvetica" font size="+3">
<b><a href="logout">Logout</a>
<a href="compose" target="compose">Compose</a>
</b></font>
<hr>
    
<table cellpadding=1 cellspacing=1 width="100%" border=1>
<tr>
<td width="25%" bgcolor="ffffcc">
<font face="Arial,Helvetica" font size="+1">
<b>Sender</b></font></td>
<td width="15%" bgcolor="ffffcc">
<font face="Arial,Helvetica" font size="+1">
<b>Date</b></font></td>
<td bgcolor="ffffcc">
<font face="Arial,Helvetica" font size="+1">
<b>Subject</b></font></td>
</tr>
<javamail:listmessages
 id="msginfo"
 folder="folder">
<%-- from --%>
<tr valign=middle>
<td width="25%" bgcolor="ffffff">
<font face="Arial,Helvetica">
<% if (msginfo.hasFrom()) { %>
<%= msginfo.getFrom() %>
</font>
<% } else { %>
<font face="Arial,Helvetica,sans-serif">
Unknown
<% } %>
</font></td>
<%-- date --%>
<td nowrap width="15%" bgcolor="ffffff">
<font face="Arial,Helvetica">
<%= msginfo.getDate() %>
</font></td>
<%-- subject & link --%>
<td bgcolor="ffffff">
<font face="Arial,Helvetica">
<a href="messagecontent?message=<%= msginfo.getNum() %>">
<% if (msginfo.hasSubject()) { %>
<%=    msginfo.getSubject() %>
<% } else { %>
<i>No Subject</i>
<% } %>
</a>
</font></td>
</tr>
</javamail:listmessages>
</table>
</body>
</html>

