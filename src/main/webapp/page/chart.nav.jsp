<%@ page pageEncoding="utf-8" contentType="text/html;charset=utf-8" %>
<%@ page import="stat.web.entity.ChartCondition" %>
<%@ page import="stat.web.entity.TimeType" %>
<%@ page import="stat.web.logic.ChartJspUtil" %>

<% /* bof */
    {
        ChartCondition cond = (ChartCondition) request.getAttribute("cond"); %>
<div class="container">

    <!-- d/h/m5/m -->
    <div class="btn-group pull-left">
        <% for (TimeType type : TimeType.values()) {
            request.setAttribute("_c", cond.alter_time_type(type)); %>
        <a href="${path}${_c}" class="btn ${cond.typ == _c.typ ? 'active':''}">${_c.typ.label}</a>
        <% } %>
    </div>

    <!-- server -->
    <div class="btn-group pull-left">
        <button class="btn">Server(${cond.ui_server})</button>
        <button class="btn dropdown-toggle" data-toggle="dropdown">
            <span class="caret"></span>
        </button>
        <ul class="dropdown-menu">
            <% for (int s : ChartJspUtil.list_s(cond)) {
                request.setAttribute("_c", cond.alter_s(s)); %>
            <li><a href="${path}${_c}">${_c.ui_server}</a></li>
            <% } %>
        </ul>
    </div>

    <!-- from -->
    <div class="navbar-form pull-left">
        <span>穿越</span><input type="text" id="daylen" value="${cond.len}" style="width:30px" maxlength="3">
        <span>天，来到</span><input type="text" id="daytill" value="${cond.till}" class="span2" style="width:80px">
        <button id="daysubmit" class="btn">查询</button>
    </div>

    <div class="btn-group pull-right">
        <a class="btn btn-small" id="btn-nocache" title="无缓存"><i class="icon-refresh"></i></a>
        <a class="btn btn-small" id="btn-unmax" title="无上限"><i class="icon-resize-vertical"></i></a>
        <a class="btn btn-small" id="btn-inverse" title="反视"><i class="icon-random"></i></a>
    </div>

    <script>setTimeout(function () {
        var submit = function () {
            var base = '${path}<%=cond.alter_t(20120101).alter_l(999999)%>';
            base = base.replace('20120101', $('#daytill').val()).replace('999999', $('#daylen').val());
            location.href = base;
        };

        $('#daytill').datepicker({
            dateFormat: 'yymmdd', // yy已是4位年份
            onSelect: submit
        });
        $('#daylen').keydown(function (e) {
            if (e.keyCode == 13)
                submit();
        });
        $('#daysubmit').click(submit);
    }, 200);</script>

</div>
<% }/* eof */ %>
