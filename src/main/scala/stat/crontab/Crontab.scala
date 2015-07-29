package stat.crontab

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

		val end_time_stamp = TimeUtil.floor(args(0), period)
		val start_time_stamp = TimeUtil.add_time(end_time_stamp, -period)
		val start_time: Long = TimeUtil.minute2time(start_time_stamp)
		val end_time: Long = TimeUtil.minute2time(end_time_stamp)
		log(s"start as $start_time_stamp ~ $end_time_stamp")

		// todo

		log(s"fin as $start_time_stamp ~ $end_time_stamp")
		System.exit(0)
	}

}
