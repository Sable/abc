<%@ taglib prefix="c" uri="http://jakarta.apache.org/tomcat/example-taglib" %>
<%@ attribute name="str" fragment="true"%>
${st<caret>r.AAA}
<jsp:invoke fragment="str"/>
<c:aaa name="${str}"/>