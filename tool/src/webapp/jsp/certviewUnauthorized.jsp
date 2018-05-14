<%@ include file="/jsp/include.jsp" %>
<jsp:include page="/jsp/header.jsp" />
<form:form id="certList" method="POST">
    <p class="instruction">
        <spring:message code="list.view.unauthorized" />
    </p>
</form:form>
<script type="text/javascript">
    $(document).ready(function() {
        loaded();
    });
</script>
<%@ include file="/jsp/footer.jsp" %>
