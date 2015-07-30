package stat.web

import java.util.Timer

import stat.common.Env
import stat.web.entity.{ChartCondition, TimeType}
import util.TimeUtil

object WebEnv {

	def default_cond() = {
		val typ = TimeType.h
		new ChartCondition(typ, 0, TimeUtil.today().toInt,
			ChartCondition.default_l(typ), Env.web_def_proj, Env.web_def_key)
	}

	val timer = new Timer("stat", true)

	var context_path = ""

}
