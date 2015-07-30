package stat.web

import javax.servlet.{ServletContextEvent, ServletContextListener}

import util.EnhancedBeanELResolver

class TheListener extends ServletContextListener {

	override def contextInitialized(sce: ServletContextEvent): Unit = {
		val ctx = sce.getServletContext
		WebEnv.context_path = ctx.getContextPath

		EnhancedBeanELResolver.register(ctx)
	}

	override def contextDestroyed(sce: ServletContextEvent): Unit = {
	}

}
