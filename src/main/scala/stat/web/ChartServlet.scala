package stat.web

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import stat.web.entity.ChartCondition

class ChartServlet extends HttpServlet {
	override def service(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
		val path = WebEnv.context_path + "/chart"
		val cond = ChartCondition.parse(req.getRequestURI.replaceFirst("^" + path, ""))
		req.setAttribute("cond", cond)
		req.setAttribute("path", path)
		req.setAttribute("ctx", WebEnv.context_path)

		req.getRequestDispatcher("/page/chart.jsp").forward(req, resp)
	}
}
