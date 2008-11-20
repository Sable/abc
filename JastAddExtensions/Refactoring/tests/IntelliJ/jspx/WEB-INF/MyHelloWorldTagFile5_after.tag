<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

 <c:if test="${feed_guestAuth}">
   <c:set var="feed_ad<caret>minNote2" value="aaa"/>
 </c:if>
 <c:if test="${feed_userAuth}">
   <c:set var="feed_adminNote2" value="bbb"/>
 </c:if>
 <span>${feed_adminNote2}"</span>