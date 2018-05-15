<%@ include file="/jsp/include.jsp" %>
<jsp:include page="/jsp/header.jsp" />
<form:form id="certList" method="POST">
    <%@ include file="/jsp/adminActionToolBar.jsp" %>

    <div class="page-header">
        <h1><spring:message code="form.menu.list" /></h1>
    </div>

    <c:choose>
        <c:when test="${certList.nrOfElements == 0}">
            <p class="instruction">
                <spring:message code="instructions.admin" />
            </p>
            <p class="instruction">
                <spring:message code="form.text.emptycertlist.instruct" /></br>
            </p>
        </c:when>
        <c:otherwise>
            <c:if test="${errorMessage != null}" >
                <div id="errorMessage" class="alertMessage">
                    <spring:message code="${errorMessage}" />
                </div>
            </c:if>
            <div class="instruction">
                <p>
                    <spring:message code="instructions.admin" />
                    <c:if test="${highMembers}">
                        <spring:message code="instructions.high.members" />
                    </c:if>
                </p>
            </div>
            <div class="row">
                <div class="col-sm-7 col-xs-12">
                    <nav class="certPager panel panel-default">
                        <div class="panel-heading">
                            <spring:message code="form.pager.showing" /> <c:out value="${firstElement}" /> - <c:out value="${lastElement}" /> of ${certList.nrOfElements}
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
                                    <input type="button" id="noNext" value="<spring:message code='pagination.next' />" disabled="disabled"/>
                                    <input type="button" id="noLast" value="<spring:message code='pagination.last' />" disabled="disabled"/>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </nav>
                </div>
            </div>
            <table id="cList" class="table table-hover table-striped table-bordered" summary="Certificates">
                <thead>
                    <tr>
                        <th class="colCertificate"><spring:message code="form.label.certificate"/></th>
                        <th class="colActions colMin"></th>
                        <th class="hidden-xs"><spring:message code="form.label.certificate.description"/></th>
                        <th><spring:message code="form.label.status"/></th>
                        <th><spring:message code="form.label.created"/></th>
                        <th><spring:message code="form.label.report"/></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="cert" items="${certList.pageList}">
                        <tr class="certificate">
                            <td class="colCertificate">
                                <a href="first.form?certId=${cert.id}" title="${form.actions.edit.title} ${cert.name}">
                                    <c:out value="${cert.name}"></c:out>
                                </a>
                            </td>
                            <td class="colActions colMin">
                                <div class="btn-group pull-right">
                                    <button id="" type="button" class="btn btn-xs dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                        <span class="sr-only"><spring:message code="form.actions" /></span>
                                        <span><spring:message code="form.actions" /></span>
                                        <span class="caret"></span>
                                    </button>
                                    <ul class="dropdown-menu row" role="menu">
                                        <li class="dropdown-button">
                                            <a href="first.form?certId=${cert.id}">
                                                <spring:message code="form.actions.edit" />
                                            </a>
                                            <c:if test="${cert.status == 'ACTIVE'}" >
                                                <a id="report${cert.id}" href="reportView.form?certId=${cert.id}" onclick="SPNR.insertSpinnerInPreallocated( this, null, 'spinner_${cert.id}' );">
                                                    <spring:message code="form.label.report.cell" />
                                                </a>
                                            </c:if>
                                            <a href="#" onClick="deleteCert('${cert.id}')">
                                                <spring:message code="form.actions.remove" />
                                            </a>
                                        </li>
                                    </ul>
                                </div>
                            </td>
                            <td class="hidden-xs"><c:out value="${cert.description}"></c:out></td>
                            <td>${cert.status}</td>
                            <td><span class="createdDate">${cert.createDate}</span></td>
                            <td>
                                <c:if test="${cert.status == 'ACTIVE'}">
                                    <a id="report${cert.id}" href="reportView.form?certId=${cert.id}" onclick="SPNR.insertSpinnerInPreallocated(this, null, 'spinner_${cert.id}');">
                                        <spring:message code="form.label.report.cell"/>
                                    </a>
                                    <div id="spinner_${cert.id}" class="allocatedSpinPlaceholder"></div>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</form:form>

<script type="text/javascript">
    var redirecting = false;

    $(document).ready(function() {
        $("tr.certificate").each(function() {
            var createdDate = $(this).find("span.createdDate");
            var value = createdDate.html();
            createdDate.html(moment(value).format('LLL'));
        });

        $("#first").click(function() {
            SPNR.disableControlsAndSpin(this, null);
            location.href = "list.form?page=first";
            return false;
        });

        $("#prev").click( function() {
            SPNR.disableControlsAndSpin( this, null );
            location.href="list.form?page=previous";
            return false;
        });

        $("#next").click( function() {
            SPNR.disableControlsAndSpin( this, null );
            location.href="list.form?page=next";
            return false;
        });

        $("#last").click( function() {
            SPNR.disableControlsAndSpin( this, null );
            location.href="list.form?page=last";
            return false;
        });

        $("#pageSize").change( function() {
            SPNR.insertSpinnerInPreallocated( this, null, "spinner" );
            location.href = "list.form?pageSize=" + $("#pageSize option:selected").val() +" &pageNo=" + $("#pageNo").val();
            return false;
        });

        $(":checkbox").click( function() {
            $(":checkbox").not(this).removeAttr("checked");
        });
    });

    function deleteCert(certId) {
        var proceed = confirm ("<spring:message code='form.remove.confirm'/>\n\n" +
                               "<spring:message code='form.remove.confirm.ok'/>");

        if (proceed === true) {
            location.href = "delete.form?certId=" + certId;
        }
        e.preventDefault();
        return false;
    }
</script>
<%@ include file="/jsp/footer.jsp" %>
