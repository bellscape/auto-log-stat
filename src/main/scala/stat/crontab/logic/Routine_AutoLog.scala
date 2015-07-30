package stat.crontab.logic

import stat.common.Project
import util.{TimeUtil, Utils}

object Routine_AutoLog extends Utils {

	// bell(2013-12): AutoLog规则为，每分钟结束后输出当前分钟的统计
	// 传入的为当前时间，所以需统计的是以此为结束时间，之前5分钟的日志
	// 即：当前为12:00，需要收集时间戳为1155~1159

	def run_minute(start_min_stamp: String, cur_min_stamp: String, projects: Seq[Project] = Project.projects): Unit = {
		val time = (start_min_stamp, cur_min_stamp)

		projects.filter(_.has_log).par.foreach { proj =>
			val machine = (proj.host, proj.host_user, proj.host_pass)
			val stat = (proj.proj, proj.server)
			do_run_auto_log(time, machine, proj.log_pattern, stat)
		}

		projects.filter(_.has_log).map(_.proj).distinct.foreach { proj =>
			CrontabData.sum_up_minute(time, proj)
			log(s"$proj sum up")
		}
	}

	def run_day(day: String, projects: Seq[Project] = Project.projects): Unit = {
		CrontabData.ensure_table_for_day(day)

		val time = (s"${day}0000", s"${TimeUtil.add_days(day, 1)}0000")

		projects.filter(_.has_log).par.foreach(proj => {
			val machine = (proj.host, proj.host_user, proj.host_pass)
			val stat = (proj.proj, proj.server)
			do_run_auto_log(time, machine, proj.log_pattern, stat)
		})

		projects.filter(_.has_log).map(_.proj).distinct.foreach(proj => {
			CrontabData.sum_up_day(day, proj)
			log(s"$proj sum up")
		})
	}

	def do_run_auto_log(time: (String, String), machine: (String, String, String), log_pattern: String, stat: (String, Int)): Unit = {
		val (stdout, stderr) = AutoLogCollector.collect(time._1, time._2, machine._1, machine._2, machine._3, log_pattern)
		if (stdout.nonEmpty) {
			val data = AutoLogCollector.parse(stdout)
			data.foreach(row => CrontabData.save_m(stat._1, stat._2, TimeUtil.minute2time(row._1), row._2))

			val size = data.foldLeft(0)(_ + _._2.length)
			log(s"${stat._1}-${stat._2} ok: size=$size")
		} else {
			log(s"${stat._1}-${stat._2} empty")
		}

		if (stderr.nonEmpty) {
			log(s"${stat._1}-${stat._2} err: $stderr")
		}
	}

}
