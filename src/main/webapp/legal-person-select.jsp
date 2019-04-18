<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include/head.jspf" %>
<body>
    <script src="static/scripts/legalperson.js" defer></script>
    <div class="c-layout">
        <div class="c-layout--full">
            <%@ include file="/include/header-bar.jspf" %>
            <%@ include file="/include/header.jspf" %>
            <div class="container">

                <div class="alert alert-neutral">

                    <h2><spring:message code="html.auth.legalperson-representative"/></h2>
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

                   <hr>

                    <h2><spring:message code="html.auth.legalperson"/></h2>
                    <c:if test="${sessionScope.naturalPerson.idCode != null}">
                        <div id="results-container" class="alert alert-neutral">
                            <span id="results-loading" class="hidden">
                                <div class="loadersmall"></div>
                                <div><spring:message code="html.auth.legalperson-loading"/></div>
                            </span>
                            <span id="results-summary" class="hidden">
                                <div><spring:message code="html.auth.legalperson-loading-result"/></div>
                                <br>
                            </span>
                            <ul id="results-legal-person-list"></ul>
                        </div>
                        <div id="error-results-container" class="alert alert-error hidden">
                            <p id="error-no-results" class="hidden"><spring:message code="html.error.no-results"/></p>
                            <p id="error-service-not-available" class="hidden"><spring:message code="html.error.service-not-available"/></p>
                            <p id="error-technical-problem" class="hidden"><spring:message code="html.error.technical-error"/></p>
                        </div>
                        <form method="post" action="confirm_legal_person" class="c-form" id="selectLegalPerson">
                            <input type="hidden" name="legalPersonId" value=""/>
                            <input type="hidden" name="lang" value="${lang}"/>
                            <button type="submit" class="c-btn c-btn--primary hidden" id="btn-select-legal-person"><spring:message code="html.auth.next" /></button>
                        </form>
                    </c:if>

                    <br>

                    <div id="error-controls-container" class="hidden">
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

            </div>
        </div>
        <%@ include file="/include/footer.jspf" %>
    </div>
</body>
</html>