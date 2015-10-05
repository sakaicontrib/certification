<%@ include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>
	<form:form id="createCertFormTwo" modelAttribute="certificateToolState" action="second.form">
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
								<td><form:input path="templateFields['${tField.key}']"/></td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
			<div id="fieldList" style="position:relative; float:left; display:block; padding:5px; margin-left:100px; max-width:30%; border:thin solid silver; background-color:#ddd;">
				<h3><spring:message code="form.label.variables"/></h3></br>
				<p><spring:message code="variable.description"/></p>
				<c:forEach items="${certificateToolState.predifinedFields}" var="preDefVar">
					${preDefVar.key}&nbsp;&#045;&nbsp;${preDefVar.value}</br>
				</c:forEach>
			</div>
		</div>
		<div style="display:block; position:relative; margin:5px">
			<input id="back" type="button" value="<spring:message code="form.submit.back" />" />&nbsp;
			<input id="save" type="button" value="<spring:message code="form.submit.saveProgress"/>"/>&nbsp;
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
		
		$("#back").click(function(){
			back();
		});
		
		$("#save").click(function() {
			save();
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
		$("#createCertFormTwo").submit();
	}
	
	function save()
	{
		if(validateForm())
		{
			$("#submitValue").val("save");
			$("#createCertFormTwo").submit();
		}
	}
	
	function next()
	{
		if(validateForm())
		{
			$("#submitValue").val("next");
			$("#createCertFormTwo").submit();
		}
	}
	
	function cancel()
	{
		$("#submitValue").val("cancel");
		$("#createCertFormTwo").submit();
	}
	
	function validateForm() 
	{
		$(".alertMessage").hide();
		var error = false;
		var errHtml = "";
		
		if(!$("input:text").val()) {
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