<%@ include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>
	<div>
		<h3><spring:message code="form.text.instruction"/></h3>
		<p><spring:message code="form.text.criteria.description"/></p>
	</div>
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
    <form:form id="createCertFormThree" modelAttribute="certificateToolState" method="POST" action="third.form">
		<div id="criteria" style="position:relative; margin-left:20px;  max-width:50%; display:inline-block;">
	        <form:hidden id="certId" path="certificateDefinition.id"/>
	        <div id="currentCriteria" style="margin-bottom:40px; ">
                <h3><spring:message code="form.text.criteria.awardCriteria"/></h3>
                <p><spring:message code="form.text.criteria.awardCriteria.instructions"/></p>
                <div id="criteriaList" style="margin-left:20px;">
		        <c:forEach items="${certificateToolState.certificateDefinition.awardCriteria}" var="criterion">
			         <div id="${criterion.id}" mergeItemCtriteriaTemplate="${criterion.itemId}${criterion.currentCriteriaTemplate}"  style="font-weight:bold; font-style:italic;">
				        ${criterion.expression}&nbsp;&nbsp;&nbsp;&nbsp;
			        	<a href="#" onclick="removeCriterion('${criterion.id}');">
			        		<spring:message code="form.text.criteria.remove"/>
			        	</a>
			        </div>
			    </c:forEach>
                </div>
	        </div>
	            <div id="newCriteriaForm" style="display:inline-block; background-color:#ddd; padding:10px">
                <h3><spring:message code="form.text.criteria.selectTemplate"/></h3>
                <select id="criteriaTemplate" onchange="completeCriterionForm(this.options[selectedIndex].value);">
                <c:forEach items="${certificateToolState.criteriaTemplates}" var="template">
                    <option value="${template.getClass().getName()}">${template.expression}</option>
                </c:forEach>
                </select>
                <h3><spring:message code="form.text.criteria.selectParameters"/></h3>
                <div id="criteriaOptions" style="margin-left:10%;"></div>
                <div id="addDiv" style="float:right">
                    <input id="add" type="button" value="<spring:message code="form.submit.add"/>" onclick="addCriterion()"/>
                </div>
            </div>
		</div>
		<div style="margin:5px">
			<input id="back" type="button" value="<spring:message code="form.submit.back"/>"/>&nbsp;
			<input id="save" type="button" value="<spring:message code="form.submit.saveProgress"/>"/>&nbsp;
			<input id="next" type="button" value="<spring:message code="form.submit.next"/>"/>&nbsp;&nbsp;&nbsp;&nbsp;
			<input id="cancel" type="button" value="<spring:message code="form.submit.cancel"/>"/>
			<form:hidden path="submitValue"/>
		</div>
	</form:form>	

<script type="text/javascript">

    var currentTemplateVariables = new Array();
    var noValuesMessage = "<spring:message code='form.text.novalues'/>";
    var mergeCreteriaItemCreteriaTemplate;

	$(document).ready(function() {

		completeCriterionForm($("#criteriaTemplate").val());
		
		$("#back").click(function() {
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
		$("#createCertFormThree").submit();
	}
	
    function save()
    {
		if(validateForm())
		{
			$("#submitValue").val("save");
			$("#createCertFormThree").submit();
		}
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
    	return true;
    }
    
    function removeCriterion(criterionId)
    {
        jQuery.ajax(
            {
                url: 'removeCriterion.form',
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
    	$("#"+criterion).remove();
    	resetHeight();
    }
    
    function appendCriterionToDiv (criterion)
    {
    	mergeCreteriaItemCreteriaTemplate=checkCriterion();
    	var divContent = "<div id='" + criterion.id + "' mergeItemCtriteriaTemplate='" + mergeCreteriaItemCreteriaTemplate + "'  style='font-weight:bold; font-style:italic;'>" + criterion.expression + "&nbsp;&nbsp;&nbsp;&nbsp;" +
                      "<a href='#' onclick=\"removeCriterion('" + criterion.id +
                      "')\"><spring:message code="form.text.criteria.remove"/></a></div>\n";

        $("#currentCriteria").append(divContent);
        resetHeight();
    }
    
    function checkCriterion() 
    {
		var selectedCreteriaItem=jQuery("#criteriaOptions > p > select :selected").val();
    	var selectedCreteriaTemplate=jQuery("#criteriaTemplate").val();
    	if(selectedCreteriaTemplate === "com.rsmart.certification.criteria.impl.gradebook.FinalGradeScoreCriteriaTemplate") {
    		mergeCreteriaItemCreteriaTemplate=selectedCreteriaTemplate;
    	} else {
    		mergeCreteriaItemCreteriaTemplate=selectedCreteriaItem+""+selectedCreteriaTemplate;
    	}
    	
    	return mergeCreteriaItemCreteriaTemplate;
    	
    }
    
    function addCriterion ()
    {
    	mergeCreteriaItemCreteriaTemplate=checkCriterion();
    	
    	var allCurrentCriterias=jQuery("#currentCriteria  div");
    	var i=1;
    	var flag=true;
		jQuery.each(allCurrentCriterias, function() {
			var  iterateEachDivInCriteriaList= jQuery(this).attr('mergeItemCtriteriaTemplate');
			
				if(iterateEachDivInCriteriaList === mergeCreteriaItemCreteriaTemplate) {
					flag=false;
					}
				
			if(!flag)
			  return false;
			i++;	
 		 })
 	
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
                url: 'addCriterion.form',
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

                    var match = patt.exec(xhr.response);

                    if (match != null)
                    {
                        $("#submitError").html(match[1]).show();
                        resetHeight();
                    }
                    else
                    {
                	    $("#submitError").html("<spring:message code='form.error.criteriaProcessingError' />").show();
                	    resetHeight();
                    }
                }
            }
        );
        
        } else {
        	  $("#submitError").html("<spring:message code='form.error.criteriaExist' /> ").show();
        }
    }

    function createMultipleChoiceVariable (varKey, varLabel, values)
    {
        var selectElement;

        for (var value in values)
        {
            selectElement += '<option value="' + value + '">' + values[value] + '</option>'
        }

        if (undefined == selectElement)
        {
            selectElement = '<p>' + noValuesMessage + '</p>';

            selectElement = selectElement.replace('{0}', varLabel);

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
          url: "getTemplate.form",
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
                      templateHtml += createMultipleChoiceVariable (templateVariable.variableKey,
                                                                    templateVariable.variableLabel,
                                                                    templateVariable.values);
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
<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
