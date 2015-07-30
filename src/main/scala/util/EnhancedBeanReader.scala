package util

import java.lang.reflect.{Method, Modifier}
import java.util.concurrent.ConcurrentHashMap

import org.slf4j.{Logger, LoggerFactory}

protected[util] class EnhancedBeanReader(typ: Class[_]) {

	import EnhancedBeanReader.logger

	/* ------------------------- parse class ------------------------- */

	private val fields = typ.getFields.filter { f =>
		val mod = f.getModifiers
		!Modifier.isStatic(mod) && Modifier.isPublic(mod)
	}.map(f => (f.getName.toLowerCase, f)).toMap.withDefaultValue(null)

	private val methods = typ.getMethods.filter { m =>
		val mod = m.getModifiers
		!Modifier.isStatic(mod) && Modifier.isPublic(mod) &&
			m.getParameterCount == 0 && m.getReturnType != classOf[Unit]
	}.map(m => (m.getName.toLowerCase, m)).toMap.withDefaultValue(null)
	methods.values.foreach(_.setAccessible(true))

	private val method_getter: Method = typ.getMethods.find { m =>
		val mod = m.getModifiers
		!Modifier.isStatic(mod) && Modifier.isPublic(mod) &&
			m.getName == "get" && m.getParameterCount == 1 && m.getParameterTypes.head == classOf[String]
	}.orNull

	/* ------------------------- api ------------------------- */

	def get(bean: AnyRef, _name: String): AnyRef = {
		try {
			if (bean == null) return null

			val name = _name.toLowerCase

			val method1 = methods(name)
			if (method1 != null) return method1.invoke(bean)
			val method2 = methods("get" + name)
			if (method2 != null) return method2.invoke(bean)
			val field = fields(name)
			if (field != null) return field.get(bean)
			if (bean.isInstanceOf[java.util.Map[_, _]]) return bean.asInstanceOf[java.util.Map[_, AnyRef]].get(name)
			if (method_getter != null) return method_getter.invoke(bean, name)

			logger.info(s"fail get ${bean.getClass}::$name from $bean")
		} catch {
			case e: Exception =>
				val cause = if ((e.getClass.getName == "java.lang.reflect.InvocationTargetException") && e.getCause.isInstanceOf[Exception]) e.getCause else e
				logger.info(s"err get ${bean.getClass}::${_name} ($cause) bean($bean)")
		}
		null
	}

}

object EnhancedBeanReader {

	private val logger: Logger = LoggerFactory.getLogger(getClass)

	private val _meta_cache = new ConcurrentHashMap[String, EnhancedBeanReader]
	def of(typ: Class[_]): EnhancedBeanReader = {
		val key = typ.getName
		var cache = _meta_cache.get(key)
		if (cache == null) {
			cache = new EnhancedBeanReader(typ)
			_meta_cache.put(key, cache)
		}
		cache
	}

}

