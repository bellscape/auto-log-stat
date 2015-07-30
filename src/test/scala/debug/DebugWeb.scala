package debug

import stat.web.entity.ChartCondition
import stat.web.logic.ChartJspUtil
import util.Utils

object DebugWeb extends Utils {
	def main(args: Array[String]) {

		val path = "/"
		val cond = ChartCondition.parse(path)
		log(s"cond> $cond")

		ChartJspUtil.print_chart(
			cond,
			System.out.println,
			force_refresh = false)

		log("fin")
		System.exit(0)
	}
}
