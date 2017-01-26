<%@ include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>
	<form:form id="certList" method="POST">
		<c:choose>
		<c:when test="${empty certList}">
		<div class="navIntraTool">
			<a href="" id="Add"><spring:message code="form.menu.add"/></a>
		</div>
		<div style="position:relative; float:left; display:inline-block;">
	 		<spring:message code="form.text.emptycertlist"/></br>
	 	</div>
		</c:when>
		<c:otherwise>
			<div class="navIntraTool">
				<a href="" id="Add"><spring:message code="form.menu.add"/></a>&nbsp;
				<a href="" id="Edit"><spring:message code="form.menu.edit"/></a>
                <a href="" id="Delete"><spring:message code="form.menu.delete"/></a>
			</div>
			<div id="submitError" class="alertMessage" style="display:none"></div>
            <c:if test="${errorMessage != null}" >
                <div id="errorMessage" class="alertMessage" >
                    <spring:message code="${errorMessage}" />
                </div>
            </c:if>
			<div class="listNav">
				<div class="pager">
					<span style="align:center"><spring:message code="form.pager.showing"/>&nbsp;<c:out value="${firstElement}" />&nbsp;&#045;&nbsp;<c:out value="${lastElement}" />&nbsp;of&nbsp;${certList.nrOfElements}</span></br>
					<c:choose>
						<c:when test="${!certList.firstPage}">
							<input type="button" id="first" value="<spring:message code="pagination.first"/>" />&nbsp;
							<input type="button" id="prev" value="<spring:message code="pagination.previous"/>" />
						</c:when>
						<c:otherwise>
							<input type="button" id="nofirst" value="<spring:message code="pagination.first"/>" disabled="disabled" />&nbsp;
							<input type="button" id="noPrev" value="<spring:message code="pagination.previous"/>" disabled="disabled" />
						</c:otherwise>
					</c:choose>
					<input type="hidden" id="pageNo" value="${pageNo}" />
					<select id="pageSize">
						<c:forEach items="${pageSizeList}" var="list">
							<c:choose>
							<c:when test="${list > 100}">
								<option value="${list}" <c:if test="${pageSize eq list}">selected="selected"</c:if>><spring:message code="form.label.showall" /></option>
							</c:when>
							<c:otherwise>
								<option value="${list}" <c:if test="${pageSize eq list}">selected="selected"</c:if>><spring:message code="form.label.show" arguments="${list}" /></option>
							</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
					<c:choose>
						<c:when test="${!certList.lastPage}">
							<input type="button" id="next" value="<spring:message code="pagination.next"/>" />&nbsp;
							<input type="button" id="last" value="<spring:message code="pagination.last"/>" />
						</c:when>
						<c:otherwise>
							<input type="button" id="noNext" value="<spring:message code="pagination.next"/>" disabled="disabled"/>
							<input type="button" id="noLast" value="<spring:message code="pagination.last"/>" disabled="disabled"/>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
			<table id="cList" class="listHier" cellspacing="2px" width="500px" summary="Certificates">
				<thead align="center">
					<tr>
	                  <th></th>
					  <th><spring:message code="form.label.certificate"/></th>
                      <th><spring:message code="form.label.certificate.description"/></th>
					  <th><spring:message code="form.label.status"/></th>
					  <th><spring:message code="form.label.created"/></th>
					  <th><spring:message code="form.label.statusurl"/></th>
					</tr>
				</thead>
				<tbody align="left">
		        	<c:forEach var="cert" items="${certList.pageList}">
		            <tr>
	                    <td>
	                        <input type="radio" name="certDefRadioButtons" value="${cert.id}"/>
	                    </td>
		            	<td>
		                	${cert.name}
		                </td>
                        <td>
                            ${cert.description}
                        </td>
		             	<td>
		             		${cert.status}
		            	</td>
		            	<td>
			            	${cert.createDate}
			         	</td>
			         	<td>
			         		<c:if test="${cert.shortUrl != null}">${cert.shortUrl}</c:if>
			         	</td> 
		          	</tr>
		       		</c:forEach>
				</tbody>
			</table>
		</c:otherwise>
		</c:choose>
	</form:form>
	<script type="text/javascript">
	
		$(document).ready(function() {

            loaded();
			
			eval($("#copyStatusUrl")).click(function() {
				
			});
			
			$("#first").click( function() {
				location.href="list.form?page=first";
				return false;
			});
			
			$("#prev").click( function() {
				location.href="list.form?page=previous";
				return false;
			});
			
			$("#next").click( function() {
				location.href="list.form?page=next";
				return false;
			});
			
			$("#last").click( function() {
				location.href="list.form?page=last";
				return false;
			});
			
			$("#pageSize").change( function() {
				location.href="list.form?pageSize=" + $("#pageSize option:selected").val() +" &pageNo=" + $("#pageNo").val();
				return false;
			});
			
			$("#Add").click( function() {
				location.href="first.form";
				return false;
			});
			
			$("#Edit").click( function() {
				
				if(singleChecked())
				{
						location.href="first.form?certId="+$("input:checked").val();
						return false;
				}
				return false;
			});

            $("#Delete").click( function() {
                if(singleChecked())
                {
                    var proceed = confirm ("<spring:message code='form.delete.confirm'/>\n\n" +
                                           "<spring:message code='form.delete.confirm.ok'/>");

                    if (proceed == true)
                        location.href = "delete.form?certId=" + $("input:checked").val();
                    	return false;
                }

                return false;
            });
			
			$(":checkbox").click( function() {
				$(":checkbox").not(this).removeAttr("checked");
			});
			
			function singleChecked()
			{
				$(".alertMessage").hide();
				if($("input:checked").length == 1)
				{
					return true;
				}
				else if($("input:checked").length == 0)
				{
					$("#submitError").html("<spring:message code='form.error.noneselected'/>").show();
					resetHeight();
					return false;
				}
				else
				{
					$("#submitError").html("<spring:message code='form.error.multipleselect'/>").show();
					resetHeight();
					return false;
				}
			}
		});
		
	</script>
<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
