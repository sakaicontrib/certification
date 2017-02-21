<%@ include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>
<form:form id="certList" method="POST">
    <c:choose>
        <c:when test="${certList.nrOfElements == 0}">
            <ul class="navIntraTool actionToolBar">
                <li><span><a href="" id="Add"><spring:message code="form.menu.add"/></a></span></li>
            </ul>
            <p class="instruction">
                <spring:message code="instructions.admin"/>
            </p>
            <p class="instruction">
                <spring:message code="form.text.emptycertlist.instruct"/></br>
            </p>
        </c:when>
        <c:otherwise>
            <ul class="navIntraTool actionToolBar">
                <li><span><a href="" id="Add"><spring:message code="form.menu.add"/></a></span></li>
                <li><span><a href="" id="Edit"><spring:message code="form.menu.edit"/></a></span></li>
                <li><span><a href="" id="Delete"><spring:message code="form.menu.delete"/></a></span></li>
            </ul>
            <div class="navPanel row">
                <div id="submitError" class="alertMessage" style="display:none"></div>
                <c:if test="${errorMessage != null}" >
                    <div id="errorMessage" class="alertMessage" >
                        <spring:message code="${errorMessage}" />
                    </div>
                </c:if>
                <div class="instruction col-md-8">
                    <p>
                        <spring:message code="instructions.admin"/>
                        <c:if test="${highMembers}">
                            <spring:message code="instructions.high.members"/>
                        </c:if>
                    </p>
                </div>
                <div class="col-md-4">
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
                                    <input type="button" id="noNext" value="<spring:message code='pagination.next' />" disabled="disabled"/>
                                    <input type="button" id="noLast" value="<spring:message code='pagination.last' />" disabled="disabled"/>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </nav>
                </div>
            </div>
            <table id="cList" class="listHier" summary="Certificates">
                <thead align="center">
                    <tr>
                        <th></th>
                        <th><spring:message code="form.label.certificate"/></th>
                        <th><spring:message code="form.label.certificate.description"/></th>
                        <th><spring:message code="form.label.status"/></th>
                        <th><spring:message code="form.label.created"/></th>
                        <th><spring:message code="form.label.report"/></th>
                    </tr>
                </thead>
                <tbody align="left">
                    <c:forEach var="cert" items="${certList.pageList}">
                    <tr>
                        <td>
                            <input type="radio" name="certDefRadioButtons" value="${cert.id}"/>
                        </td>
                        <td>
                            <c:out value="${cert.name}"></c:out>
                        </td>
                        <td>
                            <c:out value="${cert.description}"></c:out>
                        </td>
                        <td>
                            ${cert.status}
                        </td>
                        <td>
                            ${cert.formattedCreateDate}
                        </td>
                        <td>
                            <c:if test="${cert.status == 'ACTIVE'}" >
                                <a id="report${cert.id}" href="reportView.form?certId=${cert.id}" onclick="SPNR.insertSpinnerInPreallocated( this, null, 'spinner_${cert.id}' );">
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

        loaded();

        $("#first").click( function() {
            SPNR.disableControlsAndSpin( this, null );
            location.href="list.form?page=first";
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
            location.href="list.form?pageSize=" + $("#pageSize option:selected").val() +" &pageNo=" + $("#pageNo").val();
            return false;
        });

        $("#Add").click( function() {
            location.href="first.form";
            return false;
        });

        $("#Edit").click( function() {

            if(singleChecked())
            {
                location.href="first.form?certId="+$("input:checked").val();
                return false;
            }
            return false;
        });

        $("#Delete").click( function() {
            if(singleChecked())
            {
                var proceed = confirm ("<spring:message code='form.delete.confirm'/>\n\n" +
                                       "<spring:message code='form.delete.confirm.ok'/>");

                if (proceed === true)
                    location.href = "delete.form?certId=" + $("input:checked").val();
                    return false;
            }

            return false;
        });

        $(":checkbox").click( function() {
            $(":checkbox").not(this).removeAttr("checked");
        });

        function singleChecked()
        {
            $(".alertMessage").hide();
            if($("input:checked").length === 1)
            {
                return true;
            }
            else if($("input:checked").length === 0)
            {
                $("#submitError").html("<spring:message code='form.error.noneselected'/>").show();
                resetHeight();
                return false;
            }
            else
            {
                $("#submitError").html("<spring:message code='form.error.multipleselect'/>").show();
                resetHeight();
                return false;
            }
        }
    });
</script>
<%@ include file="/WEB-INF/jsp/footer.jsp" %>
