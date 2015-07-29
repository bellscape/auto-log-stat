package debug

import stat.web.entity.ChartCondition
import util.Utils

object DebugWeb extends Utils {
	def main(args: Array[String]) {

		val path = "/"
		val cond = ChartCondition.parse(path)
		log(s"cond> $cond")

		// todo
		// ChartJspUtil.printChart ...

		log("fin")
		System.exit(0)
	}
}
