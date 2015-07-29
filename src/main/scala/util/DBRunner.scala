package util

import java.sql._
import javax.sql.DataSource

import org.apache.commons.dbutils.{AbstractQueryRunner, DbUtils, ResultSetHandler}
import util.DBRunner.{EachHandler, RawHandler, SeqHandler, SingleHandler}

import scala.collection.mutable.ArrayBuffer

object DBRunner {

	class RawHandler[T](mapper: ResultSet => T) extends ResultSetHandler[T] {
		override def handle(rs: ResultSet): T = {
			mapper.apply(rs)
		}
	}
	class SingleHandler[T](mapper: ResultSet => T, default: T = null) extends ResultSetHandler[T] {
		override def handle(rs: ResultSet): T = {
			if (rs.next) mapper.apply(rs) else default
		}
	}
	class SeqHandler[T](mapper: ResultSet => T) extends ResultSetHandler[Seq[T]] {
		override def handle(rs: ResultSet): Seq[T] = {
			val rows = new ArrayBuffer[T]
			while (rs.next)
				rows += mapper(rs)
			rows.toSeq
		}
	}

	class EachHandler(mapper: ResultSet => Unit) extends ResultSetHandler[Unit] {
		override def handle(rs: ResultSet): Unit = {
			while (rs.next)
				mapper(rs)
		}
	}
	class DumpHandler extends ResultSetHandler[Int] {
		override def handle(rs: ResultSet): Int = {
			val meta = rs.getMetaData
			val len = meta.getColumnCount

			var rows = 0
			while (rs.next()) {
				// bell(2014-6): 无输出则不print thead
				if (rows == 0) {
					val thead = (1 to len)
						.map(meta.getColumnLabel)
						.mkString(" \t ")
					println(thead)
				}

				rows += 1

				val line = (1 to len)
					.map(rs.getObject)
					.mkString(" \t ")
				println(line)
			}
			rows
		}
	}

	private val singleIntHandler = new SingleHandler[Int](_.getInt(1), 0)
	private val singleLongHandler = new SingleHandler[Long](_.getLong(1), 0)
	private val singleDoubleHandler = new SingleHandler[Double](_.getDouble(1), 0)
	private val singleStringHandler = new SingleHandler[String](_.getString(1), "")
	private val singleTimestampHandler = new SingleHandler[Timestamp](_.getTimestamp(1))
	private val singleBytesHandler = new SingleHandler[scala.Array[Byte]](_.getBytes(1))

	private val intSeqHandler = new SeqHandler[Int](_.getInt(1))
	private val longSeqHandler = new SeqHandler[Long](_.getLong(1))
	private val strSeqHandler = new SeqHandler[String](_.getString(1))
	private val strDoubleSeqHandler = new SeqHandler[(String, Double)](rs => (rs.getString(1), rs.getDouble(2)))
	private val intDoubleSeqHandler = new SeqHandler[(Int, Double)](rs => (rs.getInt(1), rs.getDouble(2)))
	private val intIntSeqHandler = new SeqHandler[(Int, Int)](rs => (rs.getInt(1), rs.getInt(2)))
	private val strStrDoubleSeqHandler = new SeqHandler[(String, String, Double)](rs => (rs.getString(1), rs.getString(2), rs.getDouble(3)))

	private val dumpHandler = new DumpHandler()
}

class DBRunner(ds: DataSource) extends AbstractQueryRunner(ds) {

	/* ------------------------- methods ------------------------- */

	def conn(): Connection = {
		this.prepareConnection()
	}

	// bell(2014-3): scala Any* -> java Object...
	// http://stackoverflow.com/questions/2334200/transforming-scala-varargs-into-java-object-varargs

	def queryInt(sql: String, params: Any*): Int = {
		query(sql, DBRunner.singleIntHandler, params: _*)
	}
	def queryIntWithDefault(sql: String, default: Int, params: Any*): Int = {
		query(sql, new SingleHandler[Int](_.getInt(1), default), params: _*)
	}
	def queryLong(sql: String, params: Any*): Long = {
		query(sql, DBRunner.singleLongHandler, params: _*)
	}
	def queryDouble(sql: String, params: Any*): Double = {
		query(sql, DBRunner.singleDoubleHandler, params: _*)
	}
	def queryString(sql: String, params: Any*): String = {
		query(sql, DBRunner.singleStringHandler, params: _*)
	}
	def queryTimestamp(sql: String, params: Any*): Timestamp = {
		query(sql, DBRunner.singleTimestampHandler, params: _*)
	}
	def queryBytes(sql: String, params: Any*): scala.Array[Byte] = {
		query(sql, DBRunner.singleBytesHandler, params: _*)
	}

	def queryIntSeq(sql: String, params: Any*): Seq[Int] = {
		query(sql, DBRunner.intSeqHandler, params: _*)
	}
	def queryLongSeq(sql: String, params: Any*): Seq[Long] = {
		query(sql, DBRunner.longSeqHandler, params: _*)
	}
	def queryStringSeq(sql: String, params: Any*): Seq[String] = {
		query(sql, DBRunner.strSeqHandler, params: _*)
	}

	def queryStrDblSeq(sql: String, params: Any*): Seq[(String, Double)] = {
		query(sql, DBRunner.strDoubleSeqHandler, params: _*)
	}
	def queryIntDblSeq(sql: String, params: Any*): Seq[(Int, Double)] = {
		query(sql, DBRunner.intDoubleSeqHandler, params: _*)
	}
	def queryIntIntSeq(sql: String, params: Any*): Seq[(Int, Int)] = {
		query(sql, DBRunner.intIntSeqHandler, params: _*)
	}
	def queryStrStrDblSeq(sql: String, params: Any*): Seq[(String, String, Double)] = {
		query(sql, DBRunner.strStrDoubleSeqHandler, params: _*)
	}

	def queryBean[T <: AnyRef](typ: Class[T], sql: String, params: Any*): T = {
		query(sql, new SingleHandler[T](RowMapperToBean.of(typ).apply, null.asInstanceOf[T]), params: _*)
	}
	def queryBeans[T <: AnyRef](typ: Class[T], sql: String, params: Any*): Seq[T] = {
		query(sql, new SeqHandler[T](RowMapperToBean.of(typ).apply), params: _*)
	}

	def queryOne[T](sql: String, mapper: ResultSet => T, params: Any*): T = {
		queryOneWithDefault[T](sql, null.asInstanceOf[T], mapper, params: _*)
	}
	def queryOneWithDefault[T](sql: String, default: T, mapper: ResultSet => T, params: Any*): T = {
		query(sql, new SingleHandler[T](mapper, default), params: _*)
	}

	def querySeq[T](sql: String, mapper: ResultSet => T, params: Any*): Seq[T] = {
		query(sql, new SeqHandler[T](mapper), params: _*)
	}
	def queryEach(sql: String, mapper: ResultSet => Unit, params: Any*): Unit = {
		query(sql, new EachHandler(mapper), params: _*)
	}
	def dump(sql: String, params: Any*): Int = {
		query(sql, DBRunner.dumpHandler, params: _*)
	}
	def query[T](sql: String, handler: ResultSet => T, params: Any*): T = {
		query(sql, new RawHandler[T](handler), params: _*)
	}

	/* ------------------------- impl ------------------------- */

	def insertReturnKey(sql: String, params: Any*): Long = {
		var conn: Connection = null
		var stmt: PreparedStatement = null
		var rs: ResultSet = null
		try {
			conn = prepareConnection
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
			fillAnyStatement(stmt, params: _*)
			stmt.executeUpdate
			rs = stmt.getGeneratedKeys
			if (rs.next) rs.getLong(1) else 0
		} catch {
			case e: SQLException => throw newException(e, sql, params: _*)
		} finally {
			DbUtils.closeQuietly(rs)
			DbUtils.closeQuietly(stmt)
			DbUtils.closeQuietly(conn)
		}
	}

	def query[T](sql: String, rsh: ResultSetHandler[T], params: Any*): T = {
		var conn: Connection = null
		var stmt: PreparedStatement = null
		var rs: ResultSet = null
		try {
			conn = prepareConnection

			// bell(2014-6): 避免大量数据时出现oom
			stmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
			stmt.setFetchSize(8192)
			stmt.setFetchDirection(ResultSet.FETCH_REVERSE)

			fillAnyStatement(stmt, params: _*)
			rs = stmt.executeQuery
			rsh.handle(rs)
		} catch {
			case e: SQLException => throw newException(e, sql, params: _*)
		} finally {
			DbUtils.closeQuietly(rs)
			DbUtils.closeQuietly(stmt)
			DbUtils.closeQuietly(conn)
		}
	}

	def update(sql: String, params: Any*): Int = {
		var conn: Connection = null
		var stmt: PreparedStatement = null
		try {
			conn = prepareConnection
			stmt = conn.prepareStatement(sql)
			fillAnyStatement(stmt, params: _*)
			stmt.executeUpdate()
		} catch {
			case e: SQLException => throw newException(e, sql, params: _*)
		} finally {
			DbUtils.closeQuietly(stmt)
			DbUtils.closeQuietly(conn)
		}
	}

	private def fillAnyStatement(stmt: PreparedStatement, params: Any*) {
		for (i <- params.indices; param = params(i)) {
			stmt.setObject(i + 1, param)
		}
	}
	private def newException(e: SQLException, sql: String, params: Any*): SQLException = {
		val msg = s"${e.getMessage} Query: $sql Parameters: ${params.mkString(", ")}"
		new SQLException(msg, e.getSQLState, e.getErrorCode)
	}

}
