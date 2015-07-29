package debug

import stat.crontab.CrontabPerDay
import util.{TimeUtil, Utils}

object DebugRoutine extends Utils {
	def main(args: Array[String]) {

		val today = TimeUtil.time2day(System.currentTimeMillis())

		(0 to 0).foreach { i =>
			val minute = TimeUtil.day2minute(TimeUtil.add_days(today, -i))
			CrontabPerDay.run(Array(minute))
		}

	}
}
