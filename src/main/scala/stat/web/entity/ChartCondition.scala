package stat.web.entity

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils.toInt
import stat.web.WebEnv
import stat.web.logic.StatUtil

import scala.util.matching.Regex.Match

object ChartCondition extends StatUtil {

	// url规则：/d-s0-t20150101-l7/dev/
	private val uri_pattern = "/((?<type>d|h|m)(-s(?<s>[0-9]+))?(-t(?<t>[0-9]+))?(-l(?<l>[0-9]+))?/)?(?<proj>[\\w.,~*-]+)/(?<key>[\\w.,~*+-]+)".r

	def parse(uri: String): ChartCondition = {
		uri_pattern.findFirstMatchIn(uri) match {
			case Some(m: Match) => {
				val typ = TimeType.valueOf(m.group("type")) // group nullable
				val s = toInt(m.group("s"), 0) // group nullable
				var till = toInt(m.group("t")) // group nullable
				var len = toInt(m.group("l")) // group nullable
				val proj = m.group("proj")
				val key = m.group("key")
				if (till / 1000000 != 20) till = today
				if (len <= 0) len = default_l(typ)
				else len = Math.min(len, max_l(typ))
				new ChartCondition(typ, s, till, len, proj, key)
			}
			case None => WebEnv.default_cond
		}
	}

	// bell(2014-2): 准备修改
	// proj ! key - v @ type
	private def max_l(typ: TimeType): Int = {
		if (typ eq TimeType.d) 600 else if (typ eq TimeType.h) 30 else 7
	}
	def default_l(typ: TimeType): Int = {
		if (typ eq TimeType.d) 60 else if (typ eq TimeType.h) 7 else 2
	}


}

case class ChartCondition(typ: TimeType, server: Int, till: Int, len: Int, proj: String, key: String) {

	def ui_server() = if (server == 0) "汇总" else if (server == 99) "展开" else s"s$server"

	override def equals(obj: Any): Boolean = {
		obj match {
			case that: ChartCondition =>
				(this.typ eq that.typ) &&
					this.server == that.server &&
					this.till == that.till &&
					this.len == that.len &&
					StringUtils.equals(this.proj, that.proj) &&
					StringUtils.equals(this.key, that.key)
			case _ => false
		}
	}

	override def toString: String = {
		val out = new StringBuilder
		out.append("/" + typ)
		if (server > 0) out.append("-s" + server)
		if (till > 0) out.append("-t" + till)
		if (len > 0) out.append("-l" + len)
		out.append("/" + proj)
		out.append("/" + key)
		out.toString()
	}

	def alter_time_type(period: TimeType): ChartCondition = {
		ChartCondition(period, server, 0, 0, proj, key)
	}
	def alter_s(s: Int): ChartCondition = {
		ChartCondition(typ, s, till, len, proj, key)
	}
	def alter_t(t: Int): ChartCondition = {
		ChartCondition(typ, server, t, len, proj, key)
	}
	def alter_l(l: Int): ChartCondition = {
		ChartCondition(typ, server, till, l, proj, key)
	}

}
