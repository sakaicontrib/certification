<%@ include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>
		<form:form id="certList" method="POST">
		<c:choose>
		<c:when test="${empty certList}">
		 		<spring:message code="form.text.emptycertlist"/>
		</c:when>
		<c:otherwise>
        <c:forEach items="${unmetCriteria}" var="condition">
            <div id="unmetConditions" class="alertMessage">
                <spring:message code="error.unmet" arguments="${condition.expression}"/>
            </div>
        </c:forEach>
	    <c:if test="${errorMessage != null}" >
	        <div id="errorMessage" class="alertMessage" >
	            <spring:message code="${errorMessage}" />
	        </div>
	    </c:if>
 		<div class="listNav">
			<div class="pager">
				<span style="align:center">showing&nbsp;<c:out value="${firstElement}" />&nbsp;&#045;&nbsp;<c:out value="${lastElement}" />&nbsp;of&nbsp;${certList.nrOfElements}</span></br>
				<c:choose>
				<c:when test="${!certList.firstPage}">
					<input type="button" id="first" value="<spring:message code="pagination.first"/>" />&nbsp;
					<input type="button" id="prev" value="<spring:message code="pagination.previous"/>" />
				</c:when>
				<c:otherwise>
					<input type="button" id="nofirst" value="<spring:message code="pagination.first"/>" disabled="disabled" />&nbsp;
					<input type="button" id="noPrev" value="<spring:message code="pagination.previous"/>" disabled="disabled" />
				</c:otherwise>
				</c:choose>
				<input type="hidden" id="pageNo" value="${pageNo}" />
				<select id="pageSize">
				<c:forEach items="${pageSizeList}" var="list">
					<c:choose>
					<c:when test="${list > 100}">
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
						<input type="button" id="next" value="<spring:message code="pagination.next"/>" />&nbsp;
						<input type="button" id="last" value="<spring:message code="pagination.last"/>" />
					</c:when>
					<c:otherwise>
						<input type="button" id="noNext" value="<spring:message code="pagination.next"/>" disabled="disabled"/>
						<input type="button" id="noLast" value="<spring:message code="pagination.last"/>" disabled="disabled"/>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		<table id="cList" class="listHier lines nolines" cellspacing="0" cellpadding="0" border="0" summary="Certificates">
			<thead>
				<tr>
				  <th><spring:message code="form.label.certificate"/></th>
                  <th><spring:message code="form.label.certificate.description"/></th>
				  <th><spring:message code="form.label.awarded"/></th>
				  <th></th>
				</tr>
			</thead>
			<tbody>
	        	<c:forEach var="cert" items="${certList.pageList}">
	            <tr>
	            	<td>
	                	${cert.name}
	                </td>
                    <td>
                        ${cert.description}
                    </td>
            <c:choose>
                <c:when test="${certAwardList[cert.id] != null}">

                    <td>
                        ${certAwardList[cert.id].certificationTimeStamp}
                    </td>
                    <td>
                        <a href='<c:url value="printPreview.form"><c:param name="certId" value="${cert.id}" /></c:url>'>
                            <spring:message code="form.label.printcert" />
                        </a>
                    </td>
                </c:when>
                <c:otherwise>
                    <td></td>
                    <td>
                        <a href='<c:url value="checkstatus.form"><c:param name="certId" value="${cert.id}" /></c:url>'>
                            <spring:message code="form.label.checkstatus" />
                        </a>
                    </td>
                </c:otherwise>
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
				location.href="list.form?page=first";
				return false;
			});
			
			$("#prev").click( function() {
				location.href="list.form?page=previous";
				return false;
			});
			
			$("#next").click( function() {
				location.href="list.form?page=next";
				return false;
			});
			
			$("#last").click( function() {
				location.href="list.form?page=last";
				return false;
			});
			
			$("#pageSize").change( function() {
				location.href="list.form?pageSize=" + $("#pageSize option:selected").val() +" &pageNo=" + $("#pageNo").val();
				return false;
			});
		});
		
	</script>
<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
