package stat.common

case class Project(var host: String,
                   var host_user: String,
                   var host_pass: String,
                   var proj: String,
                   var server: Int,
                   var log_pattern: String) {

	def this() {
		this("", "", "", "", 0, "")
	}

	def has_log: Boolean = log_pattern.nonEmpty

}

object Project {

	val projects = Env.db.queryBeans(new Project().getClass, "select * from conf_project where enable > 0 order by id")

	def get_server_size(proj: String): Int = {
		projects.find(_.proj == proj) match {
			case Some(p) => p.server
			case None => 0
		}
	}

}
