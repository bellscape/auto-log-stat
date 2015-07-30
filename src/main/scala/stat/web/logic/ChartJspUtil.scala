package stat.web.logic

import java.io.StringWriter
import javax.servlet.http.HttpServletRequest
import javax.servlet.jsp.JspWriter

import com.fasterxml.jackson.databind.ObjectMapper
import stat.common.Project
import stat.web.entity.{ChartCondition, ChartConfig, ChartDot}

object ChartJspUtil {

	/* ------------------------- nav ------------------------- */

	def list_s(cond: ChartCondition): Array[Int] = {
		Array(0) ++ Array(99) ++ (1 to Project.get_server_size(cond.proj))
	}

	/* ------------------------- chart ------------------------- */

	def print_chart(req: HttpServletRequest, out: JspWriter): Unit = {
		val cond = req.getAttribute("cond").asInstanceOf[ChartCondition]
		val forceRefresh = req.getParameter("nocache") == "on"
		print_chart(cond, out.println, forceRefresh)
	}
	def print_chart(cond: ChartCondition, out_println: String => Unit, force_refresh: Boolean): Unit = {
		val conf = ChartConfig.get(cond)
		val sql_list = if (conf != null) conf.to_sql(cond) else Seq.empty
		if (conf != null) {
			print_config(out_println, conf)

			if (sql_list.size == 1) {
				paint_chart(out_println, 0, sql_list.head, force_refresh)
			} else {
				sql_list.zipWithIndex.foreach { case (sql, i) =>
					paint_chart(out_println, i + 1, sql, force_refresh)
				}
			}

		} else {
			paint_err(out_println, s"未找到配置：${cond.key}", "")
		}


		out_println("<div class='btn-group'>")
		out_println("<a class='btn btn-mini' id=btn-showdebug title=debug><i class=icon-eye-open></i></a>")
		out_println("<a class='btn btn-mini' id=btn-showtable title=table><i class=icon-th></i></a>")
		out_println("</div>")

		out_println("<div id=showdebug class='alert alert-block alert-info hide'>")
		out_println("= debug info =<br>")
		out_println("cond=" + cond + "<br>")
		out_println("config=" + conf + "<br>")
		out_println("<hr>")
		sql_list.foreach(_.foreach(sql => out_println(s"$sql<br>")))
		out_println("</div>")
	}
	private def print_config(out_println: String => Unit, conf: ChartConfig) {
		if (!conf.comment.isEmpty) out_println(s"<div class='code'>${conf.comment}</div>")
		out_println(s"<script>${conf.chart_params}</script>")
	}
	private def paint_chart(out_println: String => Unit, chart_id: Int, sql: Seq[String], force_refresh: Boolean) {
		val id = s"chart$chart_id"
		if (chart_id > 0) out_println(s"<hr>$chart_id:")
		out_println(s"<div id=${id}_progress class='progress progress-success progress-striped active'>" +
			s"<div class=bar style='width:100%'></div></div>")
		out_println(s"<div id=$id class='chart${if (chart_id == 0) "" else " chart-mini"}'></div>")
		out_println(s"<div id=${id}_table class=hide></div>")

		try {
			val data: Seq[ChartDot] = SqlLogic.batch_select_with_cache(sql, force_refresh)
			out_println(s"<script>$$('#${id}_progress').hide();</script>")
			out_println(s"<script>var ${id}_data = ${to_json(data)};</script>")
			out_println(s"<script>$$('#$id').timeChart(${id}_data, chartParams);</script>")
		} catch {
			case e: Exception =>
				paint_err(out_println, "查询出错", e)
		}
	}
	private def paint_err(out_println: String => Unit, h4: String, text: String) {
		out_println(s"<div class='alert alert-block alert-error'><h4>$h4</h4>$text</div>")
	}
	private def paint_err(out_println: String => Unit, h4: String, e: Exception) {
		out_println(s"<div class='alert alert-block alert-error'><h4>$h4</h4><pre>")

		out_println(e.toString)
		e.getStackTrace.foreach(stack => out_println(s"\tat $stack"))

		out_println("</pre></div>")
	}

	private def to_json(data: Seq[ChartDot]): String = {
		val out = new StringWriter
		new ObjectMapper().writer().writeValue(out, data.map(_.to_array).toArray)
		out.toString
	}

}
