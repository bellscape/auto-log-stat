package stat.web

import javax.servlet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.apache.commons.lang3.StringUtils.isNotEmpty
import org.joda.time.format.DateTimeFormat

class TheFilter extends Filter {
	override def doFilter(_req: ServletRequest, _resp: ServletResponse, chain: FilterChain): Unit = {

		val req = _req.asInstanceOf[HttpServletRequest]
		val resp = _resp.asInstanceOf[HttpServletResponse]
		val url = get_url(req)
		val isStatic: Boolean = is_static_request(url)

		set_header(req, resp, isStatic)

		val start = System.currentTimeMillis
		chain.doFilter(req, resp)
		if (!isStatic)
			print_log(start, req)
	}

	private def is_static_request(url: String) = {
		val staticPostfixes = Array(".js", ".map", ".css", ".less", ".gif", ".png", ".jpg", ".jpeg", ".ico")
		staticPostfixes.exists(url.endsWith)
	}

	private def set_header(req: HttpServletRequest, resp: HttpServletResponse, isStatic: Boolean) = {
		if (isStatic) {
			// todo: css/js/img -> cacheable
		} else {
			req.setCharacterEncoding("UTF-8")
			resp.setCharacterEncoding("UTF-8")

			resp.setContentType("text/plain")
			resp.setHeader("Cache-Control", "no-cache")
			resp.setDateHeader("Expires", 0)
		}
	}
	private def print_log(start: Long, req: HttpServletRequest) = {
		val end = System.currentTimeMillis
		val cost = (end - start).asInstanceOf[Int]
		val url = get_url(req).replace(",", "%2c")
		val ip = get_ip(req)

		System.out.println(s"[INFO] [${log_date_format.print(end)}] ip=$ip cost=$cost url=$url")
	}
	private val log_date_format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.S")

	override def init(filterConfig: FilterConfig): Unit = {}
	override def destroy(): Unit = {}

	/* ------------------------- utils ------------------------- */

	private def get_url(req: HttpServletRequest): String = {
		val query = req.getQueryString
		req.getRequestURI + (if (isNotEmpty(query)) "?" + query else "")
	}

	private def get_ip(req: HttpServletRequest): String = {
		// bell(2014-11): TODO 增加代理转发的考虑（X-FORWARDED-FOR）
		req.getRemoteAddr
	}

}
