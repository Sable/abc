<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<f:view>
  <h:outputLabel for="userNameInput" />
  <h:inputText id="user<caret>NameInput"/>
  <h:message for="userNameInput"/>
</f:view>