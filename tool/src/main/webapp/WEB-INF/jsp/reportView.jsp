<%@ include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>
    <form:form id="reportView" method="POST">
        <div class="navIntraTool">
            <a href="" id="export"><spring:message code="export.csv"/></a>&nbsp;
            <a href="" id="return"><spring:message code="return.cert.list"/></a>
        </div>
        <h2><spring:message code="report.header" arguments="${cert.name}"/></h2>
        <p>
            <spring:message code="report.requirements"/>
            <ul>
                <c:forEach items="${requirements}" var="requirement">
                    <li>${requirement}</li>
                </c:forEach>
            </ul>
        </p>
        <!-- TODO: put this in the c:choose -->
        <p class="viewNav"><spring:message code="report.blurb"/></p>
        <div class="listNav">
            <div class="pager">
                <span style="align:center"><spring:message code="form.pager.showing"/>&nbsp;<c:out value="${firstElement}" />&nbsp;&#045;&nbsp;<c:out value="${lastElement}" />&nbsp;of&nbsp;${reportList.nrOfElements}</span></br>
                <c:choose>
                    <c:when test="${!reportList.firstPage}">
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
        <table id="reporttable" class="listHier" width="500px" cellspacing="2px" summary="Report" certificateid="${cert.id}">
            <thead align="center">
                <tr>
                    <!-- the columns need to be in a c:for -->
                    <!-- create the headers in an array from java code -->
                    <th><spring:message code="report.table.header.name"/></th>
                    <th><spring:message code="report.table.header.userid"/></th>
                    <c:forEach items="${userPropHeaders}" var="prop">
                        <th>${prop}</th>
                    </c:forEach>
                    <th><spring:message code="report.table.header.issuedate"/></th>
                    <c:forEach items="${critHeaders}" var="crit">
                        <th>${crit}</th>
                    </c:forEach>
                    <th><spring:message code="report.table.header.awarded"/></th>
                </tr>
            </thead>
            <tbody align="left">
            <c:forEach var="row" items="${reportList.pageList}">
                <tr>
                    <td>${row.name}</td>
                    <td>${row.userId}</td>
                    <c:forEach var="prop" items="${row.extraProps}">
                        <td>${prop}</td>
                    </c:forEach>
                    <td>${row.issueDate}</td>
                    <c:forEach var="criterionCell" items="${row.criterionCells}">
                        <td>${criterionCell}</td>
                    </c:forEach>
                    <td>${row.awarded}</td>
                </tr>
            </c:forEach>
        </table>
        <!--
        <c:choose>
        <c:when test="${empty reportList}">
            TODO
        </c:when>
        <c:otherwise>
        </c:otherwise>
        </c:choose>
        -->
    </form:form>
    <script type="text/javascript">
        $(document).ready(function() 
        {
            loaded();

            $("#return").click( function() {
                location.href="list.form";
                return false;
            });

            var id = document.getElementById('reporttable').getAttribute('certificateid');

            $("#export").click( function() {
                location.href="reportView.form?certId=" + id + "&export=true";
                return false;
            });

            $("#first").click( function() {
                location.href="reportView.form?certId=" + id + "&page=first";
                return false;
            });

            $("#prev").click( function() {
                location.href="reportView.form?certId=" + id + "&page=previous";
                return false;
            });

            $("#next").click( function() {
                location.href="reportView.form?certId=" + id + "&page=next";
                return false;
            });

            $("#last").click( function() {
                location.href="reportView.form?certId=" + id + "&page=last";
                return false;
            });

            $("#pageSize").change( function() {
                location.href="reportView.form?certId=" + id + "&pageSize=" + $("#pageSize option:selected").val() +" &pageNo=" + $("#pageNo").val();
                return false;
            });
        });
    </script>
<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
