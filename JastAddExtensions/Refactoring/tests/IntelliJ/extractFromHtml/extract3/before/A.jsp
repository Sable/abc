<%--
  Created by IntelliJ IDEA.
  User: Mikhail.Gedzberg
  Date: 10.07.2006
  Time: 13:23:05
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>Simple jsp page</title>
<selection>      <script type="text/javascript">
          function setCookie(name, value, lifespan, access_path) {
              var cookietext = name + "=" + escape(value);
              if (lifespan != null) {
                  var today = new Date();
                  var expiredate = new Date();
                  expiredate.setTime(today.getTime() + 1000 * 60 * 60 * 24 * lifespan);
                  cookietext += "; expires=" + expiredate.toGMTString();
              }
              if (access_path != null) {
                  cookietext += "; PATH=" + access_path;
              }
              document.cookie = cookietext;
              return null;
          }
      </script>
</selection>
  </head>
  <body>Place your content here</body>
</html>