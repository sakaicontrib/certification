<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<% response.setContentType("text/html; charset=UTF-8"); %>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
   <head>
      <title><%= (String)request.getAttribute("_title")%></title>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
      <meta http-equiv="Content-Style-Type" content="text/css" />
      <link href="<c:out value="${sakai_skin_base}" />" type="text/css" rel="stylesheet" media="all" />
      <link href="<c:out value="${sakai_skin}" />" type="text/css" rel="stylesheet" media="all" />
      <script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js"></script>
      <script type="text/javascript" src="/library/js/jquery/1.4.2/jquery-1.4.2.min.js"></script>
      <%
          String panelId = request.getParameter("panel");
          if (panelId == null) {
             panelId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
          }
      %>

      <script language="javascript">
         function resetHeight() 
         {
            setMainFrameHeight('<%= org.sakaiproject.util.Validator.escapeJavascript(panelId)%>');
         }

         function loaded() {
            resetHeight();
            parent.updCourier(doubleDeep, ignoreCourier);
            if (parent.resetHeight) {
               parent.resetHeight();
            }
         }
      </script>
   </head>

<body >
    <div class="portletBody">
       <c:if test="${not empty requestScope.panelId}"><div class="ospEmbedded"></c:if>
