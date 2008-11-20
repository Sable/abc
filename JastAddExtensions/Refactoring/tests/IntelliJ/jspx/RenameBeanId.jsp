<jsp:useBean id="myBean" class="MyClass"/>
<jsp:getProperty name="myBean" property="aAA"/>
${myBean}
<%
  out.println(my<caret>Bean);
%>