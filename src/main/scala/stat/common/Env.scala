package stat.common

import java.io.InputStreamReader
import java.util.Properties
import javax.sql.DataSource

import com.alibaba.druid.pool.DruidDataSource
import util.{DBRunner, TimeUtil}

object Env {

	val (Array(host, dbname, user, pass), (web_def_proj, web_def_key)) = {
		val prop = new Properties()
		prop.load(new InputStreamReader(
			getClass.getClassLoader.getResourceAsStream("env.conf"),
			"UTF-8"))

		(
			"host,dbname,user,pass".split(",").map { key => prop.getProperty(s"env.db.autolog.$key") },
			"proj,key".split(",").map { key => prop.getProperty(s"web.ui.def.$key") })
	}

	val db = new DBRunner(get_datasource(host, 3306, dbname, user, pass, 10))

	/* ------------------------- impl ------------------------- */

	private def get_datasource(host: String, port: Int, dbname: String, user: String, pass: String, max_conn: Int): DataSource = {
		// ref: http://dev.mysql.com/doc/connector-j/en/connector-j-reference-configuration-properties.html
		val url = s"jdbc:mysql://$host:$port/$dbname?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=utf-8"

		// ref: https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_DruidDataSource%E5%8F%82%E8%80%83%E9%85%8D%E7%BD%AE
		val ds = new DruidDataSource
		ds.setUrl(url)
		ds.setUsername(user)
		ds.setPassword(pass)

		ds.setInitialSize(1)
		ds.setMinIdle(1)
		ds.setMaxActive(max_conn)
		ds.setMaxWait(1 * TimeUtil.minute)

		ds.setTimeBetweenEvictionRunsMillis(1 * TimeUtil.minute)
		ds.setMinEvictableIdleTimeMillis(5 * TimeUtil.minute)

		ds.setValidationQuery("select 'x'")
		ds.setTestWhileIdle(true)
		ds.setTestOnBorrow(false)
		ds.setTestOnReturn(false)

		ds.setPoolPreparedStatements(true)
		ds.setMaxPoolPreparedStatementPerConnectionSize(20)

		// ds.addFilters("mergeStat,wall");
		// mergeStat/wall 均无法正确处理这个sql：
		// replace into raw_d select p, s, k, t-interval(time_to_sec(t) mod 86400) second as d, sum(v*c)/sum(c) as v, sum(c) as c from raw_h_201504 where p='dev' group by p,s,k,d

		ds.addConnectionProperty("druid.stat.slowSqlMillis", "3000")
		ds.setRemoveAbandoned(true)
		ds.setRemoveAbandonedTimeoutMillis(30 * TimeUtil.minute)

		ds.init()
		ds
	}

}
