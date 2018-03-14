<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%--
  ~ MIT License
  ~
  ~ Copyright (c) 2018 Estonian Information System Authority
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a copy
  ~ of this software and associated documentation files (the "Software"), to deal
  ~ in the Software without restriction, including without limitation the rights
  ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  ~ copies of the Software, and to permit persons to whom the Software is
  ~ furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in all
  ~ copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  ~ SOFTWARE.
  ~
  --%>

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
                        <h2><spring:message code="html.error.title"/></h2>

                        <p class="row"><spring:message code="error.general"/></p>
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