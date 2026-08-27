<%@ include file="/jsp/include.jsp" %>
<jsp:include page="/jsp/header.jsp" />
<%@ include file="/jsp/adminActionToolBar.jsp" %>

<form:form id="createCertFormThree" modelAttribute="certificateToolState" action="third.form">
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
    <p id="fieldValueHelp" class="instruction">
        <spring:message code="form.text.fields.description" />
    </p>
    <spring:message code="form.label.customText" var="customTextLabel" />
    <div id="submitError" class="alertMessage hidden"></div>
    <c:if test="${statusMessageKey != null}">
        <div id="statusMessageKey" class="alertMessage">
            <spring:message code="${statusMessageKey}" />
        </div>
    </c:if>
    <c:if test="${errorMessage != null}">
        <div id="errorMessage" class="alertMessage">
            <spring:message code="${errorMessage}" />
        </div>
    </c:if>
    <form:errors path="templateFields" cssClass="alertMessage" element="div" />
    <form:errors path="customTemplateFields" cssClass="alertMessage" element="div" />
    <div id="tabledata">
        <table id="tFList" class="table table-hover table-striped table-bordered" summary="Template Fields">
            <thead>
                <tr>
                    <th><spring:message code="form.label.field"/></th>
                    <th><spring:message code="form.label.value"/><span class="reqStarInline">*</span></th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${certificateToolState.templateFields}" var="tField" varStatus="index">
                    <tr>
                        <td>
                            <form:label path="templateFields['${tField.key}']" for="templateFieldValue${index.index}">
                                <c:out value="${tField.key}" />
                            </form:label>
                        </td>
                        <td>
                            <form:select path="templateFields['${tField.key}']"
                                         id="templateFieldValue${index.index}"
                                         class="form-control certificateFieldValue"
                                         aria-controls="customTemplateField${index.index}"
                                         aria-describedby="fieldValueHelp">
                                <c:forEach items="${certificateToolState.orderedEscapedPredifinedFields}" var="escapedPredefField">
                                    <form:option value="${escapedPredefField[0]}" label="${escapedPredefField[1]}" />
                                </c:forEach>
                                <form:option value="${certificateToolState.customFieldValue}" label="${customTextLabel}" />
                            </form:select>
                            <div id="customTemplateField${index.index}" class="customTemplateField mt-2">
                                <form:label path="customTemplateFields['${tField.key}']"
                                            for="customTemplateFieldValue${index.index}">
                                    <spring:message code="form.label.customTextFor" arguments="${tField.key}" htmlEscape="true" />
                                </form:label>
                                <form:input path="customTemplateFields['${tField.key}']"
                                            id="customTemplateFieldValue${index.index}"
                                            class="form-control"
                                            maxlength="${certificateToolState.maxFieldValueLength}"
                                            aria-describedby="fieldValueHelp"
                                            autocomplete="off" />
                            </div>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
    <div class="my-2">
        <input id="continue" class="btn btn-primary" type="button" value="<spring:message code='form.submit.continue' />" />
        <input id="back" class="btn" type="button" value="<spring:message code='form.submit.back' />" />
        <input id="cancel" class="btn" type="button" value="<spring:message code='form.submit.cancel' />" />
        <form:hidden path="submitValue" />
    </div>
</form:form>

<script type="text/javascript">
    $(document).ready(function() {
        $("#back").click(function() {
            back();
        });

        $("#continue").click(function() {
            next();
        });

        $("#cancel").click(function() {
            cancel();
        });

        $(".certificateFieldValue").each(function() {
            toggleCustomField(this);
        }).change(function() {
            toggleCustomField(this);
        });
    });

    function toggleCustomField(selectElement) {
        var customField = $(selectElement).siblings(".customTemplateField");
        var showCustomField = selectElement.value === "${certificateToolState.customFieldValue}";
        customField.toggle(showCustomField);
        $(selectElement).attr("aria-expanded", showCustomField);
    }

    function back() {
        SPNR.disableControlsAndSpin(this, null);
        $("#submitValue").val("back");
        $("#createCertFormThree").submit();
    }

    function next() {
        if(checkUnassigned()) {
            SPNR.disableControlsAndSpin(this, null);
            $("#submitValue").val("next");
            $("#createCertFormThree").submit();
        }
    }

    function cancel() {
        SPNR.disableControlsAndSpin(this, null);
        $("#submitValue").val("cancel");
        $("#createCertFormThree").submit();
    }

    function checkUnassigned() {
        var unassignedVals = false;
        var elements = $('select[name^="templateFields"]');

        for (var i = 0; i < elements.length; i++) {
            var customValueIsEmpty = elements[i].value === "${certificateToolState.customFieldValue}"
                && !$(elements[i]).siblings(".customTemplateField").find("input").val().trim();
            if (elements[i].value === "${certificateToolState.unassignedValue}" || customValueIsEmpty) {
                unassignedVals = true;
            }
        }

        if (unassignedVals) {
            return confirm("<spring:message code="form.text.unassigned.confirm" />");
        }

        return true;
    }
</script>
<%@ include file="/jsp/footer.jsp" %>
