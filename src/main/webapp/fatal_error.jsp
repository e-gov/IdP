<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ include file="/include/head.jspf" %>
<body>
    <div class="c-layout">
        <div class="c-layout--full">
            <%@ include file="/include/header-bar.jspf" %>
            <%@ include file="/include/header.jspf" %>
            <div class="container">
                <div class="alert alert-error">
                    <p><strong><spring:message code="html.error.title"/></strong></p>
                    <p><spring:message code="error.general"/></p>
                </div>
            </div>
        </div>
        <%@ include file="/include/footer.jspf" %>
    </div>
</body>
</html>