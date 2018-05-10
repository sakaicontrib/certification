<%@ include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>
<script type="text/javascript" src="/library/js/jquery/cookie/jquery.cookie.js"></script>
<script type="text/javascript" src="/library/js/lang-datepicker/lang-datepicker.js"></script>
<form:form id="reportView" method="POST">
    <ul class="navIntraTool actionToolBar">
        <li><span><a href="${toolUrl}/reportView.form?certId=${cert.id}&export=true" id="export"><spring:message code="export.csv"/></a></span></li>
        <li><span><a href="" id="return"><spring:message code="return.cert.list"/></a></span></li>
    </ul>
    <div class="page-header">
        <h1><spring:message code="report.header" arguments="${cert.name}" htmlEscape="true"/></h1>
    </div>
    <div id="errorDiv">
        <c:forEach items="${errors}" var="error">
            <div class="alertMessage">
                ${error}
            </div>
        </c:forEach>
    </div>

    <div class="panel-group" id="accordion">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h4 class="panel-title">
                    <a data-toggle="collapse" data-parent="#accordion" href="#panel-requeriments">
                        <spring:message code="report.requirements"/>
                    </a>
                </h4>
            </div>
            <div id="panel-requeriments" class="panel-collapse collapse">
                <div class="panel-body">
                    <ul>
                        <c:forEach items="${requirements}" var="requirement">
                            <li>${requirement}</li>
                        </c:forEach>
                    </ul>
                    <p>
                        <c:if test="${expiryOffset != null}">
                            <spring:message code="report.disclaimer.1" arguments="${expiryOffset}" />
                        </c:if>
                        <spring:message code="report.disclaimer.2" />
                    </p>
                </div>
            </div>
        </div>
        <div class="panel panel-default">
            <div class="panel-heading">
                <h4 class="panel-title">
                    <a data-toggle="collapse" data-parent="#accordion" href="#panel-display-options">
                        <spring:message code="report.filter.head"/>
                    </a>
                </h4>
            </div>
            <div id="panel-display-options" class="panel-collapse collapse in">
              <div class="panel-body">
                <p>
                    <b><spring:message code="report.filter.show"/></b>
                </p>
                <p>
                    <label>
                        <input id="rdAll" type="radio" name="show" value="all" onclick="$('#dateRange').css('display','none');" checked>
                            <spring:message code="report.filter.all"/>
                        </input>
                    </label><br/>
                    <label>
                        <input id="idUnawarded" type="radio" name="show" value="unawarded" onclick="$('#dateRange').css('display','none');">
                            <spring:message code="report.filter.unawarded"/>
                        </input>
                    </label><br/>
                    <label>
                        <input id="idAwarded" type="radio" name="show" value="awarded" onclick="$('#dateRange').css('display','inline');">
                            <spring:message code="report.filter.awarded"/>
                        </input>
                    </label><br/>
                    <div id="dateRange" style="display:none;">
                        <spring:message code="report.filter.awarded.1"/>
                        <c:choose>
                            <c:when test="${expiryOffset != null}">
                                <select id="filterDateType">
                                    <option value="issueDate"><spring:message code="report.filter.issuedate"/></option>
                                    <option value="expiryDate"><spring:message code="report.filter.expirydate"/></option>
                                </select>
                            </c:when>
                            <c:otherwise>
                                <spring:message code="report.filter.issuedate.lower"/>
                            </c:otherwise>
                        </c:choose>
                        <spring:message code="report.filter.awarded.2"/>
                        <input id="startDate" type="text"/>
                        <spring:message code="report.filter.awarded.3"/>
                        <input id="endDate" type="text"/>
                    </div>
                </p>
                <p>
                    <label>
                        <input id="historical" type="checkbox" value="historical">
                            <spring:message code="report.filter.historical"/>
                        </input>
                    </label>
                </p>
                <div>
                    <input id="filterApply" type="submit" value="Apply"/>
                    <input id="filterReset" type="submit" value="Reset"/>
                </div>
              </div>
            </div>
        </div>
    </div>
    <p class="viewNav"><spring:message code="report.blurb"/></p>
    <div class="navPanel row">
        <div class="col-sm-7 col-xs-12">
            <nav class="certPager panel panel-default">
                <div class="panel-heading">
                    <spring:message code="form.pager.showing"/> <c:out value="${firstElement}" /> - <c:out value="${lastElement}" /> of ${reportList.nrOfElements}
                    <div id="spinner" class="allocatedSpinPlaceholder"></div>
                </div>
                <div class="panel-body">
                    <c:choose>
                        <c:when test="${!reportList.firstPage}">
                            <input type="button" id="first" value="<spring:message code='pagination.first' />" />
                            <input type="button" id="prev" value="<spring:message code='pagination.previous' />" />
                        </c:when>
                        <c:otherwise>
                            <input type="button" id="nofirst" value="<spring:message code='pagination.first' />" disabled="disabled" />
                            <input type="button" id="noPrev" value="<spring:message code='pagination.previous' />" disabled="disabled" />
                        </c:otherwise>
                    </c:choose>
                    <input type="hidden" id="pageNo" value="${pageNo}" />
                    <select id="pageSize">
                        <c:forEach items="${pageSizeList}" var="list">
                            <c:choose>
                            <c:when test="${list > 200}">
                                <option value="${list}" <c:if test="${pageSize eq list}">selected="selected"</c:if>><spring:message code="form.label.showall" /></option>
                            </c:when>
                            <c:otherwise>
                                <option value="${list}" <c:if test="${pageSize eq list}">selected="selected"</c:if>><spring:message code="form.label.show" arguments="${list}" /></option>
                            </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </select>
                    <c:choose>
                        <c:when test="${!reportList.lastPage}">
                            <input type="button" id="next" value="<spring:message code='pagination.next' />" />
                            <input type="button" id="last" value="<spring:message code='pagination.last' />" />
                        </c:when>
                        <c:otherwise>
                            <input type="button" id="noNext" value="<spring:message code='pagination.next' />" disabled="disabled"/>
                            <input type="button" id="noLast" value="<spring:message code='pagination.last' />" disabled="disabled"/>
                        </c:otherwise>
                    </c:choose>
                </div>
            </nav>
        </div>
    </div>
    <input id="certificateId" type="button" style="display:none" value="${cert.id}"></input>
    <table id="reporttable" class="table table-hover table-striped table-bordered" summary="Report">
        <thead align="center">
            <tr>
                <!-- the columns need to be in a c:for -->
                <!-- create the headers in an array from java code -->
                <th>
                    <a href="reportViewSort.form?certId=${cert.id}&sort=name" id="sortByName" onClick="SPNR.insertSpinnerInPreallocated( this, null, 'spinner' );">
                        <spring:message code="report.table.header.name"/>
                    </a>
                </th>
                <th>
                    <a href="reportViewSort.form?certId=${cert.id}&sort=userId" id="sortByUserId" onClick="SPNR.insertSpinnerInPreallocated( this, null, 'spinner' );">
                        <spring:message code="report.table.header.userid"/>
                    </a>
                </th>
                <th class="hidden-xs">
                    <a href="reportViewSort.form?certId=${cert.id}&sort=role" id="sortByRole" onClick="SPNR.insertSpinnerInPreallocated( this, null, 'spinner' );">
                        <spring:message code="report.table.header.role"/>
                    </a>
                </th>
                <c:if test="${canUserViewStudentNums != false}">
                    <th class="hidden-xs">
                        <a href="reportViewSort.form?certId=${cert.id}&sort=studentNumber" id="sortByStudentNum" onClick="SPNR.insertSpinnerInPreallocated( this, null, 'spinner' );">
                            <spring:message code="report.table.header.studentNum"/>
                        </a>
                    </th>
                </c:if>
                <th class="hidden-xs">
                    <a href="reportViewSort.form?certId=${cert.id}&sort=issueDate" id="sortByIssueDate" onClick="SPNR.insertSpinnerInPreallocated( this, null, 'spinner' );">
                        <spring:message code="report.table.header.issuedate"/>
                    </a>
                </th>
                <c:forEach items="${critHeaders}" var="crit">
                    <th class="hidden-xs">
                        ${crit}
                    </th>
                </c:forEach>
                <th>
                    <a href="reportViewSort.form?certId=${cert.id}&sort=awarded" id="sortByAwarded" onClick="SPNR.insertSpinnerInPreallocated( this, null, 'spinner' );">
                        <spring:message code="report.table.header.awarded"/>
                    </a>
                </th>
            </tr>
        </thead>
        <tbody align="left">
            <c:forEach var="row" items="${reportList.pageList}">
                <tr>
                    <td>${row.name}</td>
                    <td>${row.userId}</td>
                    <td class="hidden-xs">${row.role}</td>
                    <c:if test="${canUserViewStudentNums != false}">
                        <td class="hidden-xs">${row.studentNumber}</td>
                    </c:if>
                    <td class="hidden-xs">${row.issueDate}</td>
                    <c:forEach var="criterionCell" items="${row.criterionCells}">
                        <c:choose>
                            <c:when test="${criterionCell.met}">
                                <td class="hidden-xs">${criterionCell.progress}</td>
                            </c:when>
                            <c:otherwise>
                                <%--TODO - use a css class--%>
                                <td  class="hidden-xs" style="color:red;">${criterionCell.progress}</td>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                    <td>${row.awarded}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</form:form>
<script type="text/javascript">
    $(document).ready(function()
    {
        loaded();

        /*If the user expands/collapses elements, use cookies to keep track of this*/
        if ($.cookie("requirementsExpanded") === "false")
        {
            $("#requirementsPanel").hide();
            $("#requirementsHeadRightArrow").css("display","inline-block");
            $("#requirementsHeadDownArrow").css("display","none");
        }

        $("#requirementsHead").click(function()
        {
            $("#requirementsPanel").slideToggle(200, function() { resetHeight(); } );
            if ($.cookie('requirementsExpanded') === "false")
            {
                $("#requirementsHeadRightArrow").css("display","none");
                $("#requirementsHeadDownArrow").css("display","inline-block");
                $.cookie('requirementsExpanded', 'true');
            }
            else
            {
                $("#requirementsHeadRightArrow").css("display","inline-block");
                $("#requirementsHeadDownArrow").css("display","none");
                $.cookie('requirementsExpanded', 'false');
            }
        });

        if (!$.cookie('displayOptionsExpanded'))
        {
            $.cookie('displayOptionsExpanded',"false");
        }

        if ($.cookie('displayOptionsExpanded') === "false")
        {
            $("#displayOptionsHeadRightArrow").css("display","inline-block");
            $("#displayOptionsHeadDownArrow").css("display","none");
            $("#displayOptionsPanel").hide();
        }

        $("#displayOptionsHead").click(function()
        {
            $("#displayOptionsPanel").slideToggle(200, function() { resetHeight(); } );
            if ($.cookie('displayOptionsExpanded') === "false")
            {
                $("#displayOptionsHeadRightArrow").css("display","none");
                $("#displayOptionsHeadDownArrow").css("display","inline-block");
                $.cookie('displayOptionsExpanded', "true");
            }
            else
            {
                $("#displayOptionsHeadRightArrow").css("display","inline-block");
                $("#displayOptionsHeadDownArrow").css("display","none");
                $.cookie('displayOptionsExpanded', "false");
            }
        });

        localDatePicker({
              input: '#startDate',
              useTime: 0,
              parseFormat: 'YYYY-MM-DD',
              allowEmptyDate: false,
              val: '${filterStartDate}',
              ashidden: { iso8601: 'startDateISO8601' }
        });

        localDatePicker({
              input: '#endDate',
              useTime: 0,
              parseFormat: 'YYYY-MM-DD',
              allowEmptyDate: false,
              val: '${filterEndDate}',
              ashidden: { iso8601: 'endDateISO8601' }
        });

        /*Use cookies to keep track of the user's display options*/
        <c:choose>
            <c:when test="${useDefaultDisplayOptions == true}">
                /*We're using the defaults, so set the cookies*/
                var filterType = $("input[name='show']:checked").val();
                <c:choose>
                    <c:when test="${expiryOffset != null}">
                        var filterDateType = $("#filterDateType option:selected").val();
                    </c:when>
                    <c:otherwise>
                        var filterDateType = "issueDate";
                    </c:otherwise>
                </c:choose>
                var filterStartDate = $("#startDate").val();
                var filterEndDate = $("#endDate").val();

                var filterHistorical = $("#historical").prop('checked');
                $.cookie("filterType", filterType);
                $.cookie("filterDateType", filterDateType);
                $.cookie("filterStartDate", filterStartDate);
                $.cookie("filterEndDate", filterEndDate);
                $.cookie("filterHistorical", filterHistorical);
            </c:when>
            <c:otherwise>
                /*We're not using the defaults, so use cookies*/
                var filterType = $.cookie("filterType");
                var filterDateType = $.cookie("filterDateType");
                var filterHistorical = $.cookie("filterHistorical");
                var filterStartDate = $.cookie("filterStartDate");
                var filterEndDate = $.cookie("filterEndDate");
                var filterHistorical = $.cookie("filterHistorical");

                /*do a click event - this way the css on the dateRange will be applied*/
                $("input[name=show][value=" + filterType + "]").click();
                <c:if test="${expiryOffset != null}">
                    $("#filterDateType").val(filterDateType);
                </c:if>
                $("#startDate").val(filterStartDate);
                $("#endDate").val(filterEndDate);
                if (filterHistorical == "true")
                {
                    $("#historical").attr("checked", "checked");
                }
            </c:otherwise>
        </c:choose>

        $("#return").click( function() {
            location.href="list.form";
            return false;
        });

        var id = $("#certificateId").val();

        $("#first").click( function() {
            SPNR.disableControlsAndSpin( this, null );
            location.href="reportView.form?certId=" + id + "&page=first";
            return false;
        });

        $("#prev").click( function() {
            SPNR.disableControlsAndSpin( this, null );
            location.href="reportView.form?certId=" + id + "&page=previous";
            return false;
        });

        $("#next").click( function() {
            SPNR.disableControlsAndSpin( this, null );
            location.href="reportView.form?certId=" + id + "&page=next";
            return false;
        });

        $("#last").click( function() {
            SPNR.disableControlsAndSpin( this, null );
            location.href="reportView.form?certId=" + id + "&page=last";
            return false;
        });

        $("#pageSize").change( function() {
            SPNR.insertSpinnerInPreallocated( this, null, "spinner" );
            location.href="reportView.form?certId=" + id + "&pageSize=" + $("#pageSize option:selected").val() +" &pageNo=" + $("#pageNo").val();
            return false;
        });

        $("#filterApply").click( function() {
            SPNR.disableControlsAndSpin( this, null );
            $("#filterReset").attr('disabled', 'disabled');
            var filterType = $("input[name='show']:checked").val();
            <c:choose>
                <c:when test="${expiryOffset != null}">
                    var filterDateType = $("#filterDateType option:selected").val();
                </c:when>
                <c:otherwise>
                    var filterDateType = "issueDate";
                </c:otherwise>
            </c:choose>
            var filterStartDate = $("#startDate").val();
            var filterEndDate = $("#endDate").val();
            var filterHistorical = $("#historical").prop('checked');
            $.cookie("filterType", filterType);
            $.cookie("filterDateType", filterDateType);
            $.cookie("filterStartDate", filterStartDate);
            $.cookie("filterEndDate", filterEndDate);
            $.cookie("filterHistorical", filterHistorical);
            filterStartDate = $("#startDateISO8601").val();
            filterEndDate = $("#endDateISO8601").val();
            location.href="reportViewFilter.form?certId=" + id + "&filterType=" + filterType + "&filterDateType=" + filterDateType + "&filterStartDate=" + filterStartDate + "&filterEndDate=" + filterEndDate + "&filterHistorical=" + filterHistorical;
            return false;
        });

        $("#filterReset").click( function() {
            SPNR.disableControlsAndSpin( this, null );
            $("#filterApply").attr('disabled', 'disabled');
            location.href="reportView.form?certId=" + id;
            return false;
        });
    });
</script>
<%@ include file="/WEB-INF/jsp/footer.jsp" %>
