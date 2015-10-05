<jsp:include page="/WEB-INF/jsp/header.jsp"/>
<script type="text/javascript" src="/library/js/jquery/1.4.2/jquery-1.4.2.min.js"></script>
<script type="text/javascript" src="../js/jquery.pagination.js"></script>

<h1>Certification Tool</h1>

<div class="listNav">
    <rct:listScroll listUrl="${listUrl}" listScroll="${listScroll}" className="pager"/>
</div>

<div>
<c:if test="${user.canAdd || user.canEdit}">
	<form:form id="certList" method="post">
		<input type="submit" value="Add">&nbsp;<input type="submit" value="Edit">
	</form>
</c:if>
</div>
<div>
	
</div
<div>
<c:if test="${}">
	<table id="cList" cellspacing="0">
		<thead>
			<tr>
			  <c:if test="${user.canEdit}"><th><form:checkbox path="${cert.id}"></c:if>
			  <th>Certificate</th>
			  <th>Revision</th>
			  <th>Awarded</th>
			  <th></th>
			</tr>
			<%--<tr>
			<th><rc:sort name="Certificate" displayName="Certificate" sortUrl="${listUrl}"/></th>
			<th><rc:sort name="Revision" displayName="Revision" sortUrl="${listUrl}"/></th>
			<th><rc:sort name="Awarded" displayName="Awarded" sortUrl="${listUrl}"/></th>
			<th><rc:sort name="Status" displayName="" sortUrl="${listUrl}"/></th>
			</tr>-->
		</thead>
		<tbody>
        	<c:forEach var="cert" items="${certList.pageList}" varStatus="status">
            <tr>
            	<td>
                	${cert.name}
                </td>
             	<td>
                	${cert.revision}
             	</td>
             	<c:choose>
             	<c:when test="${user.canAdd || user.canEdit}">
	             	<c:choose>
		             	<c:when test="${cert.isAwarded}">
		             	<td>
		                	${cert.awardDate}
		                </td>
		                <td>
		                	<a href="<c:url value="/CertificateListController/printCert"><c:param name="certid" value="${cert.id}"/></c:url>" />
		                </td>
		                </c:when>
		                <c:otherwise>
		                <td>
		                	N/A
		                </td>
		                <td>
		                	<a href="<c:url value="/CertificateListController/checkStatus"><c:param name="certid" value="${cert.id}"/><c:param name="userid" value="${user.id}"/></c:url>" />
		                </td>
		                </c:otherwise>
	                </c:choose>
                </c:when>
                <c:otherwise>
	                <c:choose>
		             	<c:when test="${cert.isAwarded}">
		             	<td>
		                	${cert.awardDate}
		                </td>
		                <td>
		                	<a href="<c:url value="/CertificateListController/printCert"><c:param name="certid" value="${cert.id}"/></c:url>" />
		                </td>
		                </c:when>
		                <c:otherwise>
		                <td>
		                	N/A
		                </td>
		                <td>
		                	<a href="<c:url value="/CertificateListController/checkStatus"><c:param name="certid" value="${cert.id}"/><c:param name="userid" value="${user.id}"/></c:url>" />
		                </td>
		                </c:otherwise>
	                </c:choose>
                </c:otherwise>
                </c:choose>
             	<c:if test="${cert.status eq 'INCOMPLETE'}">
             		TODO
             	</c:if>
             	<c:if test="${cert.status eq 'COMPLETE'}">
             		TODO
             	</c:if>
             	</td>
          	</tr>
       		</c:forEach>
		</tbody>
	</table>
</c:if>
</div>

<script type="text/javascript">
	$(document).ready(function() {
		populateCertificateList();
	});
	
	//can choose to either populate entire data through jquery or use jstl core
	function populateCertificateList() {		
		$.getJSON("CertificateListController/list", function(certList) {
			$.each(certList.pageList,function(){
				$(#cList tbody).html(appendHtmlData(this));
			});
		
		});
	}		
			
</script>	