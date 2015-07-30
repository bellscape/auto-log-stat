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

	def floor(minute: String, period: Long): Long = {
		val time = minute2time(minute)
		val core: Long = (time + timezone_fix) / period
		core * period - timezone_fix
	}
	def ceil(minute: String, period: Long): Long = {
		val time = minute2time(minute)
		val remainder = (time + timezone_fix) % period
		if (remainder > 0) floor(minute, period) + period else time
	}
	// bell(2015-7): assuming time zone UTC+8
	private val timezone_fix = 8 * hour

	def minute2day(text: String): String = {
		text.take(8)
	}
	def day2minute(text: String): String = {
		text + "0000"
	}
	def add_days(day_str: String, i: Int): String = {
		time2day(day2time(day_str) + i * day)
	}

}
