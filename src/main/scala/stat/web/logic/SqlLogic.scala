package stat.web.logic

import java.security.MessageDigest
import java.util.TimerTask

import stat.common.Env
import stat.web.WebEnv
import stat.web.entity.ChartDot
import util.{TimeUtil, Utils}

object SqlLogic extends Utils {

	val cache_duration = 1 * TimeUtil.hour
	val cache_size = 300

	private var cache_holder = new JavaLRUCache[String, Seq[ChartDot]](cache_size)
	WebEnv.timer.schedule(new TimerTask {
		override def run(): Unit = {
			cache_holder = new JavaLRUCache(cache_size)
		}
	}, cache_duration, cache_duration)


	def batch_select_with_cache(sqls: Seq[String], force_refresh: Boolean): Seq[ChartDot] = {
		sqls.flatMap(sql => select_with_cache(sql, force_refresh))
	}

	private def select_with_cache(sql: String, force_refresh: Boolean): Seq[ChartDot] = {
		val key = md5(sql)
		var cache: Seq[ChartDot] = if (force_refresh) null else cache_holder.get(key)
		if (cache == null) {
			cache = select(sql)
			cache_holder.put(key, cache)
		}
		cache
	}
	private def select(sql: String): Seq[ChartDot] = {
		Env.db.query_seq(sql, rs => ChartDot(rs.getString(1), rs.getTimestamp(2).getTime, rs.getDouble(3)))
	}
	private def md5(s: String): String = {
		val bytes = MessageDigest.getInstance("MD5").digest(s.getBytes(charset_utf8))
		bytes.flatMap(b => "%02x" format b).mkString
	}

	def main(args: Array[String]) {
		println(md5("")) // d41d8cd98f00b204e9800998ecf8427e
		println(md5("bell")) // 8d45c85b51b27a04ad7fdfc3f126f9f8
	}

}
