<%@ page pageEncoding="utf-8" contentType="text/html;charset=utf-8" %>
<%@ page import="stat.web.logic.MenuBuilder" %>
<% MenuBuilder menu = new MenuBuilder(out, request); %>

<div class="container">
    <ul class="nav nav-pills">
        <% menu.p("数据库1", "bell/db.main.query.cost-v|DB1查询耗时||bell/db.main.update.cost-v|DB1更新耗时"); %>

        <% menu.p("数据库2", "bell/db.main.query.cost-v|DB2查询耗时||bell/db.main.update.cost-v|DB2更新耗时"); %>

        <% menu.show_ul();%>
    </ul>

    <%--<div class="btn-group pull-left">--%>
    <%--<a id="debug" onclick="...(this)">debug</a>--%>
    <%--</div>--%>
</div>
<div class="container" id="sumli">
<% menu.show_li();%>
</div>
