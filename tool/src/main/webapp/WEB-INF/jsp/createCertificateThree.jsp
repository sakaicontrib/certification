<%@ include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>
	<form:form id="createCertFormThree" modelAttribute="certificateToolState" action="third.form">
		<h3><spring:message code="form.text.instruction"/></h3	>
		<p><spring:message code="form.text.fields.description"/></p>
		<div id="submitError" class="alertMessage" style="display:none"></div>
		<c:if test="${statusMessageKey != null}" >
			<div id="statusMessageKey" class="alertMessage" >
				<spring:message code="${statusMessageKey}"/>
			</div>
		</c:if>
		<c:if test="${errorMessage != null}" >
			<div id="errorMessage" class="alertMessage" >
				<spring:message code="${errorMessage}"/>
			</div>
		</c:if>
		<div style="position:relative; display:inline-block; margin-left:20px">
			<div id="tabledata" style="position:relative; float:left; max-width:30%; display:block">
				<table id="tFList" class="listHier lines nolines" summary="Template Fields">
					<thead>
						<tr>
							<th><spring:message code="form.label.field"/></th>
							<th><spring:message code="form.label.value"/><span class="reqStarInline">*</span></th>
						</tr>
					</thead>
					<tbody>
						<c:forEach items="${certificateToolState.templateFields}" var="tField" varStatus="index">
							<tr>
								<td>${tField.key}</td>
								<td>
									<form:select path="templateFields['${tField.key}']">
										<% /* k, so if tField.key is DateOfIssue (comes from the PDF), then java code has to look it up and find that it maps to ${cert.date}, and convert that to the pretty message*/ %>
										<% // <form:option value="0" label="${certificateToolState.getSelectedPredefField(tField.key)}"/> %>
										<form:options items="${certificateToolState.predifinedFields}"/>
									</form:select>
								</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</div>
		<div style="display:block; position:relative; margin:5px">
			<input id="back" type="button" value="<spring:message code="form.submit.back" />" />&nbsp;
			<input id="next" type="button" value="<spring:message code="form.submit.next"/>"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<input id="cancel" type="button" value="<spring:message code="form.submit.cancel"/>"/>
			<form:hidden path="submitValue" />
		</div>
	</div>
	</form:form>
</div>

<script type="text/javascript">

	$(document).ready(function() {

		loaded();
		
		$("#back").click(function() {
			back();
		});
	
		$("#next").click(function() {
			next();
		});
		
		$("#cancel").click(function() {
			cancel();
		});

	});

	function back()
	{
		$("#submitValue").val("back");
		$("#createCertFormThree").submit();
	}

	function next()
	{
		if(validateForm())
		{
			$("#submitValue").val("next");
			$("#createCertFormThree").submit();
		}
	}

	function cancel()
	{
		$("#submitValue").val("cancel");
		$("#createCertFormThree").submit();
	}

	function validateForm() 
	{
		$(".alertMessage").hide();
		var error = false;
		var errHtml = "";

		if(!$("select").val()) {
			errHtml = errHtml + "<spring:message code="form.error.fieldValue"/>" + "</br>" ;
			error = true;
		}

		if(error)
		{
			$("#submitError").html(errHtml).show();
			resetHeight();
			return false;
		}
		else
		{
			return true;
		}
	}

</script>
