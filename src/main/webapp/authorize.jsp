<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include/head.jspf" %>
<body>
    <div class="c-layout">
        <div class="c-layout--full">
            <%@ include file="/include/header-bar.jspf" %>
            <%@ include file="/include/header.jspf" %>
            <div class="container">

                <div class="alert alert-neutral">
                    <h2><spring:message code="html.auth.identified"/></h2>
                    <table>
                        <tr>
                            <th><spring:message code="html.auth.personalCode"/></th>
                            <td>${personalCode}</td>
                        </tr>
                        <tr>
                            <th><spring:message code="html.auth.lastName"/></th>
                            <td>${surName}</td>
                        </tr>
                        <tr>
                            <th><spring:message code="html.auth.firstName"/></th>
                            <td>${name}</td>
                        </tr>
                        <tr>
                            <th><spring:message code="html.auth.birthdate"/></th>
                            <td>${birthDate}</td>
                        </tr>
                    </table>
                    <c:if test="${legalPersonIdentifier != null}">
                        <hr>
                        <table>
                            <h2><spring:message code="html.auth.legalperson.selected-legal-person"/></h2>
                            <tr>
                                <th><spring:message code="html.auth.legalperson.legalPersonName"/></th>
                                <td>${legalPersonName}</td>
                            </tr>
                            <tr>
                                <th><spring:message code="html.auth.legalperson.legalPersonIdentifier"/></th>
                                <td>${legalPersonIdentifier}</td>
                            </tr>
                        </table>
                    </c:if>
                    <br>
                    <form method="post" action="${responseCallback}" class="c-form" id="authorizeForm">
                        <input type="hidden" name="SAMLResponse" value="${SAMLResponse}"/>
                        <button type="submit" class="c-btn c-btn--primary"  id="submitToEidasNode"><spring:message code="html.auth.home" /></button>
                    </form>
                </div>

            </div>
        </div>
        <%@ include file="/include/footer.jspf" %>
    </div>
</body>
</html>