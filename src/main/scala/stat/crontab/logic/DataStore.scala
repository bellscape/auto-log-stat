package stat.crontab.logic

import stat.common.Env
import util.{TimeUtil, Utils}

object DataStore extends Utils {

	val db = Env.db

	/* ------------------------- save ------------------------- */

	def save_m(proj: String, s: Int, time: Long, rows: Seq[(String, Double, Int)]): Unit = {
		val table = s"raw_m_${TimeUtil.time2day(time)}"
		val t = TimeUtil.time2exp(time)
		rows.foreach(row =>
			db.update(s"replace into $table(p,s,t,k,v,c)values('$proj',$s,'$t',?,?,?)",
				row._1, row._2, row._3))
	}

	def sum_up_day(day: String, proj: String): Unit = {
		val day_table = "raw_d"
		val hour_table = s"raw_h_${day.take(6)}"
		val minute_table = s"raw_m_$day"

		// key = p,s,k,t

		// sum -> s=0
		// select p, 0 as s, k, t, sum(v*c)/sum(c) as v, sum(c) as c from raw_m_20131105
		// where p='tc-imginfo' and s>0 group by p,k,t
		db.update(s"replace into $minute_table" +
			s" select p, 0 as s, k, t, sum(v*c)/sum(c) as v, sum(c) as c from $minute_table" +
			s" where p='$proj' and s>0 group by p,k,t")

		// sum -> hourly
		// select p, s, k, t-interval(time_to_sec(t) mod 3600) second as h, sum(v*c)/sum(c) as v, sum(c) as c from raw_m_20131105
		// where p='tc-imginfo' group by p,s,k,h
		db.update(s"replace into $hour_table" +
			s" select p, s, k, t-interval(time_to_sec(t) mod 3600) second as h, sum(v*c)/sum(c) as v, sum(c) as c from $minute_table" +
			s" where p='$proj' group by p,s,k,h")

		// sum -> daily
		db.update(s"replace into $day_table" +
			s" select p, s, k, t-interval(time_to_sec(t) mod 86400) second as d, sum(v*c)/sum(c) as v, sum(c) as c from $hour_table" +
			s" where p='$proj' group by p,s,k,d")
	}

	def sum_up_minute(minuteStamps: (String, String), proj: String): Unit = {
		val day = minuteStamps._1.take(8)
		val day_table = "raw_d"
		val hour_table = s"raw_h_${day.take(6)}"
		val minute_table = s"raw_m_$day"

		// key = p,s,k,t

		// sum -> s=0
		{
			val tick = TimeUtil.time2exp(TimeUtil.minute2time(minuteStamps._1))
			// select p, 0 as s, k, t, sum(v*c)/sum(c) as v, sum(c) as c from raw_m_20131105
			// where p='tc-imginfo' and s>0 group by p,k,t
			db.update(s"replace into $minute_table" +
				s" select p, 0 as s, k, t, sum(v*c)/sum(c) as v, sum(c) as c from $minute_table" +
				s" where p='$proj' and s>0 and t='$tick' group by p,k,t")
		}

		if (!minuteStamps._2.endsWith("00"))
			return

		// sum -> hourly
		{
			val hour_start = TimeUtil.time2exp(TimeUtil.minute2time(minuteStamps._2) - TimeUtil.hour)
			val hour_end = TimeUtil.time2exp(TimeUtil.minute2time(minuteStamps._2))
			// select p, s, k, t-interval(time_to_sec(t) mod 3600) second as h, sum(v*c)/sum(c) as v, sum(c) as c from raw_m_20131105
			// where p='tc-imginfo' group by p,s,k,h
			db.update(s"replace into $hour_table" +
				s" select p, s, k, t-interval(time_to_sec(t) mod 3600) second as h, sum(v*c)/sum(c) as v, sum(c) as c from $minute_table" +
				s" where p='$proj' and t>='$hour_start' and t<'$hour_end' group by p,s,k,h")
		}

		// sum -> daily
		db.update(s"replace into $day_table" +
			s" select p, s, k, t-interval(time_to_sec(t) mod 86400) second as d, sum(v*c)/sum(c) as v, sum(c) as c from $hour_table" +
			s" where p='$proj' group by p,s,k,d")
	}

	/* ------------------------- util ------------------------- */

	def ensure_table_for_day(day: String) {
		ensure_table("raw_d")
		ensure_table(s"raw_h_${day.take(6)}")
		ensure_table(s"raw_m_$day")
	}

	private def ensure_table(table: String) {
		val exists = db.queryStringSeq(s"show tables like '$table'").nonEmpty
		if (exists) return

		// bell(2013-11): key顺序是这样考虑的：
		// 首先，前端保证不会跨p,s查询，所以p+s在索引最前
		// 最频繁用到的是最近2天的5分钟级查询，是5分钟表的所有时间汇总，所以索引中，k放在t之前
		// 对于其余常用查询：如小时级最近一周、天级最近2月，k与t的区分度相当，出于保持一致考虑
		// bell(2013-11): 字段全部使用单字母，原因是，后续配置中拼sql会频繁用到，节省体力
		db.update( s"""
CREATE TABLE `$table` (
`p` varchar(20) NOT NULL comment 'project',
`s` tinyint(4) NOT NULL comment 'server',
`k` varchar(100) NOT NULL comment 'key',
`t` timestamp NOT NULL default 0 comment 'start time of period',
`v` double default 0 comment 'value',
`c` int(11) default 0 comment 'count',
PRIMARY KEY  (p,s,k,t)
) ENGINE=MyISAM""")
		log(s"created table: $table")
	}

}
