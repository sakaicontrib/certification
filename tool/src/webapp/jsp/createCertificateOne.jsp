<%@ include file="/jsp/include.jsp" %>
<jsp:include page="/jsp/header.jsp" />
<%@ include file="/jsp/adminActionToolBar.jsp" %>

<form:form id="createCertFormOne" modelAttribute="certificateToolState" action="first.form" enctype="multipart/form-data">
    <div class="page-header">
        <c:choose>
            <c:when test="${certificateToolState.certificateDefinition.id == null}">
                <h1><spring:message code="form.add.title" /></h1>
            </c:when>
            <c:otherwise>
                <h1><spring:message code="form.modify.title" /></h1>
            </c:otherwise>
        </c:choose>
    </div>
    <p class="instruction">
        <spring:message code="form.text.create.description" />
    </p>
    <div id="submitError" class="alertMessage hidden"></div>
    <c:if test="${statusMessageKey != null}" >
        <div id="statusMessageKey" class="alertMessage" >
            <spring:message code="${statusMessageKey}" />
        </div>
    </c:if>
    <c:if test="${errorMessage != null}">
        <div id="errorMessage" class="alertMessage">
            <spring:message code="${errorMessage}" arguments="${errorArguments}" />
        </div>
    </c:if>

    <div class="form-group row form-required">
        <form:label path="certificateDefinition.name" class="col-sm-12 form-control-label block">
            <b><spring:message code="form.label.name" /></b>
        </form:label>
        <div class="col-sm-6">
            <form:input path="certificateDefinition.name" id="name" type="text" class="form-control" />
        </div>
    </div>
    <div class="form-group row" class="col-sm-12 form-control-label block">
        <form:label path="certificateDefinition.description" class="col-sm-12 form-control-label block">
            <b><spring:message code="form.label.description" /></b>
        </form:label>
        <div class="col-sm-6">
            <form:textarea id="description" path="certificateDefinition.description" type="text" rows="7" class="form-control" />
        </div>
    </div>

    <table>
        <tbody>
            <tr>
                <td>
                    <c:if test="${certificateToolState.templateFilename != null}">
                        <spring:message code="form.label.currentFile" />
                        <c:out value="${certificateToolState.templateFilename}" /><br/>
                        <form:hidden id="currentTemplate" path="templateFilename" />
                    </c:if>
                    <input id="templateFile" type="file" name="newTemplate" accept="application/pdf" />
                    <span class="mimetype"><spring:message code="form.label.mimeTypes" arguments="${certificateToolState.mimeTypes}" /></span>
                </td>
            </tr>
        </tbody>
    </table>

    <div>
        <input id="continue" type="button" value="<spring:message code='form.submit.continue' />" />
        <input id="cancel" type="button" value="<spring:message code='form.submit.cancel' />" />
        <form:hidden path="submitValue" />
    </div>
</form:form>

<script type="text/javascript">
    $(document).ready(function() {
        $("#name").attr("placeholder", "<spring:message code='form.label.name'/>");
        $("#description").attr("placeholder", "<spring:message code='form.label.description'/>");

        $("#continue").click(function() {
            next();
        });

        $("#cancel").click(function() {
            cancel();
        });
    });

    function cancel() {
        SPNR.disableControlsAndSpin( this, null );
        $("#submitValue").val("cancel");
        $("#createCertFormOne").submit();
    }

    function next() {
        if(validateForm()) {
            SPNR.disableControlsAndSpin( this, null );
            $("#submitValue").val("next");
            $("#createCertFormOne").submit();
        }
    }

    function validateForm() {
        $(".alertMessage").addClass("hidden");
        var error = false;
        var errors = [];

        if (!$("#name").val()) {
            var $errDiv = $("<p/>", {
                text: '<spring:message code="form.error.namefield"/>',
            });
            errors.push($errDiv);
            error = true;
        }

        var currentTemplateVal = $("#currentTemplate").val();
        var templateFileVal = $("#templateFile").val();

        if ((currentTemplateVal !== undefined && !currentTemplateVal.endsWith(".pdf")) ||
            (currentTemplateVal !== undefined && templateFileVal != "" && !templateFileVal.endsWith(".pdf")) ||
            (currentTemplateVal === undefined && !templateFileVal.endsWith(".pdf"))) {
            var $errDiv = $("<p/>", {
                text: '<spring:message code="form.error.templateField"/>',
            });
            errors.push($errDiv);
            error = true;
        }

        if (error) {
            var $errorsDiv = $("#submitError");
            $errorsDiv.empty();
            $(errors).each(function(index, $errDiv) {
                $errorsDiv.append($errDiv);
            });
            $errorsDiv.removeClass("hidden");
            return false;
        }

        return true;
    }
</script>
<%@ include file="/jsp/footer.jsp" %>
