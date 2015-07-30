package util

import java.beans.FeatureDescriptor
import java.util.Iterator
import javax.el.{ELContext, ELResolver}
import javax.servlet.ServletContext
import javax.servlet.jsp.JspFactory

import org.slf4j.LoggerFactory

object EnhancedBeanELResolver {

	private val logger = LoggerFactory.getLogger(getClass)

	def register(ctx: ServletContext) {
		val jsp = JspFactory.getDefaultFactory.getJspApplicationContext(ctx)
		jsp.addELResolver(new EnhancedBeanELResolver)
	}

}

/** 扩充${foo.bar}的语义. @see {@link EnhancedBeanReader#get(Object, String)} */
class EnhancedBeanELResolver extends ELResolver {

	import EnhancedBeanELResolver.logger

	/* ------------------------- impl ------------------------- */

	def getValue(ctx: ELContext, base: AnyRef, name: AnyRef): AnyRef = {
		if (base == null || name == null || !name.isInstanceOf[String])
			null
		else if (base.getClass.getName.startsWith("javax.servlet.jsp.jstl") || base.getClass.getName.startsWith("com.caucho.jsp."))
			null
		else
			try {
				val meta = EnhancedBeanReader.of(base.getClass)
				val value = meta.get(base, name.asInstanceOf[String])
				ctx.setPropertyResolved(true)
				value
			} catch {
				case e: Exception =>
					logger.error("", e)
					null
			}
	}


	/* ------------------------- useless ------------------------- */

	override def getCommonPropertyType(ctx: ELContext, obj: AnyRef): Class[_] = null
	override def getFeatureDescriptors(ctx: ELContext, obj: AnyRef): Iterator[FeatureDescriptor] = null
	override def getType(ctx: ELContext, obj: AnyRef, obj1: AnyRef): Class[_] = null
	override def isReadOnly(ctx: ELContext, obj: AnyRef, obj1: AnyRef): Boolean = false
	override def setValue(ctx: ELContext, obj: AnyRef, obj1: AnyRef, obj2: AnyRef): Unit = {}

}
