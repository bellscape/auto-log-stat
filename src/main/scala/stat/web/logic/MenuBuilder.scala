package stat.web.logic

import javax.servlet.http.HttpServletRequest
import javax.servlet.jsp.JspWriter

import stat.web.WebEnv
import stat.web.entity.ChartCondition

import scala.collection.mutable.ArrayBuffer

class MenuBuilder(val out_println: String => Unit, cond: ChartCondition, val active: String, val base_path: String) {

	val nav_ul = new ArrayBuffer[String]()
	val nav_sub_li = new ArrayBuffer[String]()
	var nav_cur_ul = ""

	def this(out_println: String => Unit, cond: ChartCondition) {
		this(out_println, cond,
			s"${cond.proj}/${cond.key}",
			WebEnv.context_path + "/show" + cond.toString.replaceFirst( """/[^/]+/[^/]+$""", "")
		)
	}
	def this(out: JspWriter, req: HttpServletRequest) {
		this(out.println: String => Unit, req.getAttribute("cond").asInstanceOf[ChartCondition])
	}

	def p(label: String, content: String): MenuBuilder = {
		nav_ul += label
		nav_sub_li += content
		if (content.contains(active)) {
			nav_cur_ul = label
		}
		this
	}

	def show_ul(): Unit = {
		p_ul(nav_cur_ul, nav_ul)
	}

	def show_li(): Unit = {
		var ili: Int = 0
		while (ili < nav_sub_li.size) {

			if (ili == nav_ul.indexOf(nav_cur_ul))
				out_println(s"<div id='subli_$ili' class='navallli' >")
			else
				out_println(s"<div id='subli_$ili' class='navallli' style='display:none'>")
			for (item <- nav_sub_li(ili).split("\\|\\|")) {
				val parts: Array[String] = item.split("\\|", 2)
				if (parts.length == 2) p_li(parts(0), parts(1))
				else p_li()
			}
			out_println("</div>")

			ili += 1
		}
	}

	private def p_ul(key: String, nav_ul: ArrayBuffer[String]) {
		out_println("<div>")
		nav_ul.indices.foreach { sub_i =>
			val nav_sub_ul = nav_ul(sub_i)
			val style_active = if (key == nav_sub_ul) "style='background-color: #e5e5e5;color: #555;'" else ""
			val html = "<a href=\"javascript:navli('$nav_sub_ul',$sub_i,this)\" id='navul_$sub_i' class='navul' $style_active>$nav_sub_ul</a>"
			out_println(html.replace("$nav_sub_ul", nav_sub_ul).replace("$sub_i", sub_i.toString).replace("$style_active", style_active))
		}
		out_println("</div>")
	}

	private def p_li(key: String, label_pattern: String) {
		val style_active = if (key == active) ";background-color: #e5e5e5;color: #555;" else ""
		val is_hot = label_pattern.startsWith("*")
		val label = if (is_hot) label_pattern.replaceFirst("\\*", "") else label_pattern
		val hot_badge = if (is_hot) "<span class='badge badge-warning'>hot</span>" else ""

		out_println(s"<a href='$base_path/$key' class='navli' style='$style_active'>$label$hot_badge</a>")
	}

	private def p_li() {
		out_println("<div style='clear:both;height:0;width:0'></div>")
	}

}
