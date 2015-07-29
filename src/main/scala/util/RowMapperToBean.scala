package util

import java.lang.reflect.{Field, Method, Modifier}
import java.sql.ResultSet
import java.util
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

import org.slf4j.LoggerFactory

class RowMapperToBean[T <: AnyRef](typ: Class[T]) {

	import RowMapperToBean._

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
		(1 to meta.getColumnCount).foreach(col => {
			val name = meta.getColumnLabel(col)
			fill(bean, rs, name, col)
		})
		bean
	}

	private def fill(bean: T, rs: ResultSet, db_col_name: String, col: Int): Unit = {
		val name = db_col_name.toLowerCase

		// map.put("foo_bar", X)
		if (bean.isInstanceOf[java.util.Map[_, _]]) {
			bean.asInstanceOf[java.util.Map[String, AnyRef]].put(name, rs.getObject(col))
			return
		}

		Seq(name, name.replace("_", "")).distinct.flatMap { n =>
			// Seq[(is_method, key)]
			Seq(
				(true, "set" + name), // call setFooBar(X)
				(true, name), // call fooBar(X)
				(true, name + "_$eq"), // call fooBar_$eq // Scala bean
				(false, name) // fill public fooBar;
			)
		}.foreach { case (is_method, key) =>
			if (is_method) {
				if (methods.contains(key)) {
					fill_method(methods(key), bean, rs, col)
					return
				}
			} else {
				if (fields.contains(key)) {
					fill_field(fields(key), bean, rs, col)
					return
				}
			}
		}

		if (name != "id") {
			if (missing_fields.add(name))
				logger.info(s"miss entry ${typ.getName} :: $db_col_name")
		}
	}
	private val missing_fields = new util.HashSet[String]()

}

object RowMapperToBean {

	private val logger = LoggerFactory.getLogger(getClass)

	/* ------------------------- fill rules ------------------------- */

	private def trans(rs: ResultSet, col: Int, typ: Class[_]): Any = {
		if (classOf[Int] == typ) rs.getInt(col)
		else if (classOf[Long] == typ)
			rs.getObject(col) match {
				case date: Date => date.getTime
				case _ => rs.getLong(col)
			}
		else if (classOf[Boolean] == typ) rs.getBoolean(col)
		else if (classOf[Double] == typ) rs.getDouble(col)
		else if (classOf[String] == typ) rs.getString(col)
		else if (classOf[Date] == typ) rs.getTimestamp(col)
		else {
			val sqlVal: AnyRef = rs.getObject(col)
			if (sqlVal == null) null
			else if (typ.isInstance(sqlVal)) sqlVal
			else {
				logger.error("TYPECAST: " + sqlVal.getClass + " -> " + typ)
				null
			}
		}
	}
	private def fill_method(method: Method, bean: Object, rs: ResultSet, col: Int): Unit = {
		val value = trans(rs, col, method.getParameterTypes.head)
		if (value != null)
			method.invoke(bean, value.asInstanceOf[Object])
	}
	private def fill_field(field: Field, bean: Object, rs: ResultSet, col: Int): Unit = {
		val value = trans(rs, col, field.getType)
		if (value != null)
			field.set(bean, value)
	}

	/* ------------------------- meta cache ------------------------- */

	private val _meta_cache = new ConcurrentHashMap[String, RowMapperToBean[_]]()
	def of[T <: AnyRef](typ: Class[T]): RowMapperToBean[T] = {
		val key = typ.getName
		var cache = _meta_cache.get(key).asInstanceOf[RowMapperToBean[T]]
		if (cache == null) {
			cache = new RowMapperToBean[T](typ)
			_meta_cache.put(key, cache)
		}
		cache
	}

}
