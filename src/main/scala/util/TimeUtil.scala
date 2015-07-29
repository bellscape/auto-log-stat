package util

import org.joda.time.format.DateTimeFormat

object TimeUtil {

	val second: Long = 1000
	val minute: Long = 60 * second
	val hour: Long = 60 * minute
	val day: Long = 24 * hour

	/* ------------------------- str -> long ------------------------- */

	private def str2time(text: String, pattern: String): Long = {
		DateTimeFormat.forPattern(pattern).parseMillis(text)
	}
	def minute2time(text: String): Long = {
		str2time(text, "yyyyMMddHHmm")
	}
	def day2time(text: String): Long = {
		str2time(text, "yyyyMMdd")
	}

	/* ------------------------- long -> str ------------------------- */

	private def time2str(time: Long, pattern: String): String = {
		DateTimeFormat.forPattern(pattern).print(time)
	}
	def time2day(time: Long): String = {
		time2str(time, "yyyyMMdd")
	}
	def time2minute(time: Long): String = {
		time2str(time, "yyyyMMddHHmm")
	}
	def time2expression(time: Long): String = {
		time2str(time, "yyyy-MM-dd HH:mm:ss")
	}
	def today(): String = {
		time2day(System.currentTimeMillis())
	}

	/* ------------------------- util ------------------------- */

	def floor(time: String, period: Long): String = {
		val core: Long = (minute2time(time) + timezone_fix) / period
		time2minute(core * period - timezone_fix)
	}
	// bell(2015-7): assuming time zone UTC+8
	private val timezone_fix = 8 * hour
	def add_time(time: String, diff: Long) = {
		time2minute(minute2time(time) + diff)
	}

	def minute2day(text: String): String = {
		text.take(8)
	}
	def day2minute(text: String): String = {
		text + "0000"
	}
	def add_days(time: String, i: Int): String = {
		time2day(day2time(time) + i * day)
	}
	def add_days(time: Int, i: Int): Int = {
		time2day(day2time(time.toString) + i * day).toInt
	}

}
