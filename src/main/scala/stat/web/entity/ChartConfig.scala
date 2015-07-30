package stat.web.entity

import util.Utils

object ChartConfig {

	private def fixParam(param: String): String = {
		if (param.isEmpty)
			"var chartParams = {};"
		else if (param.startsWith("{") && param.endsWith("}"))
			s"var chartParams = $param;"
		else
			param
	}

	def get(cond: ChartCondition): ChartConfig = {
		autoConfig(cond.key)
	}
	private def getKClause(k: String): String = {
		if (!k.contains(",")) return getSubKClause(k)
		"(" + k.split(",").map(getSubKClause).mkString(" or ") + ")"
	}
	private def getSubKClause(keys: String): String = {
		if (keys.contains("*")) s"k like '${keys.replace("*", "%")}'" else s"k='$keys'"
	}
	private def getVCParam(k: String): String = {
		if (isMultiKey(k)) "" else "{ yAxis: [{opposite:true,min:0},{min:0}], series: [{yAxis:0}, {yAxis:1}] }"
	}
	private def isMultiKey(k: String): Boolean = {
		k.contains("*") || k.contains(",")
	}

	def autoConfig(id: String): ChartConfig = {
		val pKeyWithV = "^([\\w.*,-]+)-v$".r
		val pKeyWithC = "^([\\w.*,-]+)-c$".r
		val pKeyWithVC = "^([\\w.*,-]+)$".r

		id match {
			case pKeyWithV(keys) =>
				return new ChartConfig(Map(
					"id" -> id,
					"sql" -> s"select k,t,v {where} ${getKClause(keys)} group by p,s,k,t"))
			case pKeyWithC(keys) =>
				return new ChartConfig(Map(
					"id" -> id,
					"sql" -> s"select k,t,c {where} ${getKClause(keys)} group by p,s,k,t"))
			case pKeyWithVC(keys) =>
				return new ChartConfig(Map(
					"id" -> id,
					"sql" -> (s"(select concat(k,'-v'),t,v {where} ${getKClause(keys)} group by p,s,k,t)" +
						s" union (select concat(k,'-c'),t,c {where} ${getKClause(keys)} group by p,s,k,t)"),
					"param" -> getVCParam(keys)))
		}
		null
	}

}

case class ChartConfig(id: String, sql: String,
                       chart_params: String, comment: String) extends Utils {

	def this(data: Map[String, String]) {
		this(data("id"), data("sql"),
			ChartConfig.fixParam(data.getOrElse("param", "")),
			data.getOrElse("comment", ""))
	}

	def to_sql(cond: ChartCondition): Seq[Seq[String]] = {
		Config2Sql.gen_sql(sql, cond)
	}

}
