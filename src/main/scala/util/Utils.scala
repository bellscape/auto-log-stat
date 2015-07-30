package util

import java.nio.charset.{Charset, CodingErrorAction}

import org.slf4j.LoggerFactory

import scala.io.Codec

trait Utils {

	val logger = LoggerFactory.getLogger(this.getClass)

	def p_hr() {
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
	val charset_utf8 = Charset.forName(utf8)

	val codec_utf8 = {
		// bell(2014-3): 主要针对 Source.fromFile，避免异常字符打断流式处理
		// ref: http://stackoverflow.com/questions/7280956/how-to-skip-invalid-characters-in-stream-in-java-scala
		val decoderUtf8 = charset_utf8.newDecoder()
		decoderUtf8.onMalformedInput(CodingErrorAction.IGNORE)
		Codec(decoderUtf8)
	}

}
