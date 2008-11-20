<jsp:useBean id="myBean2" class="MyClass"/>
<jsp:getProperty name="myBean2" property="aAA"/>
${myBean2}
<%
  out.println(my<caret>Bean2);
%>