package util

import java.io.{IOException, InputStream}
import java.util.concurrent.atomic.AtomicInteger

import ch.ethz.ssh2.Connection

import scala.io.Source

object SSHClient {

	def conn(host: String, user: String, pass: String): SSHClient = {
		val conn = get_conn(host, user, pass)
		new SSHClient(conn)
	}
	private def get_conn(host: String, user: String, pass: String): Connection = {
		val conn = new Connection(host)
		conn.connect
		val authenticated: Boolean = conn.authenticateWithPassword(user, pass)
		if (!authenticated) throw new IOException("auth failed: " + host)
		conn
	}

}

class SSHClient(val conn: Connection) extends Utils {

	def close() = this.conn.close()

	/* ------------------------- api.exec ------------------------- */

	def exec(cmd: String): (Seq[String], Seq[String]) = {
		val sess = conn.openSession
		sess.execCommand(cmd, utf8)
		val stdout = consume(sess.getStdout)
		val stderr = consume(sess.getStderr)
		sess.close()
		(stdout, stderr)
	}
	private def consume(is: InputStream): Seq[String] = {
		Source.fromInputStream(is)(codecUtf8).getLines().toArray.toSeq
	}

	/* ------------------------- api.consume ------------------------- */

	def consume(remoteFile: String, consumer: InputStream => Unit) {
		val in = conn.createSCPClient.get(remoteFile)
		try {
			consumer(in)
		} finally {
			if (in != null) in.close()
		}
	}

	/* ------------------------- api.exec ------------------------- */

	def exec(cmd: String, consumer: String => Unit, show_err: Boolean = false) {
		val start = System.currentTimeMillis
		val total = new AtomicInteger
		val sess = conn.openSession
		sess.execCommand(cmd, utf8)

		Source.fromInputStream(sess.getStdout)(codecUtf8).getLines().foreach(line => {
			total.incrementAndGet()
			consumer(line)
		})

		if (show_err)
			Source.fromInputStream(sess.getStderr)(codecUtf8).getLines().foreach(line => {
				println(s"err> $line")
			})

		sess.close()
		println(s"ssh remote (${System.currentTimeMillis - start} ms, ${total.get} lines)>> $cmd")
	}

}
