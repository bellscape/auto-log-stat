package stat.web.logic

import util.TimeUtil

trait StatUtil {

	def today: Int = {
		TimeUtil.time2day(System.currentTimeMillis()).toInt
	}
	def add_day(day: Int, offset: Int): Int = {
		val time = TimeUtil.day2time(day.toString) + offset * TimeUtil.day
		TimeUtil.time2day(time).toInt
	}

}
