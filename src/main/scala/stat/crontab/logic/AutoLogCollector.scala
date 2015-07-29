package stat.crontab.logic

import java.io.IOException

import util.{SSHClient, Utils}

object AutoLogCollector extends Utils {

	// bell(2013-12): 登录到对应服务器，获取对应时间的autolog内容
	// startTime, endTime格式为：yyyyMMddHHmm，目前不支持跨n天取数据
	// autologPath = "/search/odin/resin/log/transpic/stat.log.%s"
	/** @return (stdout, stderr) */
	def collect(start_time: String, end_time: String, host: String, user: String, pass: String, log_pattern: String): (Seq[String], Seq[String]) = {
		try {
			// todo: support more patterns
			val log_file = log_pattern.replace("${yyyyMMdd}", start_time.take(8))

			// bell(2013-12): autolog文件按天分隔，但有可能2359的日志出现在第二天的文件里，所以有可能需要cat两个文件
			// val logFile = Array(startTime, endTime).map(_.take(8)).distinct.map(logPath.format(_)).mkString(" ")
			// bell(2013-12): 使用awk截取对应时间区域的log
			val cmd = """fgrep "[AutoLog]" %s | sed 's/.*\]\s*//g' | awk '{if ($1 >= %s && $1 < %s){print $0}}'"""
				.format(log_file, start_time, end_time)

			val ssh = SSHClient.conn(host, user, pass)
			val (stdout, stderr) = ssh.exec(cmd)
			ssh.close()
			return (stdout, stderr)
		} catch {
			case e: IOException =>
				log("#run io err.", e)
			case e: Throwable =>
				log("#run _ err.", e)
		}
		(Seq.empty[String], Seq.empty[String])
	}

	// bell(2013-12): 此函数返回的结果已经按5分钟合并
	/** @return time -> (k,v,c) */
	def parse(data: Seq[String]): Map[String, Seq[(String, Double, Int)]] = {
		// (201311032304, crawl.cost, 1147.567469879518, 1660)
		val rows: Seq[(Long, String, Double, Int)] = data.map(line => line.trim match {
			case log_line_pattern(time, k, v, c) => (time.toLong, k, v.toDouble, c.toInt)
			case _ => null
		}).filter(_ != null)

		// 按5分钟合并
		// bell(2013-12): 注，由于存在server重启等情况，一个时间戳可能打出多条log，即使按分钟汇总，合并逻辑仍需要保留
		rows.groupBy(row => (row._1 - row._1 % 5).toString).mapValues(t_rows => {
			t_rows.groupBy(_._2).mapValues(k_rows => {
				val sum: Double = k_rows.foldLeft(0d)((sum, row) => sum + row._3 * row._4)
				val c: Int = k_rows.foldLeft(0)((sum, row) => sum + row._4)
				(sum / c, c)
			}).map(row => (row._1, row._2._1, row._2._2)).toSeq
		})
	}

	// 201311032304  crawl.hit  avg:0.6120481927710844  count:1660
	val log_line_pattern = "(\\d{12})\\s+(\\S{1,80})\\s+avg:(\\S+)\\s+count:(\\d+)".r

}
