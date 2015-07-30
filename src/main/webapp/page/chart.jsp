<%@ page pageEncoding="utf-8" contentType="text/html;charset=utf-8" %>
<%@ page import="stat.web.logic.ChartJspUtil" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">
    <link href="${ctx}/static/bootstrap/css/bootstrap.min.css" type="text/css" rel="stylesheet" />
    <link href="${ctx}/static/bootstrap/css/bootstrap-responsive.min.css" type="text/css" rel="stylesheet" />
    <link href="${ctx}/static/jquery/jquery-ui-1.8.17.custom.css" type="text/css" rel="stylesheet" />
    <link href="${ctx}/static/chart.css" type="text/css" rel="stylesheet" />

    <script src="${ctx}/static/jquery/jquery-1.7.2.min.js" type="text/javascript"></script>
    <script src="${ctx}/static/jquery/jquery-ui-1.8.17.custom.min.js" type="text/javascript"></script>
    <script src="${ctx}/static/bootstrap/bootstrap.min.js" type="text/javascript"></script>
    <script src="${ctx}/static/highcharts/highcharts-4.1.7.js" type="text/javascript"></script>
    <script src="${ctx}/static/jquery.util.js" type="text/javascript"></script>
    <script src="${ctx}/static/chart-paint.js" type="text/javascript"></script>
    <script src="${ctx}/static/chart-nav.js" type="text/javascript"></script>
    <title>${cond.key}</title>
</head>
<body>
<nav class="navbar navbar-static-top">
    <div class="navbar-inner">
        <%@include file="chart.nav.jsp"%>
        <%@include file="chart.menu.jsp"%>
    </div>
</nav>

<div class="container">
    <%
        ChartJspUtil.print_chart(request, out);
    %>
</div>

<%--<footer class="footer">
    <div class="container">
        O(∩_∩)O~
    </div>
</footer>--%>
</body>
</html>
