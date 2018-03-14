<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<head>
    <title><spring:message code="welcome.title"/></title>

    <link href="https://fonts.googleapis.com/css?family=Roboto%7CRoboto+Condensed" rel="stylesheet" type="text/css">
    <link href="static/favicon.png" rel="shortcut icon" type="image/png">
    <link href="static/global.css" rel="stylesheet" media="screen">

    <meta name="author" content="<spring:message code="html.author"/>">
    <meta name="copyright" content="www.ria.ee">
    <meta name="description" content="<spring:message code="html.description"/>">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>

<!-- PAGE BAR -->
<div class="page-bar">
    <div class="container clear">
        <h1><spring:message code="welcome.title"/></h1>
    </div>
</div>
<!-- //PAGE BAR -->

<!-- PAGE WRAPPER -->
<div class="page-wrapper">
    <!-- MAIN -->
    <div class="main">
            <!-- ROW -->
            <div class="row col-2 clear">
                <div class="block copy">
                    <div class="wrapper">
                        <h2><spring:message code="html.mid.auth"/></h2>
                        <form id="startmid" method="post" action="midauth">
                            <input type="hidden" name="lang" value="${lang}"/>
                            <div class="row">
                                <label for="phoneNumber"><spring:message code="html.mid.phonenumber"/>:</label>
                                <input id="phoneNumber" name="phoneNumber" type="text" class="txt">
                            </div>
                            <div class="row btns">
                                <input type="hidden" name="SAMLRequest" value="${SAMLRequest}"/>
                                <button type="submit" class="btn btn--primary"><spring:message code="html.mid.authbtn"/></button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
     </div>

    <!-- FOOTER -->
    <div class="footer">
        <div class="funding">
            <p><img src="static/logo-europe.png" alt=""><spring:message code="html.funding.1"/><br><spring:message code="html.funding.2"/></p>
        </div>
        <div class="ria">
            <p><spring:message code="html.ria.name"/>, <a href="mailto:help@ria.ee">help@ria.ee</a>, <a href="http://www.ria.ee" target="_blank">www.ria.ee</a></p>
        </div>
    </div>
</div>
<!-- //PAGE WRAPPER -->
</body>
</html>