<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html>
<head>
    <title>Invalid authentication data</title>
</head>
<body>
<form id="sendresponse" method="post" action="${responseCallback}">
    <input type="hidden" name="SAMLResponse" value="${SAMLResponse}"/>
    <button type="submit" id="authbtn">Send response</button>
</form>
</body>
</html>
