package util

import java.nio.charset.{Charset, CodingErrorAction}
import java.util.Random

import org.slf4j.LoggerFactory

import scala.io.Codec

trait Utils {

	val logger = LoggerFactory.getLogger(this.getClass)

	def pHr() {
		println("------------------------------------------------------------")
	}
	def log(start: Long, msg: String) {
		logger.info(f"${System.currentTimeMillis - start}% 6d ms | $msg")
	}
	def log(msg: String) {
		logger.info(msg)
	}
	def log(msg: String, e: Throwable) {
		logger.error(msg, e)
	}

	/* ------------------------- string ------------------------- */

	val utf8 = "UTF-8"
	val gbk = "GB18030"
	val charsetUtf8 = Charset.forName(utf8)
	val charsetGbk = Charset.forName(gbk)

	val codecUtf8 = {
		// bell(2014-3): 主要针对 Source.fromFile，避免异常字符打断流式处理
		// ref: http://stackoverflow.com/questions/7280956/how-to-skip-invalid-characters-in-stream-in-java-scala
		val decoderUtf8 = charsetUtf8.newDecoder()
		decoderUtf8.onMalformedInput(CodingErrorAction.IGNORE)
		Codec(decoderUtf8)
	}

	/* ------------------------- misc ------------------------- */

	def sleep(millis: Long) {
		if (millis > 0) Thread.sleep(millis)
	}

	def random(): Double = rand.nextDouble()
	def randInt(n: Int): Int = rand.nextInt(n)
	private final val rand: Random = new Random

}
