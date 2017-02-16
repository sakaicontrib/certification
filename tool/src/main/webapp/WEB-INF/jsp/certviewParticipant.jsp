<%@ include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>
		<form:form id="certList" method="POST">
		<c:choose>
		<c:when test="${certList.nrOfElements == 0}">
			<p class="instruction">
				<spring:message code="instructions.student"/>
			</p>
			<h3 class="instruction" style="text-align:center">
				<spring:message code="form.text.emptycertlist"/>
			</h3>
		</c:when>
		<c:otherwise>
        <c:forEach items="${unmetCriteria}" var="condition">
            <div id="unmetConditions" class="alertMessage">
                <spring:message code="error.unmet" arguments="${condition.expression}"/>
            </div>
        </c:forEach>
	    <c:if test="${errorMessage != null}" >
	        <div id="errorMessage" class="alertMessage" >
	            <spring:message code="${errorMessage}" arguments="${errorArgs}"/>
	        </div>
	    </c:if>
		<div class="navPanel row">
			<div class="instruction col-md-8">
				<p><spring:message code="instructions.student"/></p>
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
	            	<td>
                        <c:out value="${cert.name}"></c:out>
	                </td>
                    <td>
                        <c:out value="${cert.description}"></c:out>
                    </td>
            <c:choose>
                <c:when test="${certRequirementList[cert.id] != null}">
                    <td>
                        <c:choose>
                            <c:when test="${cert.progressHidden}">
                                <span class="instruction">
                                    <spring:message code="form.label.requirements.hidden"/>
                                </span>
                            </c:when>
                            <c:otherwise>
                                <ul style="margin-top:0px; padding-left:14px">
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
                            <a id="viewCert${cert.id}" href="print.form?certId=${cert.id}">
                                <spring:message code="form.submit.print"/>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <spring:message code="form.submit.na"/>
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
	</div>
	<script type="text/javascript">
		$(document).ready(function() {

			loaded();

			$("#first").click( function() {
				SPNR.disableControlsAndSpin( this, null );
				location.href = "list.form?page=first";
				return false;
			});

			$("#prev").click( function() {
				SPNR.disableControlsAndSpin( this, null );
				location.href = "list.form?page=previous";
				return false;
			});

			$("#next").click( function() {
				SPNR.disableControlsAndSpin( this, null );
				location.href = "list.form?page=next";
				return false;
			});

			$("#last").click( function() {
				SPNR.disableControlsAndSpin( this, null );
				location.href = "list.form?page=last";
				return false;
			});

			$("#pageSize").change( function() {
				SPNR.insertSpinnerInPreallocated( this, null, "spinner" );
				location.href = "list.form?pageSize=" + $("#pageSize option:selected").val() +" &pageNo=" + $("#pageNo").val();
				return false;
			});
		});

	</script>
<%@ include file="/WEB-INF/jsp/footer.jsp" %>
