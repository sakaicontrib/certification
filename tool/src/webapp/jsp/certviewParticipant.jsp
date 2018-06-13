<%@ include file="/jsp/include.jsp" %>
<jsp:include page="/jsp/header.jsp"/>
<form:form id="certList" method="POST">
    <c:choose>
        <c:when test="${certList.nrOfElements == 0}">
            <div class="navPanel">
                <p class="instruction">
                    <spring:message code="instructions.student" />
                </p>
                <p class="instruction">
                    <spring:message code="form.text.emptycertlist" />
                </h3>
            </div>
        </c:when>
        <c:otherwise>
            <div class="navPanel">
                <c:forEach items="${unmetCriteria}" var="condition">
                    <div id="unmetConditions" class="alertMessage">
                        <spring:message code="error.unmet" arguments="${condition.expression}" />
                    </div>
                </c:forEach>
                <c:if test="${errorMessage != null}">
                    <div id="errorMessage" class="alertMessage">
                        <spring:message code="${errorMessage}" arguments="${errorArgs}" />
                    </div>
                </c:if>
                <div class="instruction">
                    <p><spring:message code="instructions.student" /></p>
                </div>
                <div class="row">
                    <div class="col-sm-7 col-xs-12">
                        <nav class="certPager panel panel-default">
                            <div class="panel-heading">
                                <spring:message code="form.pager.showing"/> <c:out value="${firstElement}" /> - <c:out value="${lastElement}" /> of ${certList.nrOfElements}
                                <div id="spinner" class="allocatedSpinPlaceholder"></div>
                            </div>
                            <div class="panel-body">
                                <c:choose>
                                    <c:when test="${!certList.firstPage}">
                                        <input type="button" id="first" value="<spring:message code='pagination.first' />" />
                                        <input type="button" id="prev" value="<spring:message code='pagination.previous' />" />
                                    </c:when>
                                    <c:otherwise>
                                        <input type="button" id="nofirst" value="<spring:message code='pagination.first' />" disabled="disabled" />
                                        <input type="button" id="noPrev" value="<spring:message code='pagination.previous' />" disabled="disabled" />
                                    </c:otherwise>
                                </c:choose>
                                <input type="hidden" id="pageNo" value="${pageNo}" />
                                <select id="pageSize">
                                    <c:forEach items="${pageSizeList}" var="list">
                                        <c:choose>
                                            <c:when test="${list > 200}">
                                                <option value="${list}" <c:if test="${pageSize eq list}">selected="selected"</c:if>><spring:message code="form.label.showall" /></option>
                                            </c:when>
                                            <c:otherwise>
                                                <option value="${list}" <c:if test="${pageSize eq list}">selected="selected"</c:if>><spring:message code="form.label.show" arguments="${list}" /></option>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </select>
                                <c:choose>
                                    <c:when test="${!certList.lastPage}">
                                        <input type="button" id="next" value="<spring:message code='pagination.next' />" />
                                        <input type="button" id="last" value="<spring:message code='pagination.last' />" />
                                    </c:when>
                                    <c:otherwise>
                                        <input type="button" id="noNext" value="<spring:message code='pagination.next' />" disabled="disabled" />
                                        <input type="button" id="noLast" value="<spring:message code='pagination.last' />" disabled="disabled" />
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </nav>
                    </div>
                </div>
                <table id="cList" class="table table-hover table-striped table-bordered" summary="Certificates">
                    <thead>
                        <tr>
                          <th><spring:message code="form.label.certificate"/></th>
                          <th><spring:message code="form.label.certificate.description"/></th>
                          <th><spring:message code="form.label.requirements"/></th>
                          <th><spring:message code="form.label.viewcert"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="cert" items="${certList.pageList}">
                            <tr>
                                <td><c:out value="${cert.name}"></c:out></td>
                                <td><c:out value="${cert.description}"></c:out></td>
                                <c:choose>
                                    <c:when test="${certRequirementList[cert.id] != null}">
                                        <td>
                                            <c:choose>
                                                <c:when test="${cert.progressHidden}">
                                                    <span class="instruction">
                                                        <spring:message code="form.label.requirements.hidden" />
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <ul>
                                                        <c:forEach items="${certRequirementList[cert.id]}" var="req">
                                                            <li>${req.key}</li>
                                                            <ul>
                                                                <li>${req.value}</li>
                                                            </ul>
                                                        </c:forEach>
                                                    </ul>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${certIsAwarded[cert.id]}">
                                                    <a target="_blank" id="viewCert${cert.id}" href="print.form?certId=${cert.id}">
                                                        <spring:message code="form.submit.print" />
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <spring:message code="form.submit.na" />
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </c:when>
                                </c:choose>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</form:form>
