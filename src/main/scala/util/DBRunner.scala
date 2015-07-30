package util

import java.sql._
import javax.sql.DataSource

import org.apache.commons.dbutils.{AbstractQueryRunner, DbUtils, ResultSetHandler}
import util.DBRunner.{EachMapper, RawMapper, SeqMapper, SingleMapper}

import scala.collection.mutable.ArrayBuffer

object DBRunner {

	class RawMapper[T](mapper: ResultSet => T) extends ResultSetHandler[T] {
		override def handle(rs: ResultSet): T = {
			mapper.apply(rs)
		}
	}
	class SingleMapper[T](mapper: ResultSet => T, default: T = null) extends ResultSetHandler[T] {
		override def handle(rs: ResultSet): T = {
			if (rs.next) mapper.apply(rs) else default
		}
	}
	class SeqMapper[T](mapper: ResultSet => T) extends ResultSetHandler[Seq[T]] {
		override def handle(rs: ResultSet): Seq[T] = {
			val rows = new ArrayBuffer[T]
			while (rs.next)
				rows += mapper(rs)
			rows.toSeq
		}
	}

	class EachMapper(mapper: ResultSet => Unit) extends ResultSetHandler[Unit] {
		override def handle(rs: ResultSet): Unit = {
			while (rs.next)
				mapper(rs)
		}
	}
	class DumpMapper extends ResultSetHandler[Int] {
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

	private val single_int_mapper = new SingleMapper[Int](_.getInt(1), 0)
	private val single_long_mapper = new SingleMapper[Long](_.getLong(1), 0)
	private val single_double_mapper = new SingleMapper[Double](_.getDouble(1), 0)
	private val single_string_mapper = new SingleMapper[String](_.getString(1), "")
	private val single_timestamp_mapper = new SingleMapper[Timestamp](_.getTimestamp(1))
	private val single_bytes_mapper = new SingleMapper[scala.Array[Byte]](_.getBytes(1))

	private val seq_int_mapper = new SeqMapper[Int](_.getInt(1))
	private val seq_long_mapper = new SeqMapper[Long](_.getLong(1))
	private val seq_str_mapper = new SeqMapper[String](_.getString(1))
	private val seq_str_double_mapper = new SeqMapper[(String, Double)](rs => (rs.getString(1), rs.getDouble(2)))
	private val seq_int_double_mapper = new SeqMapper[(Int, Double)](rs => (rs.getInt(1), rs.getDouble(2)))
	private val seq_int_int_mapper = new SeqMapper[(Int, Int)](rs => (rs.getInt(1), rs.getInt(2)))
	private val seq_str_str_double_mapper = new SeqMapper[(String, String, Double)](rs => (rs.getString(1), rs.getString(2), rs.getDouble(3)))

	private val dump_mapper = new DumpMapper()
}

class DBRunner(ds: DataSource) extends AbstractQueryRunner(ds) {

	/* ------------------------- methods ------------------------- */

	def conn(): Connection = {
		this.prepareConnection()
	}

	// bell(2014-3): scala Any* -> java Object...
	// http://stackoverflow.com/questions/2334200/transforming-scala-varargs-into-java-object-varargs

	def query_int(sql: String, params: Any*): Int = {
		query(sql, DBRunner.single_int_mapper, params: _*)
	}
	def query_int_with_default(sql: String, default: Int, params: Any*): Int = {
		query(sql, new SingleMapper[Int](_.getInt(1), default), params: _*)
	}
	def query_long(sql: String, params: Any*): Long = {
		query(sql, DBRunner.single_long_mapper, params: _*)
	}
	def query_double(sql: String, params: Any*): Double = {
		query(sql, DBRunner.single_double_mapper, params: _*)
	}
	def query_string(sql: String, params: Any*): String = {
		query(sql, DBRunner.single_string_mapper, params: _*)
	}
	def query_timestamp(sql: String, params: Any*): Timestamp = {
		query(sql, DBRunner.single_timestamp_mapper, params: _*)
	}
	def query_bytes(sql: String, params: Any*): scala.Array[Byte] = {
		query(sql, DBRunner.single_bytes_mapper, params: _*)
	}

	def query_int_seq(sql: String, params: Any*): Seq[Int] = {
		query(sql, DBRunner.seq_int_mapper, params: _*)
	}
	def query_long_seq(sql: String, params: Any*): Seq[Long] = {
		query(sql, DBRunner.seq_long_mapper, params: _*)
	}
	def query_string_seq(sql: String, params: Any*): Seq[String] = {
		query(sql, DBRunner.seq_str_mapper, params: _*)
	}

	def query_bean[T <: AnyRef](typ: Class[T], sql: String, params: Any*): T = {
		query(sql, new SingleMapper[T](DBBeanMapper.of(typ).apply, null.asInstanceOf[T]), params: _*)
	}
	def query_beans[T <: AnyRef](typ: Class[T], sql: String, params: Any*): Seq[T] = {
		query(sql, new SeqMapper[T](DBBeanMapper.of(typ).apply), params: _*)
	}

	def query_one[T](sql: String, mapper: ResultSet => T, params: Any*): T = {
		query_one_with_default[T](sql, null.asInstanceOf[T], mapper, params: _*)
	}
	def query_one_with_default[T](sql: String, default: T, mapper: ResultSet => T, params: Any*): T = {
		query(sql, new SingleMapper[T](mapper, default), params: _*)
	}

	def query_seq[T](sql: String, mapper: ResultSet => T, params: Any*): Seq[T] = {
		query(sql, new SeqMapper[T](mapper), params: _*)
	}
	def query_each(sql: String, mapper: ResultSet => Unit, params: Any*): Unit = {
		query(sql, new EachMapper(mapper), params: _*)
	}
	def dump(sql: String, params: Any*): Int = {
		query(sql, DBRunner.dump_mapper, params: _*)
	}
	def query[T](sql: String, mapper: ResultSet => T, params: Any*): T = {
		query(sql, new RawMapper[T](mapper), params: _*)
	}

	/* ------------------------- impl ------------------------- */

	def insert_return_key(sql: String, params: Any*): Long = {
		var conn: Connection = null
		var stmt: PreparedStatement = null
		var rs: ResultSet = null
		try {
			conn = prepareConnection
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
			fill_any_statement(stmt, params: _*)
			stmt.executeUpdate
			rs = stmt.getGeneratedKeys
			if (rs.next) rs.getLong(1) else 0
		} catch {
			case e: SQLException => throw new_exception(e, sql, params: _*)
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

			fill_any_statement(stmt, params: _*)
			rs = stmt.executeQuery
			rsh.handle(rs)
		} catch {
			case e: SQLException => throw new_exception(e, sql, params: _*)
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
			fill_any_statement(stmt, params: _*)
			stmt.executeUpdate()
		} catch {
			case e: SQLException => throw new_exception(e, sql, params: _*)
		} finally {
			DbUtils.closeQuietly(stmt)
			DbUtils.closeQuietly(conn)
		}
	}

	private def fill_any_statement(stmt: PreparedStatement, params: Any*) {
		for (i <- params.indices; param = params(i)) {
			stmt.setObject(i + 1, param)
		}
	}
	private def new_exception(e: SQLException, sql: String, params: Any*): SQLException = {
		val msg = s"${e.getMessage} Query: $sql Parameters: ${params.mkString(", ")}"
		new SQLException(msg, e.getSQLState, e.getErrorCode)
	}

}
