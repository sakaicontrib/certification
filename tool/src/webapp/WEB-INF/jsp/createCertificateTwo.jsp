<%@ include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>
<%@ include file="/WEB-INF/jsp/adminActionToolBar.jsp" %>

<div class="page-header">
    <c:choose>
        <c:when test="${certificateToolState.certificateDefinition.id == null}">
            <h1><spring:message code="form.add.title"/></h1>
        </c:when>
        <c:otherwise>
            <h1><spring:message code="form.modify.title"/></h1>
        </c:otherwise>
    </c:choose>
</div>
<p class="instruction">
    <spring:message code="form.text.criteria.description"/>
</p>
<div id="submitError" class="alertMessage" style="display:none"></div>
<c:if test="${statusMessageKey != null}" >
    <div id="statusMessageKey" class="alertMessage" >
        <spring:message code="${statusMessageKey}"/>
    </div>
</c:if>
<c:if test="${errorMessage != null}" >
    <div id="errorMessage" class="alertMessage" >
        <spring:message code="${errorMessage}" />
    </div>
    </c:if>
<form:form id="createCertFormTwo" modelAttribute="certificateToolState" method="POST" action="second.form">
    <div id="criteria">
        <form:hidden id="certId" path="certificateDefinition.id"/>
        <div id="currentCriteria" style="margin-bottom:30px;">
            <h4><spring:message code="form.text.criteria.awardCriteria"/></h4>
            <div id="currentCriteriaBox">
                <c:choose>
                    <c:when test="${empty certificateToolState.certificateDefinition.awardCriteria}">
                        <p id="removeInstructions" style="display:none"><spring:message code="form.text.criteria.awardCriteria.instructions"/></p>
                        <p id="noCriteria"><spring:message code="form.text.criteria.awardCriteria.nocriteria"/></p>
                    </c:when>
                    <c:otherwise>
                        <p id="removeInstructions"><spring:message code="form.text.criteria.awardCriteria.instructions"/></p>
                        <p id="noCriteria" style="display:none"><spring:message code="form.text.criteria.awardCriteria.nocriteria"/></p>
                    </c:otherwise>
                </c:choose>
                <ul id="criteriaList" style="margin-left:20px;">
                <c:forEach items="${certificateToolState.certificateDefinition.awardCriteria}" var="criterion">
                    <li id="crit_${criterion.id}" mergeItemCriteriaTemplate="${criterion.itemId}${criterion.currentCriteriaTemplate}">
                        ${criterion.expression}
                        <a href="#" onclick="removeCriterion('${criterion.id}');" title="<spring:message code='form.text.criteria.remove'/>">
                            <span class="sr-only">
                                <spring:message code="form.text.criteria.remove"/>
                            </span>
                            <span class="fa fa-lg fa-fw fa-trash" aria-hidden="true"></span>
                        </a>
                    </li>
                </c:forEach>
                </ul>
            </div>
        </div>
        <div id="newCriteriaForm" style="display:inline-block; padding:10px">
            <h4><spring:message code="form.text.criteria.selectTemplate"/></h4>
            <select id="criteriaTemplate" onchange="completeCriterionForm(this.options[selectedIndex].value);">
            <c:forEach items="${certificateToolState.criteriaTemplates}" var="template">
                <option value="${template['class'].name}">${template.expression}</option>
            </c:forEach>
            </select>
            <h4><spring:message code="form.text.criteria.selectParameters"/></h4>
            <div id="criteriaOptions"></div>
            <div id="createDiv">
                <input id="create" type="button" value='<spring:message code="form.submit.add"/>' onclick="addCriterion()"/>
            </div>
        </div>
        <br/>
        <br/>
        <label>
            <form:checkbox path="certificateDefinition.progressShown"/>
            <spring:message code="form.label.showRequirements"/>
        </label>
        <br/>
        <br/>
    </div>
    <div>
        <input id="back" type="button" value="<spring:message code='form.submit.back' />" />
        <input id="next" type="button" value="<spring:message code='form.submit.next' />" />
        <input id="cancel" type="button" value="<spring:message code='form.submit.cancel' />" />
        <form:hidden path="submitValue" />
    </div>
</form:form>

<script type="text/javascript">

    var currentTemplateVariables = new Array();
    var noValuesMessage = "<spring:message code='form.text.emptyGradebook'/>";
    var mergeCreteriaItemCreteriaTemplate;

    $(document).ready(function() {

        completeCriterionForm($("#criteriaTemplate").val());

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
        SPNR.disableControlsAndSpin( this, null );
        $("#submitValue").val("back");
        $("#createCertFormTwo").submit();
    }

    function next()
    {
        SPNR.disableControlsAndSpin( this, null );
        $("#submitValue").val("next");
        $("#createCertFormTwo").submit();
    }

    function cancel()
    {
        SPNR.disableControlsAndSpin( this, null );
        $("#submitValue").val("cancel");
        $("#createCertFormTwo").submit();
    }

    function removeCriterion(criterionId)
    {
        jQuery.ajax(
            {
                url: '${toolUrl}/removeCriterion.form',
                data:
                    {
                        certId: $("#certId").val(),
                        criterionId: criterionId
                    },
                dataType: 'json',
                type: 'POST',
                success: function (data, textStatus, jqXHR)
                {
                    removeCriterionFromDiv(data);
                },
                error: function(foo, bar, baz)
                {
                    $("#submitError").html("<spring:message code='form.error.criteriaProcessingError' />").show();
                    resetHeight();
                }
            }
        );
    }

    function removeCriterionFromDiv(criterion)
    {
        $("#crit_"+criterion).remove();
        var $criteria = $("li[id^='crit_']");
        if ($criteria.length === 0)
        {
            $("#removeInstructions").attr('style', 'display:none');
            $("#noCriteria").attr('style', '');
        }
        resetHeight();
    }

    function appendCriterionToDiv (criterion)
    {
        mergeCriteriaItemCriteriaTemplate=checkCriterion();
        var liContent = "<li id='crit_" + criterion.id + "' mergeItemCriteriaTemplate='" + mergeCriteriaItemCriteriaTemplate + "'>" + criterion.expression +
                        "<a href='#' onclick=\"removeCriterion('" + criterion.id +
                        "')\" title='<spring:message code="form.text.criteria.remove"/>'><span class='sr-only'><spring:message code="form.text.criteria.remove"/></span><span class='fa fa-lg fa-fw fa-trash' aria-hidden='true'></span></a></li>\n";

        $("#criteriaList").append(liContent);
        $("#removeInstructions").attr('style', '');
        $("#noCriteria").attr('style', 'display:none');
        resetHeight();
    }

    function checkCriterion()
    {
        var selectedCreteriaItem=jQuery("#criteriaOptions > p > select :selected").val();
        var selectedCreteriaTemplate=jQuery("#criteriaTemplate").val();
        if(selectedCreteriaTemplate === "org.sakaiproject.certification.criteria.impl.gradebook.FinalGradeScoreCriteriaTemplate") {
            mergeCriteriaItemCriteriaTemplate=selectedCreteriaTemplate;
        } else {
            mergeCriteriaItemCriteriaTemplate=selectedCreteriaItem+""+selectedCreteriaTemplate;
        }

        return mergeCriteriaItemCriteriaTemplate;
    }

    function addCriterion ()
    {
        mergeCriteriaItemCriteriaTemplate=checkCriterion();

        var allCurrentCriterias=jQuery("#currentCriteria  div");
        var i=1;
        var flag=true;
        jQuery.each(allCurrentCriterias, function() {
            var iterateEachDivInCriteriaList= jQuery(this).attr('mergeItemCriteriaTemplate');

                if(iterateEachDivInCriteriaList === mergeCriteriaItemCriteriaTemplate) {
                    flag=false;
                }

            if(!flag)
            {
                return false;
            }
            i++;
        });

        var values = {};
        $(".alertMessage").hide();

        /*
           this is divided into two .each() calls because I couldn't come up with the right selector
           to get both the <select> and the <input> elements
         */
        jQuery.each ($('#criteriaOptions > p > input'), function (index, val)
        {
            values[val.id] = val.value;
        });
        jQuery.each ($('#criteriaOptions > p > select'), function (index, val)
        {
            values[val.id] = val.value;
        });


        if(flag){
            jQuery.ajax(
            {
                url: '${toolUrl}/addCriterion.form',
                data: {
                    certId : $("#certId").val(),
                    templateId : $("#criteriaTemplate").val(),
                    variableValues : values
                },
                dataType: 'json',
                type: 'POST',
                success: function (data, textStatus, jqXHR)
                {
                    appendCriterionToDiv(data);
                },
                error: function (xhr, status, errorThrown)
                {
                    var patt = new RegExp("ERROR_MESSAGE(.*?)/ERROR_MESSAGE");

                    var match = patt.exec(xhr.responseText);

                    if (match !== null)
                    {
                        $("#submitError").html(match[1]).show();
                        resetHeight();
                    }
                    else if( xhr.responseText.indexOf( "**TooManyExpiry**" ) !== -1 )
                    {
                        $("#submitError").html("<spring:message code='form.expiry.tooMany' />").show();
                        resetHeight();
                    }
                    else
                    {
                        $("#submitError").html("<spring:message code='form.error.criteriaProcessingError' />").show();
                        resetHeight();
                    }
                }
            });

        } else {
            $("#submitError").html("<spring:message code='form.error.criteriaExist' /> ").show();
        }
    }

    function createMultipleChoiceVariable (varKey, varLabel, values)
    {
        var selectElement;

        for (var value in values)
        {
            selectElement += '<option value="' + value + '">' + values[value] + '</option>';
        }

        if (undefined === selectElement)
        {
            return selectElement;
        }

        selectElement = '<p>' + varLabel + ': <select id="' + varKey + '" name="' + varKey + '">'
                        + selectElement + '</select></p>';

        return selectElement;
    }

    function createTextVariable (key, label)
    {
        return '<p>' + label + ': <input id="' + key + '" type="text" name="' + key + '"></input></p>\n';
    }

    function completeCriterionForm(selectedTemplate)
    {
        jQuery.ajax({
            url: '${toolUrl}/getTemplate.form',
            data: { templateId : selectedTemplate },
            dataType: 'json',
            type: 'POST',
            success: function(data, textStatus, jqXHR)
            {
                var templateHtml = "";

                currentTemplateVariables = new Array();

                for (var i = 0; i < data.templateVariables.length; i++)
                {
                    var templateVariable = data.templateVariables[i];

                    currentTemplateVariables[i] = templateVariable.variableLabel;

                    if (templateVariable.multipleChoice)
                    {
                        var multChoiceVar = createMultipleChoiceVariable (templateVariable.variableKey, templateVariable.variableLabel, templateVariable.values);
                        if (undefined === multChoiceVar)
                        {
                            templateHtml += '<p>' + data.message + '</p>';
                        }
                        else
                        {
                            templateHtml += multChoiceVar;
                        }
                    }
                    else
                    {
                        templateHtml += createTextVariable (templateVariable.variableKey,
                                                            templateVariable.variableLabel);
                    }
                }

                $('#criteriaOptions').html(templateHtml);

                resetHeight();
            },
            error: function (xhr, status, errorThrown)
            {
                $("#submitError").html("<spring:message code='form.error.criteriaProcessingError' />").show();
                resetHeight();
            }
        });
    }

</script>
<%@ include file="/WEB-INF/jsp/footer.jsp" %>
