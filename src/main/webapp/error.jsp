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
                    <p><spring:message code="${errorMessageCode}"/></p>
                </div>
                <form method="post" action="auth">
                    <p class="link-back">
                        <input type="hidden" name="lang" value="${lang}"/>
                        <input type="hidden" name="SAMLRequest" value="${SAMLRequest}"/>
                        <button type="submit" class="as-link"><spring:message code="html.error.restart"/></button>
                    </p>
                </form>
                <form method="post" action="${responseCallback}">
                    <p class="link-back">
                        <input type="hidden" name="SAMLResponse" value="${SAMLResponse}"/>
                        <button type="submit" class="as-link"><spring:message code="html.error.cancel"/></button>
                    </p>
                </form>

            </div>
        </div>
        <%@ include file="/include/footer.jspf" %>
    </div>
</body>
</html>