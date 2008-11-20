<%@ taglib prefix="c" uri="http://jakarta.apache.org/tomcat/example-taglib" %>
<%@ attribute name="aaa" fragment="true"%>
${aa<caret>a.AAA}
<jsp:invoke fragment="aaa"/>
<c:aaa name="${aaa}"/>