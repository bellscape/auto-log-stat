package stat.crontab

import stat.crontab.logic.DataStore
import util.{TimeUtil, Utils}

/* ------------------------- main ------------------------- */

object CrontabPerDay extends Crontab(TimeUtil.day, "1day") {
	def main(args: Array[String]) {
		run(args)
	}
}
object CrontabPerHour extends Crontab(TimeUtil.hour, "1hour") {
	def main(args: Array[String]) {
		run(args)
	}
}
object CrontabPer5Min extends Crontab(5 * TimeUtil.minute, "5min") {
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
			run(Array(TimeUtil.time2minute(System.currentTimeMillis())))
			return
		}

		// init params
		val end_time_stamp = TimeUtil.floor(args(0), period)
		val start_time_stamp = TimeUtil.add_time(end_time_stamp, -period)
		val start_time: Long = TimeUtil.minute2time(start_time_stamp)
		val end_time: Long = TimeUtil.minute2time(end_time_stamp)
		log(s"start as $start_time_stamp ~ $end_time_stamp")

		DataStore.ensure_table_for_day(TimeUtil.min2day(end_time_stamp))

		// bell(2014-1): sleep的第一个目的：保证autolog肯定能输出完
		// bell(2014-1): sleep的第二个目的：整点时候通常有些定时任务，机器性能吃紧，monitor结果可能不准，回避一下
		sleep(2000)

		// todo: Routine_Monitor

		

		// todo: Routine_Alert

		log(s"fin as $start_time_stamp ~ $end_time_stamp")
		System.exit(0)
	}

}
