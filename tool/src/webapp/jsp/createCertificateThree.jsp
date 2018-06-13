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
    <p class="instruction">
        <spring:message code="form.text.fields.description" />
    </p>
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
    <div id="tabledata">
        <table id="tFList" class="table table-hover table-striped table-bordered" summary="Template Fields">
            <thead>
                <tr>
                    <th><spring:message code="form.label.field"/></th>
                    <th><spring:message code="form.label.value"/><span class="reqStarInline">*</span></th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${certificateToolState.escapedFieldValues}" var="tField" varStatus="index">
                    <tr>
                        <td>${tField.key}</td>
                        <td>
                            <form:select path="templateFields['${tField.key}']">
                                <c:if test="${not empty tField.value}">
                                    <c:forEach items="${certificateToolState.templateFieldsToDescriptions}" var="predefDefault" varStatus="index">
                                        <c:if test="${tField.key eq predefDefault.key}">
                                            <form:option value="${tField.value}" label="${predefDefault.value}" />
                                        </c:if>
                                    </c:forEach>
                                </c:if>
                                <c:forEach items="${certificateToolState.orderedEscapedPredifinedFields}" var="escapedPredefField" varStatus="index">
                                    <c:if test="${tField.value ne escapedPredefField[0]}">
                                        <form:option value="${escapedPredefField[0]}" label="${escapedPredefField[1]}" />
                                    </c:if>
                                </c:forEach>
                            </form:select>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
    <div>
        <input id="continue" type="button" value="<spring:message code='form.submit.continue' />" />
        <input id="back" type="button" value="<spring:message code='form.submit.back' />" />
        <input id="cancel" type="button" value="<spring:message code='form.submit.cancel' />" />
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
    });

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
            if (elements[i].value === "${certificateToolState.unassignedValue}") {
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
