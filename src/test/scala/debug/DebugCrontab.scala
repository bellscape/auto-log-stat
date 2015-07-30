package debug

import stat.crontab.CrontabPerDay
import util.{TimeUtil, Utils}

object DebugCrontab extends Utils {
	def main(args: Array[String]) {

		val today = TimeUtil.today()

		(0 to 2).foreach { i =>
			val day = TimeUtil.add_days(today, -i)
			println(s"run day $day")
			CrontabPerDay.run(Array(TimeUtil.day2minute(day)))
		}

		println("fin")
	}
}
