<%@ include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>
<form:form id="certList" method="POST">
    <h3 style="text-align:center"><spring:message code="list.view.unauthorized"/></h3>
</form:form>
<script type="text/javascript">
    $(document).ready(function() {
        loaded();
    });
</script>
<%@ include file="/WEB-INF/jsp/footer.jsp" %>
