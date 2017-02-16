<%@ include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>

<div id="header" style="display:inline-block">
	<div>
		<h3><spring:message code="print.title"/></h3>
		<p><spring:message code="print.congratulations"/></p>
	</div>
	<div>
    <table class="listHier lines nolines">
    	<thead>
    		<tr>
    			<th><spring:message code="form.label.certificate" /></th>
    			<th><spring:message code="form.label.certificate.description" /></th>
    			<th><spring:message code="form.label.awarded" /></th>
    		</tr>
    	</thead>
    	<tbody>
    		<tr>
    			<td>${award.certificateDefinition.name}</td>
    			<td>${award.certificateDefinition.description}</td>
    			<td>${award.formattedCertificationTimeStamp}</td>
    		</tr>
    	</tbody>
    </table>
    </div>
    <div style="margin:5px">
	    <form:form id="printForm" action="print.form" method="POST">
	        <input type="hidden" id="certId" name="certId" value="${award.certificateDefinition.id}"/>
	        <input id="print" type="submit" value="<spring:message code='form.submit.print' />" onclick="SPNR.disableControlsAndSpin( this, null );" />
	        <input id="cancel" type="button" value="<spring:message code='form.submit.cancel' />" onclick="SPNR.disableControlsAndSpin( this, null );" />
	    </form:form>
	</div>
</div>
<script type="text/javascript">
	$(document).ready(function() {

		loaded();

		$("#cancel").click( function(){
			location.href="list.form";
			return false;
		});
	});
</script>
