package stat.web.entity

import org.joda.time.format.DateTimeFormat

case class ChartDot(key: String, time: Long, value: Double) {

	def to_array: Array[Object] = {
		// ref: http://stackoverflow.com/questions/26546299/result-type-of-an-implicit-conversion-must-be-more-specific-than-anyref
		Array(key, ChartDot.time_format.print(time), value.asInstanceOf[java.lang.Double])
	}

}

object ChartDot {
	val time_format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
}
