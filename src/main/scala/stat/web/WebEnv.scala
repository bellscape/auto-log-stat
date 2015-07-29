package stat.web

import stat.common.Env
import stat.web.entity.ChartCondition._
import stat.web.entity.{TimeType, ChartCondition}

object WebEnv {

	val default_cond = {
		val typ = TimeType.h
		new ChartCondition(typ, 0, today, ChartCondition.default_l(typ), Env.web_def_proj, Env.web_def_key)
	}

}
