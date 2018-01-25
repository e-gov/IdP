<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html>
<head>
    <title>Some kind of title</title>
</head>
<body>
<form id="sendresponse" method="post" action="${responseCallback}">
    Original request: ${SAMLRequest}
    <input type="hidden" name="SAMLResponse" value="${SAMLResponse}"/>
    <button type="submit" id="authbtn">Send response</button>
</form>
</body>
</html>
