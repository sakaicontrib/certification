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
                        <c:forEach items="${certificateToolState.escapedFieldValues}" var="tField" varStatus="index">
                            <tr>
                                <td>${tField.key}</td>
                                <td>
                                    <form:select path="templateFields['${tField.key}']">
                                        <c:forEach items="${certificateToolState.templateFieldsToDescriptions}" var="predefDefault" varStatus="index">
                                            <c:if test="${tField.key eq predefDefault.key}">
                                                <form:option value="${tField.value}" label="${predefDefault.value}"/>
                                            </c:if>
                                        </c:forEach>
                                        <c:forEach items="${certificateToolState.orderedEscapedPredifinedFields}" var="escapedPredefField" varStatus="index">
                                            <c:if test="${tField.value ne escapedPredefField[0]}">
                                                <form:option value="${escapedPredefField[0]}" label="${escapedPredefField[1]}"/>
                                            </c:if>
                                        </c:forEach>
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
        if(checkUnassigned())
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

    function checkUnassigned()
    {
        var unassignedVals = false;
        var elements = $('select[name^="templateFields"]');

        for (var i = 0; i < elements.length; i++)
        {
            if (elements[i].value === "${certificateToolState.unassignedValue}")
            {
                unassignedVals = true;
            }
        }

        if (unassignedVals)
        {
            return confirm("<spring:message code="form.text.unassigned.confirm" />");
        }

        return true;
    }
</script>
<%@ include file="/WEB-INF/jsp/footer.jsp" %>
