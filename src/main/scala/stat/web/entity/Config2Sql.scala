package stat.web.entity

import org.joda.time.format.DateTimeFormat
import stat.common.Project
import stat.web.logic
import util.TimeUtil

object Config2Sql extends logic.StatUtil {

	def gen_sql(sql: String, cond: ChartCondition): Seq[Seq[String]] = {
		val s = cond.server
		val expand_all_servers = s >= 99
		if (expand_all_servers) {
			(1 to Project.get_server_size(cond.proj))
				.map(i => do_gen_sql(sql, cond, s"s=$i"))
				.toSeq
		} else {
			Seq(do_gen_sql(sql, cond, s"s=$s"))
		}
	}

	private def do_gen_sql(sql_pattern: String, cond: ChartCondition, server_clause: String): Seq[String] = {
		val last = cond.till
		val first = add_day(last, 1 - cond.len)

		cond.typ match {
			case TimeType.d =>
				val time_clause = s"t>='${to_day_start(first)}' and t<'${to_day_end(last)}'"
				Seq(do_get_sql(sql_pattern, cond, "raw_d", time_clause, server_clause))

			case TimeType.h =>
				// Seq[(month, has_first_day_limit, has_last_day_limit)]
				val months = Iterator.iterate[Int](to_first_day_of_month(last))(day => to_first_day_of_month(add_day(day, -1)))
					.takeWhile(day => (day / 100) >= (first / 100)).toSeq.reverse
				months.map { day =>
					val table = s"raw_h_${day / 100}"

					val has_first_day_limit = ((day / 100) == (first / 100)) && (first % 100 > 1)
					val has_last_day_limit = (day / 100) == (last / 100)
					val day_clause = Seq(
						if (has_first_day_limit) s"t>='${to_day_start(first)}'" else "",
						if (has_last_day_limit) s"t<'${to_day_end(last)}'" else ""
					).filter(_.nonEmpty).mkString(" and ")

					do_get_sql(sql_pattern, cond, table, day_clause, server_clause)
				}

			case TimeType.m =>
				val days = Iterator.iterate[Int](last)(day => add_day(day, -1)).takeWhile(_ >= first).toSeq.reverse
				days.map(day => do_get_sql(sql_pattern, cond, s"raw_m_$day", "", server_clause))
		}
	}

	private def to_first_day_of_month(day: Int): Int = (day / 100) * 100 + 1
	private def to_day_start(day: Int): String = {
		sql_time_format.print(TimeUtil.day2time(day.toString))
	}
	private val sql_time_format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
	private def to_day_end(day: Int): String = {
		to_day_start(add_day(day, 1))
	}

	private def do_get_sql(sql_pattern: String, cond: ChartCondition, table: String, time_clause: String, server_clause: String): String = {
		val typ = cond.typ
		val proj_clause = s"p='${cond.proj}'"

		sql_pattern.replace("{table}", table)
			.replace("{where}", "from " + table + " where" +
			(if (proj_clause.isEmpty) "" else s" $proj_clause and") +
			(if (server_clause.isEmpty) "" else s" $server_clause and") +
			(if (time_clause.isEmpty) "" else s" $time_clause and"))
	}

}
