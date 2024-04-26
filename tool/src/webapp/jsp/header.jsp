<!DOCTYPE html>
<%@ include file="/jsp/include.jsp" %>

<html lang="en">
<head>
    <title><%= (String)request.getAttribute("_title")%></title>
    <link href='<c:out value="${sakai_skin_base}" />' rel="stylesheet" />
    <link href='<c:out value="${sakai_skin}" />' rel="stylesheet" />
    <link href="css/certification.css" rel="stylesheet" />
    <script src="/library/js/headscripts.js"></script>
    <script src="/library/js/spinner.js"></script>
    <%
        String panelId = request.getParameter("panel");
        if (panelId == null) {
            panelId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
        }
    %>

    <script>
        var sakai = sakai || {};
        sakai.locale = sakai.locale || {};
        sakai.locale.userCountry = '${localeRef.getCountry()}';
        sakai.locale.userLanguage = '${localeRef.getLanguage()}';
        sakai.locale.userLocale = '${locale}';

        includeLatestJQuery('sakai.certification');
    </script>
</head>

<body >
    <div class="portletBody container-fluid">
        <c:if test="${not empty requestScope.panelId}"><div class="ospEmbedded"></c:if>
