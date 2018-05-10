<ul class="navIntraTool actionToolBar">
    <li>
        <c:choose>
            <c:when test="${view == 'add' or view == null}">
                <span class="current">
                    <spring:message code="form.menu.add" />
                </span>
            </c:when>
            <c:otherwise>
                <span>
                    <a href="first.form">
                        <spring:message code="form.menu.add" />
                    </a>
                </span>
            </c:otherwise>
        </c:choose>
    </li>
    <li>
        <c:choose>
            <c:when test="${view == 'list'}">
                <span class="current">
                    <spring:message code="form.menu.list" />
                </span>
            </c:when>
            <c:otherwise>
                <span>
                    <a href="#" id="certificationList">
                        <spring:message code="form.menu.list" />
                    </a>
                </span>
            </c:otherwise>
        </c:choose>
    </li>
</ul>
<script type="text/javascript">
    $("#certificationList").click(function() {
        SPNR.disableControlsAndSpin( this, null );
        $("#submitValue").val("cancel");
        $("[id^=createCertForm]").submit();
    });
</script>
