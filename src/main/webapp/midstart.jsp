<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html>
<head>
    <title>Start mobile id auth</title>
</head>
<body>

<spring:message code="welcome.title"/>

<form id="startmid" method="post" action="midauth">
    Original request: ${SAMLRequest}
    <input type="hidden" name="SAMLRequest" value="${SAMLRequest}"/>
    <input name="phoneNumber"/>
    <button type="submit" id="startbtn">Start mobile id authentication</button>
</form>
</body>
</html>
