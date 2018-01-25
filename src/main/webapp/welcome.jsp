<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html>
<head>
    <title>"<spring:message code="welcome.title"/>"</title>
</head>
<body>

<spring:message code="welcome.title"/>

Original request: ${SAMLRequest}

<form id="selectlang" method="post" action="auth">
    <input type="hidden" name="SAMLRequest" value="${SAMLRequest}"/>
    <button type="submit" name="lang" value="et">Eesti</button>
    <button type="submit" name="lang" value="en">English</button>
    <button type="submit" name="lang" value="ru">Vene</button>
</form>

<form id="idcardauth" method="post" action="idauth">
    <input type="hidden" name="SAMLRequest" value="${SAMLRequest}"/>
    <button type="submit" id="idcardauthbtn">Authenticate with id card</button>
</form>

<form id="midauth" method="post" action="midwelcome">
    <input type="hidden" name="lang" value="${lang}"/>
    <input type="hidden" name="SAMLRequest" value="${SAMLRequest}"/>
    <button type="submit" id="midauthbtn">Authenticate with mobile id</button>
</form>
</body>
</html>
