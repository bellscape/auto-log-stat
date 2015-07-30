package stat.crontab

import stat.crontab.logic.{CrontabData, Routine_AutoLog}
import util.TimeUtil._
import util.Utils

/* ------------------------- main ------------------------- */

object CrontabPerDay extends Crontab(day, "1day") {
	def main(args: Array[String]) {
		run(args)
	}
}
object CrontabPerHour extends Crontab(hour, "1hour") {
	def main(args: Array[String]) {
		run(args)
	}
}
object CrontabPer5Min extends Crontab(5 * minute, "5min") {
	def main(args: Array[String]) {
		run(args)
	}
}

/* ------------------------- impl ------------------------- */

class Crontab(period: Long, label: String) extends Utils {

	// usage: Main $yyyyMMddHHmm
	def run(args: Array[String]): Unit = {

		// use current time as default
		if (args.length != 1) {
			run(Array(time2minute(System.currentTimeMillis())))
			return
		}

		// init params
		val end_time = floor(args(0), period)
		val end_time_stamp = time2minute(end_time)
		val start_time_stamp = time2minute(end_time - period)
		log(s"start as $start_time_stamp ~ $end_time_stamp")

		CrontabData.ensure_table_for_day(minute2day(start_time_stamp))

		// bell(2014-1): sleep的第一个目的：保证autolog肯定能输出完
		// bell(2014-1): sleep的第二个目的：整点时候通常有些定时任务，机器性能吃紧，monitor结果可能不准，回避一下
		Thread.sleep(2000)

		// todo: Routine_Monitor

		Routine_AutoLog.run_minute(start_time_stamp, end_time_stamp)

		// todo: Routine_Alert

		log(s"fin as $start_time_stamp ~ $end_time_stamp")
	}

}
