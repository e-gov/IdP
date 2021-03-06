﻿<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ include file="/include/head.jspf" %>
<body class="is-mobile-subview">
    <div class="c-layout">
        <div class="c-layout--full">
            <%@ include file="/include/header-bar.jspf" %>
            <%@ include file="/include/header.jspf" %>
            <div class="container">
                <div class="c-tab-login">

                    <div class="c-tab-login__header hide-in-desktop">
                        <ul class="c-tab-login__nav">
                            <li class="c-tab-login__nav-item is-active">
                                <a href="#" type="submit" class="c-tab-login__nav-link is-active" data-tab="mobile-id">
                                    <span class="c-tab-login__nav-label">
                                        <svg class="icon icon-mobile-id">
                                            <use xlink:href="#icon-mobile-id"></use>
                                        </svg>
                                        <spring:message code="html.mobile-id.title"/>
                                    </span>
                                </a>
                            </li>
                        </ul>
                    </div>

                    <div class="c-tab-login__main">

                        <div class="c-tab-login__content is-active" data-tab="mobile-id">
                            <div class="c-tab-login__content-wrap">
                                <div class="c-tab-login__content-icon">
                                    <svg class="icon icon-mobile-id">
                                        <use xlink:href="#icon-mobile-id"></use>
                                    </svg>
                                </div>
                                <div class="c-tab-login__content-text">
                                    <h2><spring:message code="html.mobile-id.title"/></h2>
                                    <p><spring:message code="html.mobile-id.instruction"/></p>
                                    <form action="midauth" method="post" id="mobileIdForm" class="c-form">
                                        <input type="hidden" name="lang" value="${lang}"/>
                                        <input type="hidden" name="SAMLRequest" value="${SAMLRequest}"/>
                                        <table>
                                            <tbody>
                                                <tr>
                                                    <td class="col-label">
                                                        <label for="mid-personal-code" class="form-label"><spring:message code="html.mobile-id.personalCode"/></label>
                                                    </td>
                                                    <td>
                                                        <div class="input-group">
                                                            <div class="input-group-prepend">
                                                                <span class="input-group-text"><spring:message code="html.mobile-id.personalCodePrefix"/></span>
                                                            </div>
                                                            <input type="text" maxlength="11" id="mid-personal-code" class="form-control" name="personalCode">
                                                        </div>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="col-label">
                                                        <label for="mid-phone-number" class="form-label"><spring:message code="html.mobile-id.phoneNumber"/></label>
                                                    </td>
                                                    <td>
                                                        <div class="input-group">
                                                            <div class="input-group-prepend">
                                                                <span class="input-group-text"><spring:message code="html.mobile-id.phoneNumberPrefix"/></span>
                                                            </div>
                                                            <input type="text" maxlength="15" id="mid-phone-number" class="form-control" name="phoneNumber">
                                                        </div>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td></td>
                                                    <td>
                                                        <button type="submit" class="c-btn c-btn--primary"><spring:message code="html.common.login"/></button>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </form>
                                </div>
                            </div>
                            <div class="c-tab-login__footer">
                                <p></p>
                                <p><a href="<spring:message code="link.mobile-id.help"/>" target="_blank" rel="noopener"><spring:message code="html.mobile-id.help"/></a></p>
                            </div>
                        </div>

                    </div>

                </div>
            </div>
        </div>
        <%@ include file="/include/footer.jspf" %>
    </div>

    <svg aria-hidden="true" class="c-inline-svg__hidden" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
        <defs>
            <symbol id="icon-mobile-id" viewBox="0 0 20 32">
                <title>mobile-id</title>
                <path d="M7.218 28.027h5.476v1.12h-5.476zM15.876 31.68h-12.089c-0 0-0 0-0 0-1.8 0-3.261-1.454-3.271-3.252v-24.854c0.005-1.803 1.468-3.262 3.271-3.262 0 0 0 0 0 0h12.089c0 0 0 0 0 0 1.803 0 3.266 1.459 3.271 3.262v24.845c-0.005 1.803-1.468 3.262-3.271 3.262 0 0 0 0 0 0v0zM3.787 1.422c0 0 0 0-0 0-1.19 0-2.155 0.962-2.16 2.151v22.045h16.409v-22.044c0 0 0 0 0-0 0-1.188-0.963-2.151-2.151-2.151-0.003 0-0.006 0-0.009 0h0zM1.627 28.418c0.005 1.189 0.97 2.151 2.16 2.151 0 0 0 0 0 0h12.089c0 0 0 0 0 0 1.19 0 2.155-0.962 2.16-2.151v-1.689h-16.409zM12.382 8.951c-0.721-0.218-1.55-0.343-2.408-0.343-0.544 0-1.076 0.050-1.592 0.147l0.053-0.008c-0.018 0.004-0.039 0.006-0.060 0.006-0.154 0-0.282-0.108-0.313-0.252l-0-0.002c-0.009-0.089 0-0.169 0.053-0.231 0.047-0.068 0.119-0.117 0.202-0.133l0.002-0c0.494-0.094 1.063-0.147 1.644-0.147 0.922 0 1.813 0.135 2.653 0.386l-0.065-0.017c0.132 0.040 0.226 0.16 0.226 0.302 0 0.174-0.141 0.315-0.315 0.315-0.032 0-0.062-0.005-0.091-0.013l0.002 0.001zM14.187 11.396c-0 0-0.001 0-0.001 0-0.066 0-0.127-0.020-0.178-0.054l0.001 0.001c-1.129-0.791-2.531-1.263-4.043-1.263-1.375 0-2.659 0.391-3.747 1.067l0.030-0.017c-0.049 0.032-0.109 0.051-0.174 0.051-0.113 0-0.213-0.059-0.27-0.148l-0.001-0.001c-0.033-0.049-0.053-0.108-0.053-0.173 0-0.116 0.064-0.217 0.158-0.271l0.002-0.001c1.151-0.716 2.547-1.141 4.043-1.141 1.65 0 3.18 0.517 4.435 1.397l-0.025-0.016c0.142 0.089 0.178 0.293 0.089 0.436-0.059 0.081-0.153 0.133-0.26 0.133-0.002 0-0.005-0-0.007-0h0zM5.236 13.92c-0.176-0.001-0.318-0.144-0.318-0.32 0-0.070 0.023-0.135 0.061-0.188l-0.001 0.001c1.147-1.527 2.954-2.505 4.99-2.505 1.961 0 3.709 0.907 4.85 2.324l0.009 0.012c0.107 0.142 0.089 0.338-0.053 0.444-0.053 0.041-0.121 0.066-0.195 0.066-0.101 0-0.19-0.046-0.249-0.119l-0-0.001c-1.035-1.279-2.604-2.090-4.364-2.090-1.826 0-3.448 0.874-4.471 2.227l-0.010 0.014c-0.057 0.078-0.146 0.13-0.248 0.133l-0.001 0zM5.556 17.138c-0.003 0-0.006 0-0.009 0-0.172 0-0.311-0.139-0.311-0.311 0-0 0-0 0-0v0-0.018c0.161-2.486 2.216-4.441 4.728-4.441 2.137 0 3.943 1.415 4.534 3.358l0.009 0.034c0.004 0.018 0.006 0.039 0.006 0.061 0 0.177-0.143 0.32-0.32 0.32-0.128 0-0.238-0.075-0.289-0.183l-0.001-0.002v-0.018c-0.511-1.722-2.079-2.956-3.936-2.956-2.181 0-3.963 1.703-4.091 3.852l-0.001 0.011c-0.010 0.164-0.145 0.293-0.311 0.293-0.003 0-0.007-0-0.010-0h0zM7.004 19.387c-0.176-0-0.319-0.144-0.319-0.32 0-0.025 0.003-0.050 0.008-0.073l-0 0.002c0.082-0.236 0.129-0.507 0.129-0.79 0-0.116-0.008-0.231-0.023-0.343l0.001 0.013c-0.085-0.277-0.134-0.596-0.134-0.926 0-1.802 1.461-3.262 3.262-3.262 1.579 0 2.896 1.122 3.197 2.612l0.004 0.021c0.142 0.587 0.213 1.182 0.204 1.778 0 0.178-0.142 0.32-0.32 0.32-0.17-0.005-0.306-0.141-0.311-0.311v-0c0-0.551-0.062-1.102-0.178-1.636-0.291-1.16-1.325-2.005-2.556-2.005-1.453 0-2.631 1.178-2.631 2.631 0 0.222 0.028 0.439 0.080 0.645l-0.004-0.018c0.080 0.329 0.044 0.809-0.107 1.422-0.036 0.137-0.157 0.236-0.302 0.24h-0zM8.311 20.151l-0.089-0.009c-0.128-0.041-0.219-0.158-0.219-0.297 0-0.034 0.005-0.066 0.015-0.096l-0.001 0.002c0.169-0.456 0.266-0.982 0.266-1.531 0-0.244-0.019-0.483-0.056-0.717l0.003 0.026c-0.059-0.17-0.093-0.367-0.093-0.571 0-0.992 0.804-1.796 1.796-1.796 0.892 0 1.632 0.65 1.772 1.503l0.001 0.010c0.213 0.836 0.222 1.804 0.027 2.88-0.001 0.176-0.144 0.319-0.32 0.319s-0.32-0.143-0.32-0.32c0-0.038 0.007-0.074 0.018-0.107l-0.001 0.002c0.178-0.987 0.169-1.867-0.018-2.631-0.128-0.51-0.583-0.882-1.124-0.882-0.639 0-1.158 0.518-1.158 1.158 0 0.098 0.012 0.193 0.035 0.284l-0.002-0.008c0.16 0.64 0.089 1.502-0.222 2.56-0.043 0.13-0.163 0.222-0.305 0.222-0.002 0-0.005-0-0.007-0h0zM9.804 20.329c-0.172-0.005-0.31-0.147-0.31-0.32 0-0.025 0.003-0.050 0.008-0.073l-0 0.002c0.159-0.552 0.256-1.187 0.267-1.843l0-0.006c0-0.311-0.036-0.622-0.107-0.916-0.001-0.008-0.001-0.017-0.001-0.026 0-0.177 0.143-0.32 0.32-0.32 0.131 0 0.244 0.079 0.294 0.192l0.001 0.002c0.089 0.356 0.124 0.711 0.116 1.067-0.010 0.716-0.113 1.403-0.298 2.056l0.014-0.056c-0.036 0.137-0.157 0.236-0.302 0.24h-0z"></path>
            </symbol>
        </defs>
    </svg>
</body>
</html>