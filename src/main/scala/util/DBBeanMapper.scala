package util

import java.lang.reflect.{Field, Method, Modifier}
import java.sql.ResultSet
import java.util
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

import org.slf4j.LoggerFactory

protected[util] class DBBeanMapper[T <: AnyRef](typ: Class[T]) {

	import DBBeanMapper._

	/* ------------------------- parse class ------------------------- */

	val fields = typ.getFields.filter { f =>
		val mod = f.getModifiers
		!Modifier.isStatic(mod) && Modifier.isPublic(mod)
	}.map(f => (f.getName.toLowerCase, f)).toMap

	val methods = typ.getMethods.filter { m =>
		val mod = m.getModifiers
		!Modifier.isStatic(mod) && Modifier.isPublic(mod) && m.getParameterCount == 1
	}.map(m => (m.getName.toLowerCase, m)).toMap

	/* ------------------------- api ------------------------- */

	def apply(rs: ResultSet): T = {
		val bean = typ.newInstance()
		val meta = rs.getMetaData
		(1 to meta.getColumnCount).foreach(col => fill(bean, rs, meta.getColumnLabel(col), col))
		bean
	}

	case class CallMethod(name: String)
	case class FillField(name: String)

	private def fill(bean: T, rs: ResultSet, db_col_name: String, col: Int): Unit = {
		val col_name = db_col_name.toLowerCase

		// map.put("foo_bar", X)
		if (bean.isInstanceOf[java.util.Map[_, _]]) {
			bean.asInstanceOf[java.util.Map[String, AnyRef]].put(col_name, rs.getObject(col))
			return
		}

		Seq(col_name, col_name.replace("_", "")).distinct // seq(foo_bar, foobar)
			.flatMap { name => Seq(
			CallMethod("set" + name),
			CallMethod(name),
			CallMethod(name + "_$eq"), // Scala bean
			FillField(name))
		}.foreach {
			case CallMethod(key) => if (methods.contains(key)) {
				fill_method(methods(key), bean, rs, col)
				return
			}
			case FillField(key) => if (fields.contains(key)) {
				fill_field(fields(key), bean, rs, col)
				return
			}
		}

		if (col_name != "id") {
			if (missing_fields.add(col_name))
				logger.info(s"miss entry ${typ.getName} :: $db_col_name")
		}
	}
	private val missing_fields = new util.HashSet[String]()

}

object DBBeanMapper {

	private val logger = LoggerFactory.getLogger(getClass)

	/* ------------------------- fill rules ------------------------- */

	// ref: http://www.scala-lang.org/old/node/1045
	private val (c_b, c_i, c_l, c_d, c_str, c_date) = (classOf[Boolean], classOf[Int], classOf[Long], classOf[Double], classOf[String], classOf[Date])
	private def convert(rs: ResultSet, col: Int, to_type: Class[_]): Any = {
		to_type match {
			case `c_b` => rs.getBoolean(col)
			case `c_i` => rs.getInt(col)
			case `c_l` => rs.getObject(col) match {
				case date: Date => date.getTime
				case _ => rs.getLong(col)
			}
			case `c_d` => rs.getDouble(col)
			case `c_str` => rs.getString(col)
			case `c_date` => rs.getTimestamp(col)
			case _ =>
				val sqlVal: AnyRef = rs.getObject(col)
				if (sqlVal == null) null
				else if (to_type.isInstance(sqlVal)) sqlVal
				else {
					logger.error("TYPECAST: " + sqlVal.getClass + " -> " + to_type)
					null
				}
		}
	}
	private def fill_method(method: Method, bean: Object, rs: ResultSet, col: Int): Unit = {
		val value = convert(rs, col, method.getParameterTypes.head)
		if (value != null)
			method.invoke(bean, value.asInstanceOf[Object])
	}
	private def fill_field(field: Field, bean: Object, rs: ResultSet, col: Int): Unit = {
		val value = convert(rs, col, field.getType)
		if (value != null)
			field.set(bean, value)
	}

	/* ------------------------- meta cache ------------------------- */

	private val _meta_cache = new ConcurrentHashMap[String, DBBeanMapper[_]]()
	def of[T <: AnyRef](typ: Class[T]): DBBeanMapper[T] = {
		val key = typ.getName
		var cache = _meta_cache.get(key).asInstanceOf[DBBeanMapper[T]]
		if (cache == null) {
			cache = new DBBeanMapper[T](typ)
			_meta_cache.put(key, cache)
		}
		cache
	}

}
