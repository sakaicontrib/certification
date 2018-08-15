<%@ include file="/jsp/include.jsp" %>
<jsp:include page="/jsp/header.jsp" />
<%@ include file="/jsp/adminActionToolBar.jsp" %>

<form:form id="createCertFormFour" modelAttribute="certificateToolState" action="fourth.form">
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
        <spring:message code="form.text.review.description" />
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
    <div class="form-group row">
        <label class="col-sm-12 form-control-label block">
            <spring:message code="form.label.name" />:
        </label>
        <span class="col-sm-12">
            <c:out value="${certificateToolState.certificateDefinition.name}"></c:out>
        </span>
    </div>
    <div class="form-group row">
        <label class="col-sm-12 form-control-label block">
            <spring:message code="form.label.description" />:
        </label>
        <span class="col-sm-12">
            <c:out value="${certificateToolState.certificateDefinition.description}"></c:out>
        </span>
    </div>
    <c:if test="${not empty certificateToolState.certificateDefinition.expiryOffset}">
        <div class="form-group row">
            <label class="col-sm-12 form-control-label block">
                <spring:message code="form.label.expiryOffset1" />:
            </label>
            <span class="col-sm-12">
                ${certificateToolState.certificateDefinition.expiryOffset}&nbsp;
                <spring:message code="form.label.expiryOffset2" />
            </span>
        </div>
    </c:if>
    <div class="form-group row">
        <label class="col-sm-12 form-control-label block">
            <spring:message code="form.label.templatefile" />
        </label>
        <span class="col-sm-12">
            <c:choose>
                <c:when test="${certificateToolState.templateFilename != null}">
                    <span>${certificateToolState.templateFilename}</span>
                </c:when>
            </c:choose>
        </span>
    </div>
    <div class="form-group row">
        <label class="col-sm-12 form-control-label block">
            <spring:message code="form.label.criteria" />
        </label>
        <span class="col-sm-12">
            <ul>
                <c:forEach items="${certificateToolState.certificateDefinition.awardCriteria}" var="criteria">
                    <li>${criteria.expression}</li>
                </c:forEach>
            </ul>
        </span>
    </div>
    <div class="form-group row">
        <label class="col-sm-12 form-control-label block">
            <spring:message code="form.label.hideRequirements" />
        </label>
        <span class="col-sm-12">
            <c:choose>
                <c:when test="${certificateToolState.certificateDefinition.progressHidden}">
                    <spring:message code="form.label.hideRequirements.yes" />
                </c:when>
                <c:otherwise>
                    <spring:message code="form.label.hideRequirements.no" />
                </c:otherwise>
            </c:choose>
        </span>
    </div>
    <div class="form-group row">
        <label class="col-sm-12 form-control-label block">
            <spring:message code="form.label.fieldValues" />
        </label>
        <span class="col-sm-12">
            <div>
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                        <tr>
                            <th><spring:message code="form.label.field"/></th>
                            <th><spring:message code="form.label.value"/></th>
                            <th><spring:message code="form.label.overwrite"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${certificateToolState.fieldToDescription}" var="tField" >
                            <tr>
                                <td>${tField.key}</td>
                                <td>${tField.value}</td>
                                <c:if test="${tField.value == 'unassigned'}">
                                    <td><form:input path="templateFields['${tField.key}']" autocomplete="off"/></td>
                                </c:if>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </span>
    </div>
    <div>
        <input id="save" type="button" value="<spring:message code='form.submit.activateCert' />" />
        <input id="back" type="button" value="<spring:message code='form.submit.back' />" />
        <input id="cancel" type="button" value="<spring:message code='form.submit.cancel' />" />
        <form:hidden path="submitValue" />
    </div>
</form:form>

<script type="text/javascript">
    $(document).ready(function() {
        $("#back").click(function(){
            back();
        });

        $("#save").click(function() {
            save();
        });

        $("#cancel").click(function() {
            cancel();
        });
    });

    function back() {
        SPNR.disableControlsAndSpin(this, null);
        $("#submitValue").val("back");
        $("#createCertFormFour").submit();
    }

    function save() {
        SPNR.disableControlsAndSpin(this, null);
        $("#submitValue").val("save");
        $("#createCertFormFour").submit();
    }

    function cancel() {
        SPNR.disableControlsAndSpin(this, null);
        $("#submitValue").val("cancel");
        $("#createCertFormFour").submit();
    }
</script>
<%@ include file="/jsp/footer.jsp" %>
